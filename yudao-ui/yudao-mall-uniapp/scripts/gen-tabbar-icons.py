#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成小程序 tabBar 图标（微信要求 81x81 PNG，透明背景）。

纯 stdlib 实现（zlib + struct），不依赖 PIL / ImageMagick。
图形用"距离场 + 4x 超采样"光栅化，得到平滑的描边线条。

用法：python3 scripts/gen-tabbar-icons.py
输出：static/tabbar/{home,session,message,mine}[-active].png
"""

import math
import os
import struct
import zlib

SIZE = 81            # 输出尺寸（微信推荐 81x81）
VIEW = 24.0          # 逻辑坐标系（与 SVG viewBox 一致）
SS = 4               # 超采样倍数
STROKE = 1.9         # 描边宽度（逻辑坐标）
COLOR_NORMAL = (0x99, 0x99, 0x99)
COLOR_ACTIVE = (0x1E, 0x5E, 0xFF)

OUT_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'static', 'tabbar')


# ---------------------------------------------------------------- 图元
def seg(x1, y1, x2, y2):
    """线段（胶囊形描边）"""
    return ('seg', x1, y1, x2, y2)


def ring(cx, cy, r):
    """整圆描边"""
    return ('ring', cx, cy, r)


def arc(cx, cy, r, a0, a1):
    """圆弧描边，角度制，0° 指向 +x，顺时针（屏幕坐标 y 向下）"""
    return ('arc', cx, cy, r, a0, a1)


def rrect(x, y, w, h, r):
    """圆角矩形描边"""
    return ('rrect', x, y, w, h, r)


def dot(cx, cy, r):
    """实心圆点"""
    return ('dot', cx, cy, r)


def _dist_seg(px, py, x1, y1, x2, y2):
    dx, dy = x2 - x1, y2 - y1
    if dx == 0 and dy == 0:
        return math.hypot(px - x1, py - y1)
    t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)
    t = max(0.0, min(1.0, t))
    return math.hypot(px - (x1 + t * dx), py - (y1 + t * dy))


def _dist_arc(px, py, cx, cy, r, a0, a1):
    ang = math.degrees(math.atan2(py - cy, px - cx)) % 360
    lo, hi = a0 % 360, a1 % 360
    inside = (lo <= ang <= hi) if lo <= hi else (ang >= lo or ang <= hi)
    if inside:
        return abs(math.hypot(px - cx, py - cy) - r)
    e0 = (cx + r * math.cos(math.radians(lo)), cy + r * math.sin(math.radians(lo)))
    e1 = (cx + r * math.cos(math.radians(hi)), cy + r * math.sin(math.radians(hi)))
    return min(math.hypot(px - e0[0], py - e0[1]), math.hypot(px - e1[0], py - e1[1]))


def _dist_rrect(px, py, x, y, w, h, r):
    """圆角矩形边界距离（描边用）"""
    cx = min(max(px, x + r), x + w - r)
    cy = min(max(py, y + r), y + h - r)
    d = math.hypot(px - cx, py - cy)
    return abs(d - r)


def coverage(prims, px, py):
    """返回该点的覆盖强度 0/1（是否落在描边内）"""
    for p in prims:
        if p[0] == 'seg':
            if _dist_seg(px, py, p[1], p[2], p[3], p[4]) <= STROKE / 2:
                return 1.0
        elif p[0] == 'ring':
            if abs(math.hypot(px - p[1], py - p[2]) - p[3]) <= STROKE / 2:
                return 1.0
        elif p[0] == 'arc':
            if _dist_arc(px, py, p[1], p[2], p[3], p[4], p[5]) <= STROKE / 2:
                return 1.0
        elif p[0] == 'rrect':
            if _dist_rrect(px, py, p[1], p[2], p[3], p[4], p[5]) <= STROKE / 2:
                return 1.0
        elif p[0] == 'dot':
            if math.hypot(px - p[1], py - p[2]) <= p[3]:
                return 1.0
    return 0.0


# ---------------------------------------------------------------- 图标定义
ICONS = {
    # 首页：房子
    'home': [
        seg(3.4, 10.4, 12.0, 3.4),
        seg(12.0, 3.4, 20.6, 10.4),
        seg(5.6, 9.6, 5.6, 20.4),
        seg(5.6, 20.4, 18.4, 20.4),
        seg(18.4, 20.4, 18.4, 9.6),
    ],
    # 开班：日历
    'session': [
        rrect(3.4, 5.4, 17.2, 15.2, 3.2),
        seg(3.4, 10.2, 20.6, 10.2),
        seg(8.2, 3.0, 8.2, 7.2),
        seg(15.8, 3.0, 15.8, 7.2),
        dot(8.2, 14.4, 1.15),
        dot(12.0, 14.4, 1.15),
        dot(15.8, 14.4, 1.15),
        dot(8.2, 17.9, 1.15),
        dot(12.0, 17.9, 1.15),
    ],
    # 消息：对话气泡
    'message': [
        rrect(3.4, 4.2, 17.2, 13.2, 3.4),
        seg(7.0, 17.4, 7.0, 22.2),
        seg(7.0, 22.2, 13.4, 17.4),
    ],
    # 我的：人像
    'mine': [
        ring(12.0, 8.0, 3.7),
        arc(12.0, 21.4, 6.6, 180, 360),
    ],
}


# ---------------------------------------------------------------- PNG 写出
def write_png(path, width, height, rgba_rows):
    raw = bytearray()
    for row in rgba_rows:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        return c + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b'\x89PNG\r\n\x1a\n'
    png += chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
    png += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
    png += chunk(b'IEND', b'')
    with open(path, 'wb') as f:
        f.write(png)


def render(prims, rgb):
    scale = SIZE / VIEW
    rows = []
    for py in range(SIZE):
        row = []
        for px in range(SIZE):
            acc = 0.0
            for sy in range(SS):
                for sx in range(SS):
                    # 像素中心 -> 逻辑坐标
                    lx = ((px + (sx + 0.5) / SS) / scale)
                    ly = ((py + (sy + 0.5) / SS) / scale)
                    acc += coverage(prims, lx, ly)
            a = int(round(255 * acc / (SS * SS)))
            row.append((rgb[0], rgb[1], rgb[2], a))
        rows.append(row)
    return rows


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, prims in ICONS.items():
        for suffix, rgb in (('', COLOR_NORMAL), ('-active', COLOR_ACTIVE)):
            path = os.path.join(OUT_DIR, f'{name}{suffix}.png')
            write_png(path, SIZE, SIZE, render(prims, rgb))
            print(f'  {os.path.relpath(path)}  {os.path.getsize(path)} bytes')
    print(f'\n完成，共 {len(ICONS) * 2} 个图标 -> {os.path.relpath(OUT_DIR)}')


if __name__ == '__main__':
    main()
