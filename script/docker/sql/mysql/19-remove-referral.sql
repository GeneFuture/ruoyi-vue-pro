-- ============================================================
-- 海员培训：彻底移除推荐返佣功能（数据库清理）
-- 警告：会删除推荐关系、佣金、提现、税务、风控及历史消息，不可恢复
-- 执行前请备份数据库
-- 执行后需清理 Redis 模板缓存：
--   redis-cli --scan --pattern 'notify_template:*' | xargs -r redis-cli DEL
-- ============================================================

-- 1. 删除推荐/佣金/提现/税务/风控表
DROP TABLE IF EXISTS maritime_referral_relation;
DROP TABLE IF EXISTS maritime_commission_record;
DROP TABLE IF EXISTS maritime_commission_account;
DROP TABLE IF EXISTS maritime_withdrawal_apply;
DROP TABLE IF EXISTS maritime_tax_record;
DROP TABLE IF EXISTS maritime_risk_blacklist;
DROP TABLE IF EXISTS maritime_risk_event;

-- 2. 删除菜单与角色授权（按组件/权限匹配，兼容新旧菜单 ID）
DROP TEMPORARY TABLE IF EXISTS tmp_referral_menu_ids;
CREATE TEMPORARY TABLE tmp_referral_menu_ids AS
SELECT id FROM system_menu
WHERE deleted = 0 AND (
    component LIKE 'maritime/referralRelation/%'
    OR component LIKE 'maritime/commissionAccount/%'
    OR component LIKE 'maritime/commissionRecord/%'
    OR component LIKE 'maritime/withdrawal/%'
    OR component LIKE 'maritime/member/%'
    OR component LIKE 'maritime/riskEvent/%'
    OR component LIKE 'maritime/blacklist/%'
    OR permission LIKE 'maritime:referral%'
    OR permission LIKE 'maritime:commission%'
    OR permission LIKE 'maritime:risk%'
    OR permission LIKE 'maritime:member%'
);
DELETE FROM system_role_menu WHERE menu_id IN (SELECT id FROM tmp_referral_menu_ids);
DELETE FROM system_menu WHERE id IN (SELECT id FROM tmp_referral_menu_ids);
DROP TEMPORARY TABLE tmp_referral_menu_ids;

-- 3. 删除佣金定时任务（数据库记录 + Quartz 触发器）
DELETE FROM infra_job_log WHERE job_id IN (SELECT id FROM infra_job WHERE handler_name IN ('commissionSettleCheckJob', 'commissionPayoutJob'));
DELETE FROM infra_job WHERE handler_name IN ('commissionSettleCheckJob', 'commissionPayoutJob');
DELETE FROM QRTZ_CRON_TRIGGERS WHERE TRIGGER_NAME IN ('commissionSettleCheckJob', 'commissionPayoutJob');
DELETE FROM QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME IN ('commissionSettleCheckJob', 'commissionPayoutJob');
DELETE FROM QRTZ_SIMPROP_TRIGGERS WHERE TRIGGER_NAME IN ('commissionSettleCheckJob', 'commissionPayoutJob');
DELETE FROM QRTZ_TRIGGERS WHERE TRIGGER_NAME IN ('commissionSettleCheckJob', 'commissionPayoutJob');
DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME IN ('commissionSettleCheckJob', 'commissionPayoutJob');

-- 4. 删除通知模板与历史通知
DELETE FROM system_notify_template WHERE code IN (
    'MARITIME_REFERRAL_SUCCESS',
    'MARITIME_COMMISSION_APPROVED',
    'MARITIME_COMMISSION_REJECTED',
    'MARITIME_COMMISSION_PAID',
    'MARITIME_WITHDRAWAL_APPLIED',
    'MARITIME_WITHDRAWAL_SUCCESS',
    'MARITIME_WITHDRAWAL_FAILED',
    'MARITIME_WITHDRAWAL_REJECTED'
);
DELETE FROM system_notify_message WHERE template_code IN (
    'MARITIME_REFERRAL_SUCCESS',
    'MARITIME_COMMISSION_APPROVED',
    'MARITIME_COMMISSION_REJECTED',
    'MARITIME_COMMISSION_PAID',
    'MARITIME_WITHDRAWAL_APPLIED',
    'MARITIME_WITHDRAWAL_SUCCESS',
    'MARITIME_WITHDRAWAL_FAILED',
    'MARITIME_WITHDRAWAL_REJECTED'
);
-- 5. 删除推荐相关字段（存在才删）
DROP PROCEDURE IF EXISTS drop_maritime_column;
DELIMITER //
CREATE PROCEDURE drop_maritime_column(IN tableName VARCHAR(64), IN columnName VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tableName AND COLUMN_NAME = columnName
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', tableName, '` DROP COLUMN `', columnName, '`');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

-- member_user
CALL drop_maritime_column('member_user', 'referral_code');
CALL drop_maritime_column('member_user', 'referred_by_id');
CALL drop_maritime_column('member_user', 'is_referral_banned');

-- maritime_enrollment
CALL drop_maritime_column('maritime_enrollment', 'referred_by_member_id');
CALL drop_maritime_column('maritime_enrollment', 'referral_code_used');
CALL drop_maritime_column('maritime_enrollment', 'referral_right_granted');

-- maritime_session_fee_config
CALL drop_maritime_column('maritime_session_fee_config', 'referral_commission_amount');
CALL drop_maritime_column('maritime_session_fee_config', 'is_referral_commission_enabled');

-- maritime_fee_template
CALL drop_maritime_column('maritime_fee_template', 'referral_commission_amount');
CALL drop_maritime_column('maritime_fee_template', 'is_referral_commission_enabled');

DROP PROCEDURE IF EXISTS drop_maritime_column;

-- 6. 删除 member_user 推荐码唯一索引（存在才删）
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'member_user' AND INDEX_NAME = 'uk_referral_code'
);
SET @ddl := IF(@idx_exists > 0, 'ALTER TABLE member_user DROP INDEX uk_referral_code', 'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
