-- ============================================================
-- 海员培训平台 maritime 模块初始化 SQL
-- 对应任务：T01-数据库与模块骨架.md
-- 注意：CREATE TABLE 语句幂等（IF NOT EXISTS）；ALTER TABLE 不幂等，仅适用于全新数据库（clean.sh 场景）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 1. 扩展 member_user 表 ==========
-- 注意：MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，运行前确认列不存在
ALTER TABLE member_user
  ADD COLUMN open_id            VARCHAR(64)  DEFAULT NULL COMMENT '微信 openid';

ALTER TABLE member_user ADD INDEX idx_open_id (open_id);

-- ========== 1.1 学员信息表（报名常用信息：姓名 / 身份证 / 手机号） ==========
CREATE TABLE IF NOT EXISTS maritime_student_info (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学员信息ID',
  member_id   BIGINT       NOT NULL COMMENT '会员ID',
  real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  id_card     VARCHAR(64)  NOT NULL COMMENT '身份证号（加密存储）',
  phone       VARCHAR(20)  NOT NULL COMMENT '手机号',
  creator     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员信息表';

-- ========== 2. 课程模板表 ==========
CREATE TABLE IF NOT EXISTS maritime_course (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  name            VARCHAR(100) NOT NULL COMMENT '课程名称',
  certificate_type VARCHAR(50) NOT NULL COMMENT '证书类型（如：STCW、精通急救）',
  cover_image     VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
  description     TEXT         DEFAULT NULL COMMENT '课程详情（富文本HTML）',
  enrollment_condition_text TEXT DEFAULT NULL COMMENT '报名条件说明文本（仅前端展示，线下验证）',
  status          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态（0下架 1上架）',
  sort_order      INT          NOT NULL DEFAULT 0 COMMENT '排序权重（越大越靠前）',
  creator         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  INDEX idx_certificate_type (certificate_type),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程模板表';

-- ========== 3. 班期表 ==========
CREATE TABLE IF NOT EXISTS maritime_course_session (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '班期ID',
  course_id       BIGINT       NOT NULL COMMENT '课程ID',
  session_code    VARCHAR(30)  NOT NULL COMMENT '班期编号（系统自动生成，如：C1-202610-001）',
  session_name    VARCHAR(50)  DEFAULT NULL COMMENT '班级名称（可选，如：A班）',
  duration_days   INT          NOT NULL COMMENT '本期课程天数',
  instructor_info VARCHAR(500) DEFAULT NULL COMMENT '本期讲师信息',
  start_date      DATE         NOT NULL COMMENT '开班日期',
  end_date        DATE         NOT NULL COMMENT '结束日期',
  enrollment_deadline DATE     DEFAULT NULL COMMENT '报名截止日期',
  max_students    INT          NOT NULL DEFAULT 30 COMMENT '最大招生人数',
  enrolled_count  INT          NOT NULL DEFAULT 0 COMMENT '已报名人数（缓存值，以 enrollment 表实际计数为准）',
  session_status  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                  COMMENT '班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）',
  remark          TEXT         DEFAULT NULL COMMENT '班期备注（富文本HTML）',
  creator         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 班期编号由系统生成且不复用；tenant_id 参与唯一键，避免不同租户生成相同编号时冲突
  UNIQUE INDEX uk_tenant_session_code (tenant_id, session_code, deleted),
  INDEX idx_course_id (course_id),
  INDEX idx_start_date (start_date),
  INDEX idx_session_status (session_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班期表';

-- ========== 4. 班期费用配置表（与班期 1:1） ==========
-- ⚠️ 所有金额字段单位：元（DECIMAL），调用 ruoyi PayOrderApi/PayTransferApi 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_session_fee_config (
  id                            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '费用配置ID',
  session_id                    BIGINT         NOT NULL COMMENT '班期ID（唯一）',
  tuition_amount                DECIMAL(10,2)  NOT NULL COMMENT '学费总额（元）',
  tuition_includes_accommodation TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '学费是否包含住宿费',
  extra_fee_items               JSON           DEFAULT NULL COMMENT '另行缴费项目（[{"name":"教材费","amount":200,"required":true}]）',
  deposit_amount                DECIMAL(10,2)  NOT NULL COMMENT '定金金额（元）',
  is_groupon_enabled            TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否开启拼团',
  groupon_discount_amount       DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团优惠减免金额（元）',
  groupon_required_count        INT            NOT NULL DEFAULT 3 COMMENT '拼团所需人数',
  groupon_expire_hours          INT            NOT NULL DEFAULT 24 COMMENT '拼团有效时间（小时）',
  groupon_fail_discount_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团失败降级优惠金额（元，单人可享）',
  is_deposit_refundable          TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '定金是否可退',
  balance_due_days_before_start  INT          NOT NULL DEFAULT 7  COMMENT '尾款截止：开班前N天',
  refund_policy_text            TEXT           DEFAULT NULL COMMENT '退款政策说明文本',
  creator                       VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
  updater                       VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
  create_time                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time                   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted                       TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id                     BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：同班期费用配置软删除后可重新创建
  UNIQUE INDEX uk_session_id (session_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班期费用配置表';

-- ========== 4.4 班期场地信息表（1:1 挂在班期上，随班期表单内嵌面板维护） ==========
-- 即「学校信息」：名称 / 地址 / 经纬度坐标 + 三个富文本字段承载
-- 「学校概况 / 住宿条件 / 教学条件」（图片由富文本内嵌，不单列图片字段）；
-- 与费用配置同属班期附属配置，不建独立管理页面与菜单
CREATE TABLE IF NOT EXISTS maritime_session_venue (
  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '班期场地ID',
  session_id            BIGINT       NOT NULL COMMENT '班期ID（唯一）',
  campus_name           VARCHAR(100) NOT NULL DEFAULT '' COMMENT '学校名称',
  province              VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '省',
  city                  VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '市（列表按此筛选）',
  district              VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '区 / 县',
  address               VARCHAR(255) NOT NULL DEFAULT '' COMMENT '详细地址（省市区之后的部分）',
  latitude              DECIMAL(10,7) DEFAULT NULL COMMENT '纬度（-90 ~ 90）',
  longitude             DECIMAL(10,7) DEFAULT NULL COMMENT '经度（-180 ~ 180）',
  contact_phone         VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '咨询电话',
  campus_intro          TEXT         DEFAULT NULL COMMENT '学校概况（富文本HTML）',
  accommodation_intro   TEXT         DEFAULT NULL COMMENT '住宿条件（富文本HTML，可空）',
  classroom_intro       TEXT         DEFAULT NULL COMMENT '教学条件（富文本HTML）',
  creator               VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater               VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted               TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id             BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：同班期场地信息软删除后可重新创建
  UNIQUE INDEX uk_session_id (session_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班期场地信息表';

-- ========== 4.5 费用模板表（独立定价方案；新增班期时选择并快照复制到班期费用配置） ==========
-- ⚠️ 所有金额字段单位：元（DECIMAL），调用 ruoyi PayOrderApi/PayTransferApi 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_fee_template (
  id                            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  name                          VARCHAR(50)    NOT NULL COMMENT '模板名称（如：STCW 标准收费）',
  tuition_amount                DECIMAL(10,2)  NOT NULL COMMENT '学费总额（元）',
  tuition_includes_accommodation TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '学费是否包含住宿费',
  extra_fee_items               JSON           DEFAULT NULL COMMENT '另行缴费项目（[{"name":"教材费","amount":200,"required":true}]）',
  deposit_amount                DECIMAL(10,2)  NOT NULL COMMENT '定金金额（元）',
  is_groupon_enabled            TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否开启拼团',
  groupon_discount_amount       DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团优惠减免金额（元）',
  groupon_required_count        INT            NOT NULL DEFAULT 3 COMMENT '拼团所需人数',
  groupon_expire_hours          INT            NOT NULL DEFAULT 24 COMMENT '拼团有效时间（小时）',
  groupon_fail_discount_amount  DECIMAL(10,2)  NOT NULL DEFAULT 0 COMMENT '拼团失败降级优惠金额（元，单人可享）',
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

-- ========== 4.6 学校库表（独立学校资料；新增班期时选择并快照复制到班期场地信息） ==========
-- 与费用模板同一思路：学校信息只维护一份，班期选择后快照存进 maritime_session_venue，
-- 学校库后续修改不影响已创建的班期
CREATE TABLE IF NOT EXISTS maritime_school (
  id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  name                  VARCHAR(100) NOT NULL COMMENT '学校名称',
  province              VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '省',
  city                  VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '市（列表按此筛选）',
  district              VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '区 / 县',
  address               VARCHAR(255) NOT NULL DEFAULT '' COMMENT '详细地址（省市区之后的部分）',
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

-- ========== 5. 报名记录表 ==========
-- ⚠️ 所有金额字段单位：元（DECIMAL），调用 ruoyi PayOrderApi 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_enrollment (
  id                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  enrollment_no           VARCHAR(64)   NOT NULL COMMENT '报名单号（系统唯一，格式：EN+时间戳）',
  member_id               BIGINT        NOT NULL COMMENT '学员 member_user.id',
  session_id              BIGINT        NOT NULL COMMENT '班期ID',
  groupon_record_id       BIGINT        DEFAULT NULL COMMENT '关联拼团记录（可为空）',
  real_name               VARCHAR(50)   NOT NULL COMMENT '真实姓名',
  id_card                 VARCHAR(64)   NOT NULL COMMENT '身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）',
  phone                   VARCHAR(20)   NOT NULL COMMENT '手机号',
  total_amount            DECIMAL(10,2) NOT NULL COMMENT '应缴总金额（元，报名时快照）',
  deposit_amount_snapshot DECIMAL(10,2) NOT NULL COMMENT '定金金额快照（元，报名时费用配置的定金值）',
  balance_amount          DECIMAL(10,2) NOT NULL COMMENT '尾款金额（元，= total_amount - deposit_amount_snapshot）',
  paid_amount             DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已支付金额（元，含定金+已付尾款）',
  balance_due_date        DATE          DEFAULT NULL COMMENT '尾款截止缴纳日期',
  enrollment_status       VARCHAR(30)   NOT NULL DEFAULT 'PENDING_DEPOSIT'
                          COMMENT '报名状态(PENDING_DEPOSIT/DEPOSITED/PAID_OFF/MANUAL_PROCESSING/COMPLETED/CANCELLED)',
  cancel_reason           VARCHAR(500)  DEFAULT NULL COMMENT '取消原因',
  creator                 VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建者',
  updater                 VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新者',
  create_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted                 TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id               BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- enrollment_no 系统生成不重用，无需含 deleted
  UNIQUE INDEX uk_enrollment_no (enrollment_no),
  -- 含 deleted：学员取消报名后可重新报名同一班期
  UNIQUE INDEX uk_id_card_session (id_card, session_id, deleted),
  INDEX idx_member_id (member_id),
  INDEX idx_session_id (session_id),
  INDEX idx_enrollment_status (enrollment_status),
  INDEX idx_referred_by (referred_by_member_id),
  INDEX idx_balance_due_date (balance_due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';

-- ========== 6. 报名订单表 ==========
-- ⚠️ amount 单位：元（DECIMAL），调用 ruoyi PayOrderCreateReqDTO 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_enrollment_order (
  id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  enrollment_id   BIGINT         NOT NULL COMMENT '报名ID',
  order_no        VARCHAR(64)    NOT NULL COMMENT '订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）',
  order_type      VARCHAR(20)    NOT NULL COMMENT '订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）',
  amount          DECIMAL(10,2)  NOT NULL COMMENT '应付金额（元）',
  pay_order_id    BIGINT         DEFAULT NULL COMMENT 'ruoyi pay模块订单ID',
  pay_channel     VARCHAR(20)    DEFAULT NULL COMMENT '支付渠道（wx_lite等）',
  order_status    VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
                  COMMENT '订单状态（PENDING/PAID/CLOSED/REFUNDED）',
  pay_time        DATETIME       DEFAULT NULL COMMENT '支付时间',
  expire_time     DATETIME       DEFAULT NULL COMMENT '订单过期时间',
  creator         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- order_no 系统生成不重用，无需含 deleted
  UNIQUE INDEX uk_order_no (order_no),
  INDEX idx_enrollment_id (enrollment_id),
  INDEX idx_order_status (order_status),
  INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名订单表';

-- ========== 7. 退费申请表 ==========
-- ⚠️ refund_amount 单位：元（DECIMAL），调用 ruoyi PayRefundCreateReqDTO 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_refund_apply (
  id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '退费申请ID',
  enrollment_id   BIGINT        NOT NULL COMMENT '报名ID',
  order_id        BIGINT        DEFAULT NULL COMMENT '关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）',
  member_id       BIGINT        NOT NULL COMMENT '申请人',
  apply_reason    VARCHAR(500)  NOT NULL COMMENT '退费原因',
  refund_amount   DECIMAL(10,2) DEFAULT NULL COMMENT '退费金额（元，审核后填写）',
  apply_status    VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                  COMMENT '申请状态（PENDING/APPROVED/REJECTED/REFUNDED）',
  admin_remark    VARCHAR(500)  DEFAULT NULL COMMENT '管理员备注',
  approve_time    DATETIME      DEFAULT NULL COMMENT '审核时间',
  approver_id     BIGINT        DEFAULT NULL COMMENT '审核人（系统用户ID）',
  pay_refund_id   BIGINT        DEFAULT NULL COMMENT 'ruoyi pay_refund.id（退款发起后填写）',
  refund_time     DATETIME      DEFAULT NULL COMMENT '实际退款到账时间',
  creator         VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  INDEX idx_enrollment_id (enrollment_id),
  INDEX idx_member_id (member_id),
  INDEX idx_apply_status (apply_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退费申请表';

-- ========== 9. 用户站内消息表 ==========
CREATE TABLE IF NOT EXISTS maritime_user_message (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  member_id       BIGINT       NOT NULL COMMENT '接收人 member_user.id',
  type            VARCHAR(30)  NOT NULL
                  COMMENT '消息类型（ORDER=订单类/GROUPON=拼团类/SYSTEM=系统公告）',
  title           VARCHAR(200) NOT NULL COMMENT '消息标题',
  content         TEXT         DEFAULT NULL COMMENT '消息正文',
  related_id      BIGINT       DEFAULT NULL COMMENT '关联业务ID（enrollment_id 等，可为空）',
  related_type    VARCHAR(30)  DEFAULT NULL COMMENT '关联业务类型（ENROLLMENT/GROUPON）',
  is_read         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读（0未读 1已读）',
  read_time       DATETIME     DEFAULT NULL COMMENT '阅读时间',
  creator         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  INDEX idx_member_id (member_id),
  INDEX idx_member_unread (member_id, is_read),
  INDEX idx_type (type),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户站内消息表';

-- ========== 10. 拼团记录表 ==========
CREATE TABLE IF NOT EXISTS maritime_groupon_record (
  id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '拼团记录ID',
  session_id      BIGINT      NOT NULL COMMENT '班期ID',
  invite_code     VARCHAR(20) NOT NULL COMMENT '拼团邀请码（唯一）',
  initiator_enrollment_id BIGINT NOT NULL COMMENT '发起人报名ID',
  required_count  INT         NOT NULL DEFAULT 3 COMMENT '需要人数',
  current_count   INT         NOT NULL DEFAULT 1 COMMENT '当前人数',
  groupon_status  VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
                  COMMENT '拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）',
  expire_time     DATETIME    NOT NULL COMMENT '拼团截止时间',
  success_time    DATETIME    DEFAULT NULL COMMENT '拼团成功时间',
  creator         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT      NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：邀请码软删除后可重新生成相同码（低概率但合规）
  UNIQUE INDEX uk_invite_code (invite_code, deleted),
  INDEX idx_session_id (session_id),
  INDEX idx_groupon_status (groupon_status),
  INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团记录表';

-- ========== 11. 拼团成员表 ==========
CREATE TABLE IF NOT EXISTS maritime_groupon_member (
  id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '拼团成员ID',
  groupon_record_id BIGINT    NOT NULL COMMENT '拼团记录ID',
  enrollment_id   BIGINT      NOT NULL COMMENT '报名ID',
  member_id       BIGINT      NOT NULL COMMENT '学员ID',
  join_time       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  member_status   VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '成员状态（ACTIVE正常/CANCELLED已退出）',
  deposit_paid_at DATETIME    DEFAULT NULL COMMENT '定金支付时间（用于24h拼团有效期检查）',
  creator         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT      NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：成员退出后可重新加入同一拼团
  UNIQUE INDEX uk_groupon_enrollment (groupon_record_id, enrollment_id, deleted),
  INDEX idx_groupon_record_id (groupon_record_id),
  INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拼团成员表';

-- ========== 12. 会员微信订阅状态表 ==========
CREATE TABLE IF NOT EXISTS maritime_member_subscribe (
  id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  member_id      BIGINT        NOT NULL COMMENT '会员ID',
  template_title VARCHAR(100)  NOT NULL COMMENT '微信订阅消息模板标题',
  subscribed     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否已授权订阅（1已授权 0拒绝/过期）',
  creator        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建者',
  updater        VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新者',
  create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  tenant_id      BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  UNIQUE INDEX uk_member_template (member_id, template_title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员微信订阅消息授权状态表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 海员培训 后台菜单 SQL
-- 说明：使用变量传递 ID，无需手工修改；可重复执行前先清理旧数据
-- ============================================================

-- ==================== 一级目录：海员培训 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('海员培训', '', 1, 80, 0, '/maritime', 'ep:ship', NULL, 0, NULL);
SET @maritimeId := LAST_INSERT_ID();

-- ==================== 课程管理 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('课程管理', '', 2, 1, @maritimeId, 'course', '', 'maritime/course/index', 0, 'MaritimeCourse');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('课程查询', 'maritime:course:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('课程新增', 'maritime:course:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('课程编辑', 'maritime:course:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('课程删除', 'maritime:course:delete', 3, 4, @menuId, '', '', '', 0);

-- ==================== 班期管理 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('班期管理', '', 2, 2, @maritimeId, 'session', '', 'maritime/session/index', 0, 'CourseSession');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('班期查询', 'maritime:course-session:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('班期新增', 'maritime:course-session:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('班期编辑', 'maritime:course-session:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('班期删除', 'maritime:course-session:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('班期导出', 'maritime:course-session:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 报名管理 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('报名管理', '', 2, 4, @maritimeId, 'enrollment', '', 'maritime/enrollment/index', 0, 'Enrollment');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('报名查询', 'maritime:enrollment:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('报名新增', 'maritime:enrollment:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('报名编辑', 'maritime:enrollment:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('报名删除', 'maritime:enrollment:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('报名导出', 'maritime:enrollment:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 报名订单 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('报名订单', '', 2, 5, @maritimeId, 'enrollment-order', '', 'maritime/enrollmentOrder/index', 0, 'EnrollmentOrder');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('订单查询', 'maritime:enrollment-order:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('订单新增', 'maritime:enrollment-order:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('订单编辑', 'maritime:enrollment-order:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('订单删除', 'maritime:enrollment-order:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('订单导出', 'maritime:enrollment-order:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 退款申请 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('退款申请', '', 2, 6, @maritimeId, 'refund-apply', '', 'maritime/refundApply/index', 0, 'RefundApply');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('退款查询', 'maritime:refund-apply:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('退款审核', 'maritime:refund-apply:update', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('退款删除', 'maritime:refund-apply:delete', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('退款导出', 'maritime:refund-apply:export', 3, 4, @menuId, '', '', '', 0);

-- ==================== 拼团记录 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('拼团记录', '', 2, 7, @maritimeId, 'groupon-record', '', 'maritime/grouponRecord/index', 0, 'GrouponRecord');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('拼团查询', 'maritime:groupon-record:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('拼团新增', 'maritime:groupon-record:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('拼团编辑', 'maritime:groupon-record:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('拼团删除', 'maritime:groupon-record:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('拼团导出', 'maritime:groupon-record:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 拼团成员 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('拼团成员', '', 2, 8, @maritimeId, 'groupon-member', '', 'maritime/grouponMember/index', 0, 'GrouponMember');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('成员查询', 'maritime:groupon-member:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('成员新增', 'maritime:groupon-member:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('成员编辑', 'maritime:groupon-member:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('成员删除', 'maritime:groupon-member:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('成员导出', 'maritime:groupon-member:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 用户消息 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('用户消息', '', 2, 17, @maritimeId, 'user-message', '', 'maritime/userMessage/index', 0, 'UserMessage');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('消息查询', 'maritime:user-message:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('消息新增', 'maritime:user-message:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('消息编辑', 'maritime:user-message:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('消息删除', 'maritime:user-message:delete', 3, 4, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('消息导出', 'maritime:user-message:export', 3, 5, @menuId, '', '', '', 0);

-- ==================== 数据统计 ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('数据统计', '', 2, 18, @maritimeId, 'stats', '', 'maritime/stats/index', 0, 'MaritimeStats');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('运营看板',   'maritime:dashboard:view',   3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('财务汇总',   'maritime:finance:view',     3, 2, @menuId, '', '', '', 0);

-- ==================== 费用模板（独立定价方案，菜单顺序可在后台调整） ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('费用模板', '', 2, 19, @maritimeId, 'fee-template', '', 'maritime/feeTemplate/index', 0, 'FeeTemplate');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板查询', 'maritime:fee-template:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板新增', 'maritime:fee-template:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板编辑', 'maritime:fee-template:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('模板删除', 'maritime:fee-template:delete', 3, 4, @menuId, '', '', '', 0);

-- ==================== 学校管理（独立学校资料，新增班期时选择并带入） ====================
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status, component_name)
VALUES ('学校管理', '', 2, 20, @maritimeId, 'school', '', 'maritime/school/index', 0, 'MaritimeSchool');
SET @menuId := LAST_INSERT_ID();
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校查询', 'maritime:school:query',  3, 1, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校新增', 'maritime:school:create', 3, 2, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校编辑', 'maritime:school:update', 3, 3, @menuId, '', '', '', 0);
INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status) VALUES ('学校删除', 'maritime:school:delete', 3, 4, @menuId, '', '', '', 0);
