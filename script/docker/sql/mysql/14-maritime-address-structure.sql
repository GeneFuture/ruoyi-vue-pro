-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 14-maritime-address-structure.sql
-- 学校地址结构化 + 删除班期「上课地点」升级脚本（已在运行环境执行 08 的库使用）：
--   1. maritime_school / maritime_session_venue 增加 province / city / district，
--      原 address 语义收窄为「详细地址」（省市区之后的部分）
--   2. maritime_course_session 删除 location（本期上课地点）
-- 全新环境直接执行 08 即可（已包含以上内容），无需执行本文件。
--
-- 背景：
--   * 学校地址原来是一整串自由文本，靠手工敲；现在改成省 / 市 / 区 + 详细地址四段，
--     管理后台用百度地图拾取，逆地理编码直接回填省市区，减少录入错误。
--   * 班期上的「上课地点」与学校信息（班期场地）表达的是同一件事，两处并存会互相打架，
--     且列表卡片与城市筛选都改从场地取城市，故整个字段删除。
--
-- 关于旧数据（重要）：
--   * 学校 / 场地的旧 address 是完整地址串，本脚本**不做自动拆分**——拆分靠正则不可靠，
--     拆错会静默写入错误数据。新增的三列留空，此时「省市区 + 详细地址」拼出来
--     与原字符串完全一致，所以小程序端展示不变；需要结构化的学校在后台重新拾取一次即可。
--   * 班期的 location 是自由文本（既有城市名也有机构名），随列一起删除，不做回填。
--     如果确实要保留，请在执行第 4 步之前先把值搬到需要的地方，例如：
--       UPDATE maritime_session_venue v JOIN maritime_course_session s ON s.id = v.session_id
--          SET v.city = s.location
--        WHERE v.deleted = 0 AND v.city = '' AND s.location <> '';
-- =====================================================================

-- ========== 1. maritime_school 增加省 / 市 / 区 ==========
-- MySQL 8 的 ADD COLUMN 不支持 IF NOT EXISTS，用 information_schema 判断保证可重复执行
SET @add_school_province := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_school ADD COLUMN province VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''省'' AFTER name',
            'SELECT ''maritime_school.province 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_school' AND COLUMN_NAME = 'province'
);
PREPARE stmt FROM @add_school_province;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_school_city := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_school ADD COLUMN city VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''市（列表按此筛选）'' AFTER province',
            'SELECT ''maritime_school.city 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_school' AND COLUMN_NAME = 'city'
);
PREPARE stmt FROM @add_school_city;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_school_district := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_school ADD COLUMN district VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''区 / 县'' AFTER city',
            'SELECT ''maritime_school.district 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_school' AND COLUMN_NAME = 'district'
);
PREPARE stmt FROM @add_school_district;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- address 语义收窄为「详细地址」，注释同步（数据不动）
ALTER TABLE maritime_school
  MODIFY COLUMN address VARCHAR(255) NOT NULL DEFAULT '' COMMENT '详细地址（省市区之后的部分）';

-- ========== 2. maritime_session_venue 增加省 / 市 / 区 ==========
SET @add_venue_province := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_venue ADD COLUMN province VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''省'' AFTER campus_name',
            'SELECT ''maritime_session_venue.province 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_venue' AND COLUMN_NAME = 'province'
);
PREPARE stmt FROM @add_venue_province;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_venue_city := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_venue ADD COLUMN city VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''市（列表按此筛选）'' AFTER province',
            'SELECT ''maritime_session_venue.city 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_venue' AND COLUMN_NAME = 'city'
);
PREPARE stmt FROM @add_venue_city;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_venue_district := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_venue ADD COLUMN district VARCHAR(50) NOT NULL DEFAULT '''' COMMENT ''区 / 县'' AFTER city',
            'SELECT ''maritime_session_venue.district 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_venue' AND COLUMN_NAME = 'district'
);
PREPARE stmt FROM @add_venue_district;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE maritime_session_venue
  MODIFY COLUMN address VARCHAR(255) NOT NULL DEFAULT '' COMMENT '详细地址（省市区之后的部分）';

-- ========== 3. 班期表删除「上课地点」索引 ==========
SET @drop_location_index := (
  SELECT IF(COUNT(*) > 0,
            'ALTER TABLE maritime_course_session DROP INDEX idx_location',
            'SELECT ''idx_location 不存在，跳过''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_course_session' AND INDEX_NAME = 'idx_location'
);
PREPARE stmt FROM @drop_location_index;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 4. 班期表删除「上课地点」列（不可逆，旧值一并丢失，见文件头说明） ==========
SET @drop_location_column := (
  SELECT IF(COUNT(*) > 0,
            'ALTER TABLE maritime_course_session DROP COLUMN location',
            'SELECT ''location 不存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_course_session' AND COLUMN_NAME = 'location'
);
PREPARE stmt FROM @drop_location_column;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
