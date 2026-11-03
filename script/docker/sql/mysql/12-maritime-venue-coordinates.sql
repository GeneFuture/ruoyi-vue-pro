-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 12-maritime-venue-coordinates.sql
-- 班期场地信息（学校信息）字段扩展脚本（已在运行环境执行 08 的库使用）：
--   给 maritime_session_venue 增加经纬度坐标，小程序端班期详情据此提供「导航」入口。
-- 说明字段（学校概况 / 住宿条件 / 教学条件）沿用既有富文本列，不新增图片字段。
-- 全新环境直接执行 08 即可（已包含以上内容），无需执行本文件。
-- =====================================================================

-- MySQL 8 的 ADD COLUMN 不支持 IF NOT EXISTS，用 information_schema 判断保证可重复执行
SET @add_latitude := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_venue ADD COLUMN latitude DECIMAL(10,7) DEFAULT NULL COMMENT ''纬度（-90 ~ 90）'' AFTER address',
            'SELECT ''latitude 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_venue' AND COLUMN_NAME = 'latitude'
);
PREPARE stmt FROM @add_latitude;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_longitude := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_venue ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL COMMENT ''经度（-180 ~ 180）'' AFTER latitude',
            'SELECT ''longitude 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_venue' AND COLUMN_NAME = 'longitude'
);
PREPARE stmt FROM @add_longitude;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 同步列注释为学校语义（列名保持不变，仅注释变更；可重复执行）
ALTER TABLE maritime_session_venue
  MODIFY COLUMN campus_name       VARCHAR(100)  NOT NULL DEFAULT '' COMMENT '学校名称',
  MODIFY COLUMN address           VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '学校地址',
  MODIFY COLUMN campus_intro      TEXT          DEFAULT NULL COMMENT '学校概况（富文本HTML）',
  MODIFY COLUMN accommodation_intro TEXT        DEFAULT NULL COMMENT '住宿条件（富文本HTML，可空）',
  MODIFY COLUMN classroom_intro   TEXT          DEFAULT NULL COMMENT '教学条件（富文本HTML）';
