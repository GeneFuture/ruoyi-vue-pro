-- ============================================================================
-- 海员培训：报名相关菜单改名并调整为同级顺序
--
-- 目标顺序：
--   报名记录 → 支付订单 → 人工处理 → 拼团记录 → 拼团成员 → 站内消息
--   → 课程管理 → 费用模板 → 班期管理 → 学校管理
-- ============================================================================

UPDATE system_menu SET name = '报名记录', sort = 0 WHERE id = 6017;
UPDATE system_menu SET name = '支付订单', sort = 1 WHERE id = 6011;
UPDATE system_menu SET name = '人工处理', sort = 2 WHERE id = 6107;

UPDATE system_menu SET sort = 3 WHERE id = 6029;
UPDATE system_menu SET sort = 4 WHERE id = 6023;
UPDATE system_menu SET sort = 5 WHERE id = 6059;

-- 支付订单的按钮权限名称同步调整（权限标识不变）
UPDATE system_menu SET name = '支付订单查询' WHERE id = 6012;
UPDATE system_menu SET name = '支付订单创建' WHERE id = 6013;
UPDATE system_menu SET name = '支付订单更新' WHERE id = 6014;
UPDATE system_menu SET name = '支付订单删除' WHERE id = 6015;
UPDATE system_menu SET name = '支付订单导出' WHERE id = 6016;
