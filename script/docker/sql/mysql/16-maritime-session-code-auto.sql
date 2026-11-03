-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 16-maritime-session-code-auto.sql
-- 班期编号改为系统自动生成（C{课程ID 36进制}-{开班年月}-{3位序号}，如 C1-202610-001）：
--   1. 唯一键加入 tenant_id，避免不同租户生成相同编号时冲突
--   2. 删除旧唯一键 uk_session_code(session_code, deleted)
-- 全新环境直接执行 08 即可（已包含以上内容），无需执行本文件。
-- 存量班期编号保持不变，仅新建班期使用新格式。
-- =====================================================================

-- ========== 1. 删除旧唯一键 ==========
SET @drop_uk := (
  SELECT IF(COUNT(*) > 0,
            'ALTER TABLE maritime_course_session DROP INDEX uk_session_code',
            'SELECT ''uk_session_code 不存在，跳过''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_course_session' AND INDEX_NAME = 'uk_session_code'
);
PREPARE stmt FROM @drop_uk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 2. 新增带租户维度的唯一键 ==========
SET @add_uk := (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE maritime_course_session ADD UNIQUE INDEX uk_tenant_session_code (tenant_id, session_code, deleted)',
            'SELECT ''uk_tenant_session_code 已存在，跳过''')
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'maritime_course_session' AND INDEX_NAME = 'uk_tenant_session_code'
);
PREPARE stmt FROM @add_uk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
