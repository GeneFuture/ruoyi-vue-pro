-- ============================================================
-- 海员培训：报名状态机 + 人工处理单
-- 1）拼团增加结束原因
-- 2）新增通用人工处理单
-- 3）报名状态注释更新为：PENDING_DEPOSIT/DEPOSITED/PAID_OFF/MANUAL_PROCESSING/COMPLETED/CANCELLED
-- ============================================================

-- 1. 拼团结束原因
ALTER TABLE maritime_groupon_record
    ADD COLUMN degrade_reason VARCHAR(200) DEFAULT NULL COMMENT '拼团结束原因（DEGRADED 时填写）' AFTER success_time;

-- 2. 通用人工处理单
CREATE TABLE IF NOT EXISTS maritime_manual_task (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '人工处理单ID',
    task_no         VARCHAR(32)  NOT NULL                COMMENT '人工处理单号',
    enrollment_id   BIGINT       NOT NULL                COMMENT '报名ID',
    member_id       BIGINT       NOT NULL                COMMENT '会员ID',
    order_id        BIGINT       DEFAULT NULL            COMMENT '关联支付订单ID',
    source_type     VARCHAR(20)  NOT NULL                COMMENT '来源类型（REFUND/BALANCE/GROUPON/OTHER）',
    source_id       BIGINT       DEFAULT NULL            COMMENT '来源记录ID',
    task_type       VARCHAR(30)  NOT NULL                COMMENT '任务类型（BALANCE_OVERDUE/REFUND_APPLY/REFUND_AMOUNT_CONFIRM/OTHER）',
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING/PROCESSING/DONE/CANCELLED）',
    priority        TINYINT      NOT NULL DEFAULT 0      COMMENT '优先级（越大越优先）',
    reason          VARCHAR(500) NOT NULL                COMMENT '进入人工的原因',
    handler_id      BIGINT       DEFAULT NULL            COMMENT '处理人用户ID',
    handle_result   VARCHAR(500) DEFAULT NULL            COMMENT '处理结果',
    handle_time     DATETIME     DEFAULT NULL            COMMENT '处理时间',
    creator         VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '创建者',
    updater         VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '更新者',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除（0正常 1删除）',
    tenant_id       BIGINT       NOT NULL DEFAULT 0      COMMENT '租户ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_no (task_no, deleted),
    KEY idx_enrollment_id (enrollment_id),
    KEY idx_member_id (member_id),
    KEY idx_status (status),
    KEY idx_task_type (task_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人工处理单';

-- 3. 报名状态注释
ALTER TABLE maritime_enrollment
    MODIFY COLUMN enrollment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_DEPOSIT'
        COMMENT '报名状态(PENDING_DEPOSIT/DEPOSITED/PAID_OFF/MANUAL_PROCESSING/COMPLETED/CANCELLED)';

-- 4. 人工处理菜单
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
SELECT '人工处理', '', 2, 6, id, 'manual-task', '', 'maritime/manualTask/index', 0, 'MaritimeManualTask'
FROM system_menu WHERE name = '海员培训' AND deleted = 0 LIMIT 1;
SET @manualMenuId := LAST_INSERT_ID();

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('人工处理查询', 'maritime:manual-task:query', 3, 1, @manualMenuId, '', '', '', 0),
       ('人工处理更新', 'maritime:manual-task:update', 3, 2, @manualMenuId, '', '', '', 0);

-- 超级管理员角色（role_id=1）授权新菜单
INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted, tenant_id)
SELECT 1, m.id, '1', NOW(), '1', NOW(), 0, 1
FROM system_menu m
WHERE m.permission LIKE 'maritime:manual-task:%'
   OR (m.component_name = 'MaritimeManualTask' AND m.deleted = 0);

-- 5. 定时任务（执行后到后台「基础设施 → 定时任务」点一次「同步」）
INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT '报名订单过期关闭', 1, 'orderExpireJob', '', '0 */5 * * * ?', 0, 0, 0, '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'orderExpireJob' AND deleted = 0);

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT '拼团超时降级', 1, 'grouponExpireJob', '', '0 */5 * * * ?', 0, 0, 0, '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'grouponExpireJob' AND deleted = 0);

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT '尾款催缴提醒', 1, 'balanceReminderJob', '', '0 0 10 * * ?', 0, 0, 0, '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'balanceReminderJob' AND deleted = 0);

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT '尾款逾期转人工', 1, 'balanceOverdueJob', '', '0 0 9 * * ?', 0, 0, 0, '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'balanceOverdueJob' AND deleted = 0);

INSERT INTO infra_job (name, status, handler_name, handler_param, cron_expression, retry_count, retry_interval, monitor_timeout, creator, updater)
SELECT '结班完成报名', 1, 'enrollmentCompleteJob', '', '0 30 1 * * ?', 0, 0, 0, '1', '1'
WHERE NOT EXISTS (SELECT 1 FROM infra_job WHERE handler_name = 'enrollmentCompleteJob' AND deleted = 0);

-- 支付通知重试任务当前是暂停状态，改为开启；同步后才会真正恢复 Quartz 触发器
UPDATE infra_job SET status = 1 WHERE handler_name = 'payNotifyJob' AND deleted = 0;

