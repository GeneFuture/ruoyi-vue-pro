-- ----------------------------
-- 海员培训平台 - 课程种子数据（8 类课程）
-- 适任类：值班水手、值班机工、三副、三管轮、GMDSS 通用操作员
-- 更新类：基本安全更新（Z01）、精通艇筏更新（Z02）、高级消防更新（Z04）
-- 内容依据：《中华人民共和国海船船员适任考试和发证规则》（2020 规则）、
--           《海船船员培训大纲（2021 版）》（交办海〔2021〕49 号）
-- 幂等性：按课程名称判重（WHERE NOT EXISTS），可重复执行，不覆盖已有课程
-- 说明：封面图片为官方演示 CDN 占位图，正式运营前请通过管理后台替换为真实课程封面
-- ----------------------------

-- 1. 值班水手适任培训
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '值班水手适任培训', '适任培训',
 'https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg',
 '<h3>课程简介</h3><p>值班水手是甲板部基础值班岗位，承担操舵、航行值班、系离泊作业与船舶保养等职责，是进入远洋甲板职业的起点。本课程依据《海船船员培训大纲（2021 版）》设置，理论结合实操，帮助学员系统掌握甲板值班核心技能，一次性通过海事局适任考试与评估。</p><h3>培训内容</h3><ul><li>船舶基础知识与船舶避碰基本知识</li><li>航海基础知识与航行值班规范</li><li>锚泊作业、系泊操作与值班</li><li>货物作业辅助与船舶日常保养</li><li>船艺技能（绳结、撇缆、甲板工艺）</li><li>海洋环境保护基本知识</li><li>水手英语听力与会话</li></ul><h3>考核方式</h3><p>理论考试 + 实操评估（船艺评估、水手英语听力与会话评估）。</p><h3>发展方向</h3><p>取得值班水手适任证书后，可上船任职积累海上资历，满足条件后可晋升三副等高级船员岗位。</p>',
 '1. 年满 16 周岁，符合海船船员健康检查标准（持有有效体检证明）；\n2. 已完成基本安全培训（Z01）及保安意识、负有指定保安职责船员培训；\n3. 学历与资历要求按航区（远洋/沿海等）执行《适任考试和发证规则》相关规定；\n4. 具体材料以现场审核为准。',
 1, 84, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '值班水手适任培训' AND deleted = 0 LIMIT 1);

-- 2. 值班机工适任培训
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '值班机工适任培训', '适任培训',
 'https://static.iocoder.cn/mall/099f261f18576902c4a18209d062a3fd.jpg',
 '<h3>课程简介</h3><p>值班机工是轮机部基础值班岗位，负责机舱值班、主辅机巡检与设备保养，是机舱职业发展的起点。本课程按《海船船员培训大纲（2021 版）》要求组织教学，覆盖 750 千瓦及以上船舶值班机工全部适任能力要求，助力学员顺利通过考试评估。</p><h3>培训内容</h3><ul><li>机舱日常值班与锅炉值班操作</li><li>主机运行管理与应急处理</li><li>辅机运行管理（泵、分油机等）</li><li>应急设备操作与应急程序应用</li><li>船舶电气与自动控制基础知识</li><li>防止海洋污染法规与防污设备操作</li><li>机工英语听力与会话</li></ul><h3>考核方式</h3><p>理论考试 + 实操评估（金工工艺、设备拆装与操作、机工英语听力与会话）。</p><h3>发展方向</h3><p>取得值班机工适任证书后可上船任职，积累机舱资历后可向三管轮等轮机员岗位晋升。</p>',
 '1. 年满 16 周岁，符合海船船员健康检查标准（持有有效体检证明）；\n2. 已完成基本安全培训（Z01）及保安意识、负有指定保安职责船员培训；\n3. 持有三管轮及以上适任证书者可免于值班机工岗位适任培训；\n4. 具体材料以现场审核为准。',
 1, 82, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '值班机工适任培训' AND deleted = 0 LIMIT 1);

-- 3. 三副适任培训
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '三副适任培训', '适任培训',
 'https://static.iocoder.cn/mall/1251259c8968cdce993e47dd121b50a7.jpg',
 '<h3>课程简介</h3><p>三副是甲板部值班驾驶员，负责航行值班、救生与消防设备管理，是从普通船员迈向高级船员的关键岗位，也是晋升二副、大副直至船长的必经阶梯。本课程面向三副适任考试开设，配备资深船长与航海院校师资，系统覆盖全部考试科目。</p><h3>考试科目（理论 7 门）</h3><ul><li>航海学</li><li>船舶值班、操纵与避碰</li><li>航海气象学与海洋学</li><li>海上货物运输</li><li>船舶结构与设备</li><li>船舶管理</li><li>航海英语</li></ul><h3>证书等级</h3><p>一等适任证书适用于 500 总吨及以上船舶；二等适用于未满 500 总吨船舶。</p><h3>取证流程</h3><p>培训结业 → 通过理论考试 → 完成船上见习 → 实操评估合格 → 向海事管理机构申请适任证书。</p>',
 '1. 符合《海船船员适任考试和发证规则》规定的学历与海上服务资历要求；\n2. 已完成基本安全培训及相应合格证培训并持有效证书；\n3. 航海类专业毕业生或完成规定岗位适任培训者可报名；\n4. 具体资历核算与材料清单以现场审核为准。',
 1, 88, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '三副适任培训' AND deleted = 0 LIMIT 1);

-- 4. 三管轮适任培训
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '三管轮适任培训', '适任培训',
 'https://static.iocoder.cn/mall/7f46e98ece1920e1a4082dfc72bcd72a.jpg',
 '<h3>课程简介</h3><p>三管轮是轮机部值班轮机员，负责机舱值班、辅机与电气设备管理维护，是轮机职业晋升二管轮、轮机长的起点。本课程紧扣三管轮适任考试大纲，理论教学与机舱实操并重，帮助学员高效备考、一次通过。</p><h3>考试科目（理论 5 门）</h3><ul><li>主推进动力装置</li><li>船舶辅机</li><li>船舶电气与自动化</li><li>船舶管理</li><li>轮机英语</li></ul><h3>证书等级</h3><p>一等适任证书适用于 750 千瓦及以上船舶；二等适用于未满 750 千瓦船舶。</p><h3>取证流程</h3><p>培训结业 → 通过理论考试 → 完成船上见习 → 实操评估合格 → 向海事管理机构申请适任证书。</p>',
 '1. 符合《海船船员适任考试和发证规则》规定的学历与海上服务资历要求；\n2. 已完成基本安全培训及相应合格证培训并持有效证书；\n3. 轮机类专业毕业生或完成规定岗位适任培训者可报名；\n4. 具体资历核算与材料清单以现场审核为准。',
 1, 86, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '三管轮适任培训' AND deleted = 0 LIMIT 1);

-- 5. GMDSS 通用操作员培训
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT 'GMDSS 通用操作员培训', '适任培训',
 'https://static.iocoder.cn/mall/bfb275263d02ab081b8670a752f2823f.jpg',
 '<h3>课程简介</h3><p>GMDSS（全球海上遇险与安全系统）通用操作员证书（GOC）是船舶通信岗位的必备适任证书，驾驶员在船操作 GMDSS 设备必须持证。课程依托 GMDSS 模拟机房开展，还原真实海上通信场景，系统训练遇险、紧急与安全通信的规范操作。</p><h3>培训内容</h3><ul><li>海上无线电通信基础与国际通信规则程序</li><li>GMDSS 设备操作（VHF/MF-HF 电台、DSC、NAVTEX、卫星通信等）</li><li>EPIRB、SART 等遇险报警与搜救定位设备使用</li><li>遇险、紧急和安全信息的发送与接收</li><li>GMDSS 模拟器综合训练</li></ul><h3>适合人群</h3><p>驾驶专业学员、在职驾驶员及需要取得 GMDSS 通用操作员适任证书的船员。</p><h3>证书说明</h3><p>通过考试评估后取得 GMDSS 通用操作员适任证书，证书再有效按海事局规定执行。</p>',
 '1. 完成航海类相关专业规定学历教育或相应职业培训；\n2. 符合海船船员健康检查标准；\n3. 已持有基本安全培训等相应合格证；\n4. 具体报名条件以现场审核为准。',
 1, 80, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = 'GMDSS 通用操作员培训' AND deleted = 0 LIMIT 1);

-- 6. 基本安全培训更新（Z01 再有效）
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '基本安全培训更新（Z01）', 'STCW更新',
 'https://static.iocoder.cn/mall/e499a4dc489cda7b8d472cb3a3b348b7.jpg',
 '<h3>课程简介</h3><p>基本安全培训合格证（Z01）是全体海员必备的 STCW 强制培训证书，有效期 5 年。证书到期前须完成知识更新培训并考核合格，方可办理再有效。本课程面向持证船员开设，聚焦新规变化与关键技能复训，课时紧凑、通过率高。</p><h3>更新内容</h3><ul><li>个人求生技能（救生设备使用、水中求生与直升机救援配合）</li><li>船上防火与灭火（火灾预防、灭火器材与探火程序）</li><li>基本急救（现场急救原则与常用急救技术）</li><li>个人安全与社会责任（安全作业、防污染与应急职责）</li><li>STCW 公约及国内法规最新要求解读</li></ul><h3>温馨提示</h3><p>请在证书有效期届满前参加培训并办理再有效，避免证书失效影响上船任职。</p>',
 '1. 持有有效期内的基本安全培训合格证（Z01），或证书在过渡期内符合再有效申请条件；\n2. 符合海船船员健康检查标准；\n3. 报名时请携带身份证与现有合格证；\n4. 具体以现场审核为准。',
 1, 70, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '基本安全培训更新（Z01）' AND deleted = 0 LIMIT 1);

-- 7. 精通艇筏和救助艇培训更新（Z02 再有效）
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '精通艇筏和救助艇培训更新（Z02）', 'STCW更新',
 'https://static.iocoder.cn/mall/06fa150a47163e973bb4806a380afaab.jpg',
 '<h3>课程简介</h3><p>精通救生艇筏和救助艇培训合格证（Z02）有效期 5 年，是承担艇筏操作职责船员的必备证书。证书到期前须完成更新培训并通过考核方可再有效。课程以实操为核心，帮助学员巩固艇筏操纵关键技能，满足持续适任要求。</p><h3>更新内容</h3><ul><li>救生艇、救生筏的降落、操纵与回收</li><li>救助艇的操纵与回收操作</li><li>艇筏属具与求救信号设备使用</li><li>海上求生要点与艇筏内管理</li><li>相关公约法规的最新要求</li></ul><h3>温馨提示</h3><p>培训包含水上实操环节，请按要求着装并遵守训练安全规定。</p>',
 '1. 持有精通救生艇筏和救助艇培训合格证（Z02），且在再有效申请期限内；\n2. 符合海船船员健康检查标准；\n3. 报名时请携带身份证与现有合格证；\n4. 具体以现场审核为准。',
 1, 65, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '精通艇筏和救助艇培训更新（Z02）' AND deleted = 0 LIMIT 1);

-- 8. 高级消防培训更新（Z04 再有效）
INSERT INTO maritime_course
  (name, certificate_type, cover_image, description, enrollment_condition_text, status, sort_order, creator, updater, deleted, tenant_id)
SELECT '高级消防培训更新（Z04）', 'STCW更新',
 'https://static.iocoder.cn/mall/banner-01.jpg',
 '<h3>课程简介</h3><p>高级消防培训合格证（Z04）有效期 5 年，是驾驶员、轮机员及负有消防指挥职责船员的必备证书。证书到期前须完成知识更新培训并考核合格方可再有效。课程结合典型船舶火灾案例，强化火场指挥与团队协作能力，贴合实际灭火场景。</p><h3>更新内容</h3><ul><li>船舶消防组织与火场指挥程序</li><li>灭火战术与协同作战训练</li><li>灭火剂特性、选用与安全注意事项</li><li>探火、封舱与通风控制要点</li><li>典型船舶火灾案例分析与法规更新解读</li></ul><h3>温馨提示</h3><p>培训含模拟灭火实操，请穿着适合训练的服装并遵守安全操作规程。</p>',
 '1. 持有高级消防培训合格证（Z04），且在再有效申请期限内；\n2. 符合海船船员健康检查标准；\n3. 报名时请携带身份证与现有合格证；\n4. 具体以现场审核为准。',
 1, 60, '1', '1', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM maritime_course WHERE name = '高级消防培训更新（Z04）' AND deleted = 0 LIMIT 1);

-- ========== 费用模板（独立定价方案，新增班期时快照引用） ==========
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
