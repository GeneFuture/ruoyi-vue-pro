-- ============================================================================
-- 删除后台总菜单：商城系统 / AI 大模型 / IoT 物联网
--
-- 删除范围：
--   * 三个顶级目录及其全部子菜单、按钮权限；
--   * 对应的 system_role_menu 角色授权记录。
--
-- 执行前建议先备份 system_menu、system_role_menu。
-- 本脚本只删除菜单和授权，不删除 mall/ai/iot 的后端代码、前端页面和 API。
-- ============================================================================

CREATE TEMPORARY TABLE tmp_remove_menu_ids AS
WITH RECURSIVE menu_tree AS (
    SELECT id FROM system_menu WHERE id IN (2362, 2758, 4000)
    UNION ALL
    SELECT m.id FROM system_menu m INNER JOIN menu_tree t ON m.parent_id = t.id
)
SELECT id FROM menu_tree;

DELETE FROM system_role_menu WHERE menu_id IN (SELECT id FROM tmp_remove_menu_ids);
DELETE FROM system_menu WHERE id IN (SELECT id FROM tmp_remove_menu_ids);
DROP TEMPORARY TABLE tmp_remove_menu_ids;
