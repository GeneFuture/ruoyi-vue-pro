#!/bin/bash
# 把本机构建产物装配到 deploy/runtime/，用于快速构建 amd64 运行时镜像
# 用法：bash deploy/prepare-runtime.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RT="$ROOT/deploy/runtime"
JAR="$ROOT/yudao-server/target/yudao-server.jar"
WEB="$ROOT/yudao-ui/yudao-ui-admin-vue3/dist-prod"

if [ ! -f "$JAR" ]; then
  echo "[prepare-runtime] 缺少 $JAR"
  echo "  请先执行：mvn -T 4 -DskipTests clean package -pl yudao-server -am"
  exit 1
fi

if [ ! -d "$WEB" ]; then
  echo "[prepare-runtime] 缺少 $WEB"
  echo "  请先执行：cd yudao-ui/yudao-ui-admin-vue3 && pnpm build:prod"
  exit 1
fi

mkdir -p "$RT/web"
cp "$JAR" "$RT/app.jar"

# 用 rsync 清理上一次构建的残留文件；没有 rsync 时退回 cp
if command -v rsync >/dev/null 2>&1; then
  rsync -a --delete "$WEB/" "$RT/web/"
else
  cp -R "$WEB/." "$RT/web/"
fi

cp "$ROOT/deploy/nginx.conf" "$RT/nginx.conf"
cp "$ROOT/deploy/entrypoint.sh" "$RT/entrypoint.sh"

echo "[prepare-runtime] 完成："
du -sh "$RT/app.jar" "$RT/web"
