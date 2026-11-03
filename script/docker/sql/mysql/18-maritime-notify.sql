-- ============================================================
-- 海员培训：消息通知模板 + 尾款提醒去重字段
-- 1）报名表增加最后一次尾款提醒日期
-- 2）初始化全部 MARITIME_* 站内信模板
-- 说明：模板参数名必须与 MaritimeNotifyServiceImpl 中传入的 Map key 完全一致
-- 若应用已启动过，执行本脚本后需清理 Redis 模板缓存，否则仍读取旧模板：
--   redis-cli --scan --pattern 'notify_template:*' | xargs -r redis-cli DEL
-- ============================================================

-- 1. 尾款提醒去重字段（幂等）
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'maritime_enrollment'
      AND COLUMN_NAME = 'last_balance_remind_date'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE maritime_enrollment ADD COLUMN last_balance_remind_date DATE DEFAULT NULL COMMENT ''最后一次尾款催缴提醒日期'' AFTER balance_due_date',
    'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 站内信模板（幂等：按 code 判断，已存在则跳过）
DROP TEMPORARY TABLE IF EXISTS tmp_maritime_notify_template;
CREATE TEMPORARY TABLE tmp_maritime_notify_template (
    name     VARCHAR(63)  NOT NULL,
    code     VARCHAR(64)  NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    content  VARCHAR(1024) NOT NULL,
    type     TINYINT      NOT NULL,
    params   VARCHAR(255) NOT NULL,
    status   TINYINT      NOT NULL,
    remark   VARCHAR(255) DEFAULT NULL
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO tmp_maritime_notify_template (name, code, nickname, content, type, params, status, remark) VALUES
('报名提交成功通知', 'MARITIME_ENROLL_SUCCESS', '海员培训',
 '您已提交【{sessionName}】班期报名，开班日期 {startDate}。请尽快支付{payType}锁定名额。',
 2, '["sessionName","startDate","payType"]', 0, '创建报名记录后发送'),

('定金支付成功通知', 'MARITIME_DEPOSIT_PAID', '海员培训',
 '【{sessionName}】定金 {amount} 元支付成功！请留意后续开班通知。',
 2, '["sessionName","amount"]', 0, '定金支付成功后发送'),

('全款支付成功通知', 'MARITIME_FULL_PAID', '海员培训',
 '【{sessionName}】全款 {amount} 元支付成功，报名已完成，请于 {startDate} 准时参加培训。',
 2, '["sessionName","amount","startDate"]', 0, '全款报名支付成功后发送'),

('尾款支付成功通知', 'MARITIME_BALANCE_PAID', '海员培训',
 '【{sessionName}】尾款 {amount} 元支付成功，报名已付清，请于 {startDate} 准时参加培训。',
 2, '["sessionName","amount","startDate"]', 0, '尾款支付成功后发送'),

('拼团成功通知', 'MARITIME_GROUPON_SUCCESS', '海员培训',
 '恭喜！您参加的【{sessionName}】拼团已成功，共 {memberCount} 人成团。',
 2, '["sessionName","memberCount"]', 0, '拼团人数达标或管理员强制成团后发送'),

('拼团结束通知', 'MARITIME_GROUPON_DEGRADED', '海员培训',
 '您参加的【{sessionName}】拼团已结束：{reason}。报名将继续有效，请留意后续通知。',
 2, '["sessionName","reason"]', 0, '拼团超时、成员提前付清尾款或管理员关闭时发送'),

('尾款催缴提醒', 'MARITIME_BALANCE_REMINDER', '海员培训',
 '您报名的【{sessionName}】将于 {startDate} 开班，请于 {deadline} 前支付尾款 {amount} 元。',
 2, '["sessionName","startDate","deadline","amount"]', 0, '尾款截止日前 7 天内发送，3 天内不重复提醒'),

('尾款逾期提醒', 'MARITIME_BALANCE_OVERDUE', '海员培训',
 '您【{sessionName}】班期的尾款 {amount} 元未付清（{reason}），报名已转人工处理，请尽快联系客服或补缴尾款。',
 2, '["sessionName","amount","reason"]', 0, '尾款逾期或开班未付清转人工时发送'),

('报名取消通知', 'MARITIME_ENROLL_CANCELLED', '海员培训',
 '您【{sessionName}】班期的报名已取消，原因：{reason}。',
 2, '["sessionName","reason"]', 0, '用户取消、管理员取消、订单超时或人工处理取消时发送'),

('培训完成通知', 'MARITIME_ENROLL_COMPLETED', '海员培训',
 '您【{sessionName}】班期的培训已完成，感谢您的参与！',
 2, '["sessionName"]', 0, '结班完成后发送'),

('退费申请已提交', 'MARITIME_REFUND_APPLIED', '海员培训',
 '您已提交【{sessionName}】班期的退费申请，客服将尽快处理，请留意后续通知。',
 2, '["sessionName"]', 0, '学员提交退费申请后发送'),

('退费审核通过', 'MARITIME_REFUND_APPROVED', '海员培训',
 '您【{sessionName}】班期的退费申请已通过，退款金额 {amount} 元，客服将按原支付渠道处理退款。',
 2, '["sessionName","amount"]', 0, '管理员登记退款结果后发送'),

('退费审核未通过', 'MARITIME_REFUND_REJECTED', '海员培训',
 '您【{sessionName}】班期的退费申请未通过，原因：{reason}。如有疑问请联系客服。',
 2, '["sessionName","reason"]', 0, '管理员拒绝退费申请后发送');

INSERT INTO system_notify_template
    (name, code, nickname, content, type, params, status, remark, creator, updater)
SELECT t.name, t.code, t.nickname, t.content, t.type, t.params, t.status, t.remark, '1', '1'
FROM tmp_maritime_notify_template t
WHERE NOT EXISTS (
    SELECT 1 FROM system_notify_template nt
    WHERE nt.code = t.code AND nt.deleted = 0
);

DROP TEMPORARY TABLE tmp_maritime_notify_template;
