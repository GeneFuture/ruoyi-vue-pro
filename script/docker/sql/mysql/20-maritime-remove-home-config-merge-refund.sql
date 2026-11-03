-- ============================================================================
-- 海员培训：下线首页配置功能 + 退费审核合并到人工处理
--
-- 执行前请确认：
--   1. 已备份 system_menu、system_role_menu；
--   2. 当前库的「海员培训」父菜单 ID 为 5986（与 08-maritime-init.sql 一致）。
--
-- 说明：
--   * 退费审核已合并到「人工处理」菜单，使用 maritime:manual-task:* 权限；
--   * 首页配置（金刚区/Banner）已从后端和小程序移除，不再需要独立菜单。
-- ============================================================================

-- 1. 删除独立的退费申请管理菜单及其按钮权限
DELETE FROM system_role_menu WHERE menu_id BETWEEN 6041 AND 6046;
DELETE FROM system_menu WHERE id BETWEEN 6042 AND 6046;
DELETE FROM system_menu WHERE id = 6041;

-- 2. 删除首页配置菜单（含历史重复菜单 6092 / 6097）及其按钮权限
DELETE FROM system_role_menu WHERE menu_id BETWEEN 6092 AND 6101;
DELETE FROM system_menu WHERE id BETWEEN 6093 AND 6101;
DELETE FROM system_menu WHERE id IN (6092, 6097);

-- 3. 可选：确认首页配置数据不再需要后，再手动删除配置表
-- DROP TABLE IF EXISTS maritime_home_config;
