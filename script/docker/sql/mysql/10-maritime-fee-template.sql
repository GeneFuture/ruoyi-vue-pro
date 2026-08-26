-- ⚠️ 执行前确保客户端字符集为 utf8mb4（mysql --default-character-set=utf8mb4），否则中文写入为乱码
SET NAMES utf8mb4;

-- =====================================================================
-- 10-maritime-fee-template.sql
-- 费用模板模块升级脚本（已在运行环境执行 08/09 的库使用）：
--   1. 新建 maritime_fee_template 表
--   2. 新增「费用模板」菜单及按钮权限
--   3. 初始化两个示例模板
-- 全新环境直接执行 08 + 09 即可（已包含以上内容），无需执行本文件。
-- =====================================================================

-- ========== 1. 费用模板表 ==========
CREATE TABLE IF NOT EXISTS maritime_fee_template (
  id                            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  name                          VARCHAR(50)    NOT NULL COMMENT '模板名称（如：STCW 标准收费）',
  tuition_amount                DECIMAL(10,2)  NOT NULL COMMENT '学费总额（元）',
  tuition_description           JSON           DEFAULT NULL COMMENT '学费说明（{"理论课":"1000","实操":"800"}）',
  deposit_amount                DECIMAL(10,2)  NOT NULL COMMENT '定金金额（元）',
  is_groupon_enabled            TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否开启拼团',
  groupon_discount_amount       DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团优惠减免金额（元）',
  groupon_required_count        INT            NOT NULL DEFAULT 3 COMMENT '拼团所需人数',
  groupon_expire_hours          INT            NOT NULL DEFAULT 24 COMMENT '拼团有效时间（小时）',
  groupon_fail_discount_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团失败降级优惠金额（元，单人可享）',
  referral_commission_amount    DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '推荐佣金金额（元，每成功推荐一人）',
  is_referral_commission_enabled TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否开启推荐佣金',
  is_deposit_refundable          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '定金是否可退',
  balance_due_days_before_start  INT          NOT NULL DEFAULT 7  COMMENT '尾款截止：开班前N天',
  refund_policy_text            TEXT           DEFAULT NULL COMMENT '退款政策说明文本',
  status                        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '状态（CommonStatusEnum：0启用 1停用）',
  creator                       VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
  updater                       VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
  create_time                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted                       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id                     BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='费用模板表';

-- ========== 2. 菜单 ==========
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
SELECT '费用模板', '', 2, 19, id, 'fee-template', '', 'maritime/feeTemplate/index', 0, 'FeeTemplate'
FROM system_menu WHERE name = '海员培训' AND deleted = 0 LIMIT 1;
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板查询', 'maritime:fee-template:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板新增', 'maritime:fee-template:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板编辑', 'maritime:fee-template:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板删除', 'maritime:fee-template:delete', 3, 4, @menuId, '', '', '', 0);

-- 超级管理员角色（role_id=1）授权新菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu m
WHERE m.permission LIKE 'maritime:fee-template:%' OR (m.component_name = 'FeeTemplate' AND m.deleted = 0);

-- ========== 3. 示例模板 ==========
INSERT INTO maritime_fee_template
  (name, tuition_amount, tuition_description, deposit_amount,
   is_groupon_enabled, groupon_discount_amount, groupon_required_count, groupon_expire_hours, groupon_fail_discount_amount,
   referral_commission_amount, is_referral_commission_enabled, is_deposit_refundable, balance_due_days_before_start,
   refund_policy_text, status, creator, updater, deleted, tenant_id)
SELECT 'STCW 标准收费', 1800.00, JSON_OBJECT('实操', '800', '理论课', '1000'), 300.00,
 0, 0.00, 3, 24, 0.00,
 50.00, 1, 1, 7,
 '开班前 7 天可全额退款；开班后按已上课时比例扣除。', 0, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_fee_template WHERE name = 'STCW 标准收费' AND deleted = 0 LIMIT 1);

INSERT INTO maritime_fee_template
  (name, tuition_amount, tuition_description, deposit_amount,
   is_groupon_enabled, groupon_discount_amount, groupon_required_count, groupon_expire_hours, groupon_fail_discount_amount,
   referral_commission_amount, is_referral_commission_enabled, is_deposit_refundable, balance_due_days_before_start,
   refund_policy_text, status, creator, updater, deleted, tenant_id)
SELECT '拼团优惠收费', 1800.00, JSON_OBJECT('实操', '800', '理论课', '1000'), 300.00,
 1, 100.00, 3, 24, 50.00,
 50.00, 1, 1, 7,
 '开班前 7 天可全额退款；拼团失败自动降级享受单人优惠。', 0, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_fee_template WHERE name = '拼团优惠收费' AND deleted = 0 LIMIT 1);
