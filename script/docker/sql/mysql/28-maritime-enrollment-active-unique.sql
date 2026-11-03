-- ============================================================
-- 海员培训：报名唯一索引改为「仅有效报名唯一」
--
-- 背景：原唯一索引 uk_id_card_session (id_card, session_id, deleted) 在取消报名后
--       仍然占用唯一槽位（取消只改 enrollment_status，deleted 仍为 0），导致学员
--       取消后无法重新报名同一班期；同时多笔已取消记录之间也会互相冲突。
--
-- 方案：新增生成列 active_flag：
--       未取消且未删除 = 1，取消/删除 = NULL；
--       MySQL 唯一索引允许多个 NULL，因此：
--         - 同一身份证 + 班期最多只能有一条有效报名（防并发重复报名）
--         - 取消后可以重新报名，且允许多条历史取消记录
--
-- 执行前自检（有结果说明库中已存在重复有效报名，需人工先处理，否则加索引会失败）：
--   SELECT id_card, session_id, COUNT(*) AS cnt
--   FROM maritime_enrollment
--   WHERE deleted = 0 AND enrollment_status <> 'CANCELLED'
--   GROUP BY id_card, session_id
--   HAVING cnt > 1;
-- ============================================================

-- 1. 新增生成列（存在则跳过）
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'maritime_enrollment'
      AND COLUMN_NAME = 'active_flag'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE maritime_enrollment ADD COLUMN active_flag TINYINT GENERATED ALWAYS AS (CASE WHEN deleted = 0 AND enrollment_status <> ''CANCELLED'' THEN 1 ELSE NULL END) STORED COMMENT ''有效报名标记（仅用于唯一索引，业务不读写）'' AFTER tenant_id',
    'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 删除旧唯一索引（存在则删）
SET @old_idx := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'maritime_enrollment'
      AND INDEX_NAME = 'uk_id_card_session'
);
SET @ddl := IF(@old_idx > 0,
    'ALTER TABLE maritime_enrollment DROP INDEX uk_id_card_session',
    'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 新增「仅有效报名唯一」索引（存在则跳过）
SET @new_idx := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'maritime_enrollment'
      AND INDEX_NAME = 'uk_id_card_session_active'
);
SET @ddl := IF(@new_idx = 0,
    'ALTER TABLE maritime_enrollment ADD UNIQUE INDEX uk_id_card_session_active (id_card, session_id, active_flag)',
    'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
