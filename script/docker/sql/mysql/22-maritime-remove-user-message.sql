-- ============================================================================
-- 海员培训：下线「站内消息管理」模块
--
-- 原因：
--   * maritime_user_message 无任何业务写入，也无小程序读取接口；
--   * 小程序消息中心实际使用系统模块的 system_notify_message；
--   * 后台真实消息入口为：系统管理 → 消息中心 → 消息记录。
--
-- 执行前请确认 maritime_user_message 无需要保留的数据（当前为 0 条）。
-- ============================================================================

DELETE FROM system_role_menu WHERE menu_id BETWEEN 6059 AND 6064;
DELETE FROM system_menu WHERE id BETWEEN 6060 AND 6064;
DELETE FROM system_menu WHERE id = 6059;

DROP TABLE IF EXISTS maritime_user_message;
