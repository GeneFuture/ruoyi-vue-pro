-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 15-maritime-extra-fee.sql
-- 费用模型调整脚本（已在运行环境执行过 08 的库使用）：
--   1. 费用方案新增 tuition_includes_accommodation：学费是否包含住宿费
--   2. tuition_description（学费说明，键值对）改名为 extra_fee_items 并改语义：
--      由「学费构成明细」改为「另行缴费项目」——到校报到时缴纳的项目，每项分必须 / 可选，
--      形状由 {"理论课":"1000"} 变为 [{"name":"教材费","amount":200,"required":true}]
-- 全新环境直接执行 08 即可（已包含以上内容），无需执行本文件。
--
-- 关于旧数据（重要）：
--   旧的键值对是「学费的组成部分」（理论课 / 实操），与「到校另行缴纳的项目」不是一回事，
--   无法自动转换，硬转会产生语义错误的数据。因此本脚本把旧形状（JSON 对象）的值**清空**，
--   请在后台按新的含义重新录入。只清 JSON 对象，已是数组的新数据不受影响（可重复执行）。
-- =====================================================================

-- ========== 1. 两张表新增「学费是否包含住宿费」 ==========
SET @add_tpl_accom := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_fee_template ADD COLUMN tuition_includes_accommodation TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''学费是否包含住宿费'' AFTER tuition_amount',
            'SELECT ''maritime_fee_template.tuition_includes_accommodation 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_fee_template' AND COLUMN_NAME = 'tuition_includes_accommodation'
);
PREPARE stmt FROM @add_tpl_accom;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_cfg_accom := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_session_fee_config ADD COLUMN tuition_includes_accommodation TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''学费是否包含住宿费'' AFTER tuition_amount',
            'SELECT ''maritime_session_fee_config.tuition_includes_accommodation 已存在，跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_fee_config' AND COLUMN_NAME = 'tuition_includes_accommodation'
);
PREPARE stmt FROM @add_cfg_accom;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 2. tuition_description 改名为 extra_fee_items ==========
SET @rename_tpl := (
  SELECT IF(COUNT(*) > 0,
            'ALTER TABLE maritime_fee_template CHANGE COLUMN tuition_description extra_fee_items JSON DEFAULT NULL COMMENT ''另行缴费项目（[{"name":"教材费","amount":200,"required":true}]）''',
            'SELECT ''maritime_fee_template.tuition_description 不存在（已是新结构），跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_fee_template' AND COLUMN_NAME = 'tuition_description'
);
PREPARE stmt FROM @rename_tpl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @rename_cfg := (
  SELECT IF(COUNT(*) > 0,
            'ALTER TABLE maritime_session_fee_config CHANGE COLUMN tuition_description extra_fee_items JSON DEFAULT NULL COMMENT ''另行缴费项目（[{"name":"教材费","amount":200,"required":true}]）''',
            'SELECT ''maritime_session_fee_config.tuition_description 不存在（已是新结构），跳过''')
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_session_fee_config' AND COLUMN_NAME = 'tuition_description'
);
PREPARE stmt FROM @rename_cfg;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 3. 清空旧形状数据（只清 JSON 对象，数组不动） ==========
UPDATE maritime_fee_template
   SET extra_fee_items = NULL
 WHERE extra_fee_items IS NOT NULL
   AND JSON_TYPE(extra_fee_items) = 'OBJECT';

UPDATE maritime_session_fee_config
   SET extra_fee_items = NULL
 WHERE extra_fee_items IS NOT NULL
   AND JSON_TYPE(extra_fee_items) = 'OBJECT';
