-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 13-maritime-school-library.sql
-- 学校库模块升级脚本（已在运行环境执行 08 的库使用）：
--   1. 新建 maritime_school 学校库表
--   2. 新增「学校管理」菜单及按钮权限
-- 全新环境直接执行 08 即可（已包含以上内容），无需执行本文件。
--
-- 背景：学校信息原本只能逐个班期在「班期管理 → 学校信息」面板里录入，
-- 与费用模板同一思路，这里建独立学校库，新增班期时选择学校并由服务端
-- 快照复制到 maritime_session_venue，学校库后续修改不影响已创建的班期。
-- =====================================================================

-- ========== 1. 学校库表 ==========
CREATE TABLE IF NOT EXISTS maritime_school (
  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  name                  VARCHAR(100) NOT NULL COMMENT '学校名称',
  address               VARCHAR(255) NOT NULL DEFAULT '' COMMENT '学校地址',
  latitude              DECIMAL(10,7) DEFAULT NULL COMMENT '纬度（-90 ~ 90）',
  longitude             DECIMAL(10,7) DEFAULT NULL COMMENT '经度（-180 ~ 180）',
  contact_phone         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '咨询电话',
  campus_intro          TEXT         DEFAULT NULL COMMENT '学校概况（富文本HTML）',
  classroom_intro       TEXT         DEFAULT NULL COMMENT '教学条件（富文本HTML）',
  accommodation_intro   TEXT         DEFAULT NULL COMMENT '住宿条件（富文本HTML，可空）',
  status                TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '状态（CommonStatusEnum：0开启 1关闭）',
  sort_order            INT          NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
  creator               VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater               VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted               TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id             BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  INDEX idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校库表';

-- ========== 2. 菜单 ==========
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
SELECT '学校管理', '', 2, 20, id, 'school', '', 'maritime/school/index', 0, 'MaritimeSchool'
FROM system_menu WHERE name = '海员培训' AND deleted = 0 LIMIT 1;
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校查询', 'maritime:school:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校新增', 'maritime:school:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校编辑', 'maritime:school:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校删除', 'maritime:school:delete', 3, 4, @menuId, '', '', '', 0);

-- 超级管理员角色（role_id=1）授权新菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu m
WHERE m.permission LIKE 'maritime:school:%' OR (m.component_name = 'MaritimeSchool' AND m.deleted = 0);

-- ========== 3. （可选）把已有班期的学校信息导入学校库 ==========
-- 如果之前已经在班期里录入过学校信息，可以执行下面这段把去重后的学校一次性导入，
-- 省去重新录入。同一个学校名称在不同班期里内容可能不同，这里按名称取「ID 最大的一条」。
-- 执行前建议先跑一遍 SELECT 确认结果符合预期；导入错了直接删除 maritime_school 里的数据重来即可，
-- 学校库与班期的快照互不影响。
--
-- INSERT INTO maritime_school
--   (name, address, latitude, longitude, contact_phone,
--    campus_intro, classroom_intro, accommodation_intro,
--    status, sort_order, creator, updater, deleted, tenant_id)
-- SELECT v.campus_name, v.address, v.latitude, v.longitude, v.contact_phone,
--        v.campus_intro, v.classroom_intro, v.accommodation_intro,
--        0, 0, '1', '1', 0, v.tenant_id
-- FROM maritime_session_venue v
-- WHERE v.deleted = 0
--   AND v.campus_name <> ''
--   AND v.id = (SELECT MAX(v2.id) FROM maritime_session_venue v2
--               WHERE v2.campus_name = v.campus_name AND v2.deleted = 0);
