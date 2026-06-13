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
  ADD COLUMN open_id            VARCHAR(64)  DEFAULT NULL COMMENT '微信 openid（用于自推检测、企业付款收款方标识）',
  ADD COLUMN referral_code      VARCHAR(20)  DEFAULT NULL COMMENT '我的推荐码（格式 REF-{uid}-{4位随机码}）',
  ADD COLUMN referred_by        BIGINT       DEFAULT NULL COMMENT '被谁推荐的（推荐人 member_user.id）',
  ADD COLUMN is_referral_banned TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否禁止推荐（0正常 1禁推，账号封禁请用 status 字段）';

ALTER TABLE member_user ADD UNIQUE INDEX uk_referral_code (referral_code, tenant_id);
ALTER TABLE member_user ADD INDEX idx_referred_by (referred_by);
ALTER TABLE member_user ADD INDEX idx_open_id (open_id);

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
  session_code    VARCHAR(30)  NOT NULL COMMENT '班期编号（如：2026-06-SH-001）',
  session_name    VARCHAR(50)  DEFAULT NULL COMMENT '班级名称（可选，如：A班）',
  location        VARCHAR(100) NOT NULL COMMENT '本期上课地点',
  duration_days   INT          NOT NULL COMMENT '本期课程天数',
  instructor_info VARCHAR(500) DEFAULT NULL COMMENT '本期讲师信息',
  start_date      DATE         NOT NULL COMMENT '开班日期',
  end_date        DATE         NOT NULL COMMENT '结束日期',
  enrollment_deadline DATE     DEFAULT NULL COMMENT '报名截止日期',
  max_students    INT          NOT NULL DEFAULT 30 COMMENT '最大招生人数',
  enrolled_count  INT          NOT NULL DEFAULT 0 COMMENT '已报名人数（缓存值，以 enrollment 表实际计数为准）',
  session_status  VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                  COMMENT '班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）',
  remark          VARCHAR(500) DEFAULT NULL COMMENT '班期备注',
  creator         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater         VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 班期编号含 deleted：同编号的班期软删除后可重新创建
  UNIQUE INDEX uk_session_code (session_code, deleted),
  INDEX idx_course_id (course_id),
  INDEX idx_start_date (start_date),
  INDEX idx_session_status (session_status),
  INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班期表';

-- ========== 4. 班期费用配置表（与班期 1:1） ==========
-- ⚠️ 所有金额字段单位：元（DECIMAL），调用 ruoyi PayOrderApi/PayTransferApi 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_session_fee_config (
  id                            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '费用配置ID',
  session_id                    BIGINT         NOT NULL COMMENT '班期ID（唯一）',
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

-- ========== 5. 报名记录表 ==========
-- ⚠️ 所有金额字段单位：元（DECIMAL），调用 ruoyi PayOrderApi 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_enrollment (
  id                      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  enrollment_no           VARCHAR(64)   NOT NULL COMMENT '报名单号（系统唯一，格式：EN+时间戳）',
  member_id               BIGINT        NOT NULL COMMENT '学员 member_user.id',
  session_id              BIGINT        NOT NULL COMMENT '班期ID',
  groupon_record_id       BIGINT        DEFAULT NULL COMMENT '关联拼团记录（可为空）',
  referred_by_member_id   BIGINT        DEFAULT NULL COMMENT '推荐人 member_user.id',
  referral_code_used      VARCHAR(16)   DEFAULT NULL COMMENT '报名时填写的推荐码',
  real_name               VARCHAR(50)   NOT NULL COMMENT '真实姓名',
  id_card                 VARCHAR(64)   NOT NULL COMMENT '身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）',
  phone                   VARCHAR(20)   NOT NULL COMMENT '手机号',
  total_amount            DECIMAL(10,2) NOT NULL COMMENT '应缴总金额（元，报名时快照）',
  deposit_amount_snapshot DECIMAL(10,2) NOT NULL COMMENT '定金金额快照（元，报名时费用配置的定金值）',
  balance_amount          DECIMAL(10,2) NOT NULL COMMENT '尾款金额（元，= total_amount - deposit_amount_snapshot）',
  paid_amount             DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已支付金额（元，含定金+已付尾款）',
  balance_due_date        DATE          DEFAULT NULL COMMENT '尾款截止缴纳日期',
  enrollment_status       VARCHAR(30)   NOT NULL DEFAULT 'PENDING_DEPOSIT'
                          COMMENT '报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)',
  referral_right_granted  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否已获得推荐权（支付定金后=1）',
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

-- ========== 8. 最新动态/公告表 ==========
CREATE TABLE IF NOT EXISTS maritime_announcement (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title        VARCHAR(200) NOT NULL COMMENT '标题',
  content      MEDIUMTEXT   DEFAULT NULL COMMENT '正文内容（富文本HTML）',
  summary      VARCHAR(500) DEFAULT NULL COMMENT '摘要（列表页展示，1-2行）',
  cover_image  VARCHAR(500) DEFAULT NULL COMMENT '缩略图/封面图URL',
  type         VARCHAR(20)  NOT NULL DEFAULT 'NOTICE'
               COMMENT '类型（NEW_COURSE新课/NOTICE公告/ACTIVITY活动/CASE成功案例）',
  external_url VARCHAR(500) DEFAULT NULL COMMENT '外链URL（可选，点击后跳转）',
  publish_time DATETIME     DEFAULT NULL COMMENT '发布时间（支持定时发布，NULL表示立即发布）',
  is_top       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否置顶（0否 1是，最多3条同时置顶）',
  top_order    INT          DEFAULT NULL COMMENT '置顶排序序号（置顶时有效，越小越靠前）',
  sort_order   INT          NOT NULL DEFAULT 0 COMMENT '非置顶优先级权重（越大越靠前）',
  status       VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态（DRAFT草稿/PUBLISHED已发布）',
  view_count   INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
  creator      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  INDEX idx_type (type),
  INDEX idx_status_publish (status, publish_time),
  INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最新动态/公告表';

-- ========== 9. 用户站内消息表 ==========
CREATE TABLE IF NOT EXISTS maritime_user_message (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  member_id       BIGINT       NOT NULL COMMENT '接收人 member_user.id',
  type            VARCHAR(30)  NOT NULL
                  COMMENT '消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）',
  title           VARCHAR(200) NOT NULL COMMENT '消息标题',
  content         TEXT         DEFAULT NULL COMMENT '消息正文',
  related_id      BIGINT       DEFAULT NULL COMMENT '关联业务ID（enrollment_id/commission_record_id等，可为空）',
  related_type    VARCHAR(30)  DEFAULT NULL COMMENT '关联业务类型（ENROLLMENT/COMMISSION/GROUPON）',
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

-- ========== 12. 推荐关系表 ==========
CREATE TABLE IF NOT EXISTS maritime_referral_relation (
  id                     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '推荐关系ID',
  referrer_member_id     BIGINT       NOT NULL COMMENT '推荐人 member_id',
  referred_enrollment_id BIGINT       NOT NULL COMMENT '被推荐人的报名ID',
  referred_member_id     BIGINT       NOT NULL COMMENT '被推荐人 member_id',
  session_id             BIGINT       NOT NULL COMMENT '班期ID',
  relation_status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                         COMMENT '关系状态（ACTIVE/INVALID）',
  invalid_reason         VARCHAR(200) DEFAULT NULL COMMENT '无效原因（风控拒绝等）',
  first_click_at         DATETIME     DEFAULT NULL COMMENT '推荐链接首次点击时间',
  referral_locked_at     DATETIME     DEFAULT NULL COMMENT '推荐关系锁定时间（被推荐人支付定金时）',
  creator                VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater                VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted                TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id              BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：推荐关系无效后可为同一报名创建新推荐关系
  UNIQUE INDEX uk_referred_enrollment (referred_enrollment_id, deleted),
  INDEX idx_referrer_member (referrer_member_id),
  INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐关系表';

-- ========== 13. 佣金记录表 ==========
-- ⚠️ commission_amount 单位：元（DECIMAL），调用 ruoyi PayTransferCreateReqDTO 时需 ×100 转换为分
CREATE TABLE IF NOT EXISTS maritime_commission_record (
  id                     BIGINT         NOT NULL AUTO_INCREMENT COMMENT '佣金记录ID',
  referrer_member_id     BIGINT         NOT NULL COMMENT '推荐人',
  referred_enrollment_id BIGINT         NOT NULL COMMENT '被推荐人的报名ID',
  session_id             BIGINT         NOT NULL COMMENT '班期ID',
  commission_amount      DECIMAL(10,2)  NOT NULL COMMENT '佣金金额（元）',
  commission_status      VARCHAR(30)    NOT NULL DEFAULT 'WAITING_FOR_CLASS'
    COMMENT '状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)',
  expected_settle_date   DATE           NOT NULL COMMENT '预计结算日期（= 开课日期 + 7天）',
  is_risk_flagged        TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否风控标记',
  risk_check_result      JSON           DEFAULT NULL COMMENT '风控检查详情（JSON）',
  trigger_time           DATETIME       NOT NULL COMMENT '触发时间（全款支付时）',
  settle_check_time      DATETIME       DEFAULT NULL COMMENT '开课7天检查执行时间',
  approve_time           DATETIME       DEFAULT NULL COMMENT '审核通过时间',
  approver_id            BIGINT         DEFAULT NULL COMMENT '审核人',
  approve_remark         VARCHAR(500)   DEFAULT NULL COMMENT '审核备注',
  payout_time            DATETIME       DEFAULT NULL COMMENT '发放时间',
  pay_transfer_id        BIGINT         DEFAULT NULL COMMENT 'ruoyi pay_transfer.id（调用 PayTransferApi 后填写）',
  fail_reason            VARCHAR(500)   DEFAULT NULL COMMENT '发放失败原因',
  retry_count            INT            NOT NULL DEFAULT 0 COMMENT '自动发放失败后已重试次数',
  creator                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '创建者',
  updater                VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '更新者',
  create_time            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted                TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id              BIGINT         NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：一笔报名只能产生一条有效佣金记录
  UNIQUE INDEX uk_referred_enrollment (referred_enrollment_id, deleted),
  INDEX idx_referrer_member (referrer_member_id),
  INDEX idx_commission_status (commission_status),
  INDEX idx_expected_settle_date (expected_settle_date),
  INDEX idx_trigger_time (trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金记录表';

-- ========== 14. 推荐人佣金账户（汇总） ==========
CREATE TABLE IF NOT EXISTS maritime_commission_account (
  id                   BIGINT        NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  member_id            BIGINT        NOT NULL COMMENT '推荐人 member_id（唯一）',
  total_amount         DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '累计佣金总额（元）',
  paid_amount          DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已发放金额（元）',
  pending_amount       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '待发放金额（元）',
  frozen_amount        DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '冻结中金额（元）',
  rejected_amount      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '被拒绝的佣金总额（元）',
  total_referral_count INT           NOT NULL DEFAULT 0 COMMENT '累计成功推荐人数',
  version              INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  creator              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建者',
  updater              VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新者',
  create_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted              TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id            BIGINT        NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：账户异常删除后可为同一会员重建账户
  UNIQUE INDEX uk_member_id (member_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐人佣金账户';

-- ========== 15. 风控手机号黑名单 ==========
CREATE TABLE IF NOT EXISTS maritime_risk_blacklist (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  phone       VARCHAR(20)  NOT NULL COMMENT '手机号',
  reason      VARCHAR(200) NOT NULL COMMENT '加入黑名单原因',
  operator_id BIGINT       DEFAULT NULL COMMENT '操作人（管理员）',
  creator     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建者',
  updater     VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新者',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
  tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (id),
  -- 含 deleted：黑名单软删除后同一手机号可重新拉入黑名单
  UNIQUE INDEX uk_phone (phone, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控手机号黑名单';

SET FOREIGN_KEY_CHECKS = 1;
