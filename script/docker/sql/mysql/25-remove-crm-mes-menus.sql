-- ============================================================================
-- 删除后台总菜单：CRM 系统 / MES 系统
--
-- 删除范围：两个顶级目录及全部子菜单、按钮权限、system_role_menu 授权。
-- 本脚本只删除菜单和授权，不删除数据库业务表。
-- ============================================================================

CREATE TEMPORARY TABLE tmp_remove_menu_ids AS
WITH RECURSIVE menu_tree AS (
    SELECT id FROM system_menu WHERE id IN (2397, 5100)
    UNION ALL
    SELECT m.id FROM system_menu m INNER JOIN menu_tree t ON m.parent_id = t.id
)
SELECT id FROM menu_tree;

DELETE FROM system_role_menu WHERE menu_id IN (SELECT id FROM tmp_remove_menu_ids);
DELETE FROM system_menu WHERE id IN (SELECT id FROM tmp_remove_menu_ids);
DROP TEMPORARY TABLE tmp_remove_menu_ids;
