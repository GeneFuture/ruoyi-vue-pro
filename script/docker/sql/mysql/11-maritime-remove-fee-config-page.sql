-- ----------------------------
-- 移除「班期费用配置」独立页面菜单（2026-08-25）
--
-- 背景：费用配置编辑已并入班期编辑表单（创建班期时由费用模板快照生成，
-- 编辑班期时内嵌费用面板修改），独立管理页面及其前端文件已删除。
--
-- 适用范围：已执行过 08-maritime-init.sql 的存量库。
-- 新装库无需执行（08 脚本已移除对应 INSERT）。
-- 前置条件：后端已升级（/save、/get-by-session 改用 maritime:course-session 权限）。
-- ----------------------------

-- 1. 清理角色-菜单关联（页面菜单 + 全部按钮权限）
DELETE FROM system_role_menu
WHERE menu_id IN (
    SELECT id FROM system_menu
    WHERE (path = 'session-fee-config' AND component = 'maritime/sessionFeeConfig/index')
       OR permission LIKE 'maritime:session-fee-config:%'
);

-- 2. 删除菜单（页面 + 查询/新增/编辑/删除/导出按钮）
DELETE FROM system_menu
WHERE (path = 'session-fee-config' AND component = 'maritime/sessionFeeConfig/index')
   OR permission LIKE 'maritime:session-fee-config:%';
