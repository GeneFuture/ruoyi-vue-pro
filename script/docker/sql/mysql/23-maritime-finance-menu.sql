-- ============================================================================
-- 海员培训：新增「资金流水」菜单
--
-- 页面：/maritime/finance
-- 权限：maritime:finance:view
-- 功能：每日资金流入/退款流出/净流入 + 日期范围汇总
-- ============================================================================

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('资金流水', '', 2, 6, 5986, 'finance', '', 'maritime/finance/index', 0, 'MaritimeFinance');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('资金流水查询', 'maritime:finance:view', 3, 1, @menuId, '', '', '', 0);
