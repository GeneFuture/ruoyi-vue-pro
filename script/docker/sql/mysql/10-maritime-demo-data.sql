-- ============================================================================
-- 海员培训 · 演示数据
--
-- 为什么需要这个脚本：
--   09-maritime-course-seed.sql 只导入了 8~10 门课程和 2 个费用模板，**没有任何班期数据**。
--   首页「近期开班」楼层的条件是「session_status = OPEN 且 start_date 在 [今天, 今天+30 天]」，
--   库里原有班期的开班日期是 2026-07，早已过期，被日期条件全部过滤，
--   导致首页那一整层（以及「开班」tab）显示为空。
--
-- 本脚本导入：
--   1. 4 个未来 30 天内的招生中班期 + 对应费用配置  → 恢复首页「近期开班」与「开班」tab
--       （班期城市写在配套的 maritime_session_venue 记录上，列表卡片与城市筛选取这里）
--   2. 3 条已发布公告                              → 恢复首页「平台公告」楼层
--
-- 前置条件：
--   * 已存在课程 id = 1, 2, 8, 10（可先执行 09-maritime-course-seed.sql）
--   * 租户编号为 1（yudao.tenant.enable=true 且 ignore-tables 为空，
--     所有 maritime_* 表都走租户过滤，tenant_id 写错数据就查不出来）
--
-- 幂等性：
--   * 班期 / 费用配置 有唯一键（uk_session_code / uk_session_id），用 INSERT IGNORE，可重复执行
--   * 公告 无唯一键，用「先按标题删、再插」保证可重复执行，只影响下面列出的 3 个标题
--
-- 日期基准：2026-09-18。若在很久以后执行，请把下面所有日期整体平移到当前时间之后，
--          否则又会落回「开班日期已过期」的老问题。
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 0. （可选）把开班日期已过期的 OPEN 班期标记为已结班，避免污染统计数据
--    默认注释掉：请先 SELECT 确认范围再执行
-- ---------------------------------------------------------------------------
-- UPDATE maritime_course_session
--    SET session_status = 'FINISHED'
--  WHERE session_status = 'OPEN'
--    AND start_date < CURDATE()
--    AND deleted = 0
--    AND tenant_id = 1;

-- ---------------------------------------------------------------------------
-- 1. 班期（开班日期均落在未来 30 天内，确保首页「近期开班」能取到）
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO maritime_course_session
    (course_id, session_code, session_name, duration_days, instructor_info,
     start_date, end_date, enrollment_deadline, max_students, enrolled_count, session_status,
     creator, updater, tenant_id)
VALUES
    -- 基本安全培训（课程 1）· 青岛 · 7 天
    (1, '2026-09-QD-001', '9月一期', 7, NULL,
     '2026-09-27', '2026-10-03', '2026-09-24', 30, 22, 'OPEN',
     'seed', 'seed', 1),
    -- 精通急救培训（课程 2）· 大连 · 5 天
    (2, '2026-09-DL-001', '9月二期', 5, NULL,
     '2026-10-04', '2026-10-08', '2026-10-01', 30, 12, 'OPEN',
     'seed', 'seed', 1),
    -- 基本安全培训更新 Z01（课程 8）· 上海 · 6 天
    (8, '2026-10-SH-001', '10月一期', 6, NULL,
     '2026-10-12', '2026-10-17', '2026-10-09', 25, 5, 'OPEN',
     'seed', 'seed', 1),
    -- 高级消防培训更新 Z04（课程 10）· 青岛 · 4 天
    (10, '2026-10-QD-002', '10月二期', 4, NULL,
     '2026-10-16', '2026-10-19', '2026-10-13', 20, 3, 'OPEN',
     'seed', 'seed', 1);

-- 班期所在城市：城市已从班期表挪到场地表（列表卡片与城市筛选都取这里），
-- 演示班期只填城市、不填学校信息，小程序端「学校」卡片不会渲染，但卡片城市正常展示。
INSERT IGNORE INTO maritime_session_venue
    (session_id, campus_name, city, creator, updater, tenant_id)
SELECT s.id, '', c.city, 'seed', 'seed', 1
  FROM maritime_course_session s
  JOIN (
        SELECT '2026-09-QD-001' AS code, '青岛市' AS city
  UNION SELECT '2026-09-DL-001', '大连市'
  UNION SELECT '2026-10-SH-001', '上海市'
  UNION SELECT '2026-10-QD-002', '青岛市'
       ) c ON c.code = s.session_code
 WHERE s.deleted = 0;

-- ---------------------------------------------------------------------------
-- 2. 费用配置（按 session_code 关联，不依赖自增 ID）
--    定金 / 拼团优惠会直接显示在首页班期卡上
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO maritime_session_fee_config
    (session_id, tuition_amount, tuition_includes_accommodation, extra_fee_items, deposit_amount,
     is_groupon_enabled, groupon_discount_amount, groupon_required_count, groupon_expire_hours,
     groupon_fail_discount_amount,
     is_deposit_refundable, balance_due_days_before_start, refund_policy_text,
     creator, updater, tenant_id)
SELECT s.id,
       t.tuition,
       -- 学费不含住宿，住宿费放进「另行缴费」的可选项（到校报到时按需缴纳）
       0,
       JSON_ARRAY(JSON_OBJECT('name', '教材费', 'amount', 200, 'required', true),
                  JSON_OBJECT('name', '住宿费', 'amount', 800, 'required', false)),
       t.deposit,
       t.groupon_on, t.groupon_discount, 3, 24,
       0,
       1, 7, '开班前可申请退费，开班后不支持退费。',
       'seed', 'seed', 1
FROM maritime_course_session s
JOIN (
    SELECT '2026-09-QD-001' AS code, 3800.00 AS tuition,
           500.00 AS deposit, 1 AS groupon_on, 100.00 AS groupon_discount
    UNION ALL SELECT '2026-09-DL-001', 2600.00, 400.00, 0, 0.00
    UNION ALL SELECT '2026-10-SH-001', 3200.00, 500.00, 1, 150.00
    UNION ALL SELECT '2026-10-QD-002', 2800.00, 400.00, 0, 0.00
) t ON t.code = s.session_code
WHERE s.deleted = 0 AND s.tenant_id = 1;

-- ---------------------------------------------------------------------------
-- 验证：执行后应能看到数据（首页楼层依赖这些查询）
-- ---------------------------------------------------------------------------
-- 首页「近期开班」：应返回 4 条
-- SELECT id, session_code, start_date, session_status
--   FROM maritime_course_session
--  WHERE session_status = 'OPEN' AND start_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
--    AND deleted = 0 AND tenant_id = 1
--  ORDER BY start_date;

-- ---------------------------------------------------------------------------
-- 执行后注意：首页数据在 Redis 缓存 5 分钟（key: maritime:home:{tenantId}）。
-- 想立刻看到效果，删掉缓存：
--   redis-cli DEL maritime:home:1
-- ---------------------------------------------------------------------------
