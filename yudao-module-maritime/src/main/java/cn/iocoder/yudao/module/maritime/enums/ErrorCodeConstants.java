package cn.iocoder.yudao.module.maritime.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Maritime 模块错误码枚举
 *
 * maritime 系统，使用 1-009-xxx-xxx 段
 */
public interface ErrorCodeConstants {

    // ========== 课程模块 1-009-001-000 ==========
    ErrorCode COURSE_NOT_EXISTS = new ErrorCode(1_009_001_000, "课程不存在");

    // ========== 班期模块 1-009-002-000 ==========
    ErrorCode SESSION_NOT_EXISTS = new ErrorCode(1_009_002_000, "班期不存在");
    ErrorCode SESSION_FEE_CONFIG_NOT_EXISTS = new ErrorCode(1_009_002_005, "班期费用配置不存在");
    ErrorCode SESSION_NOT_OPEN = new ErrorCode(1_009_002_001, "班期不在招生状态");
    ErrorCode SESSION_FULL = new ErrorCode(1_009_002_002, "班期名额已满");
    ErrorCode SESSION_CODE_DUPLICATE = new ErrorCode(1_009_002_003, "班期编号已存在");

    // ========== 报名模块 1-009-003-000 ==========
    ErrorCode ENROLLMENT_NOT_EXISTS = new ErrorCode(1_009_003_000, "报名记录不存在");
    ErrorCode ENROLLMENT_DUPLICATE = new ErrorCode(1_009_003_001, "同一班期不能重复报名");
    ErrorCode ENROLLMENT_ORDER_NOT_EXISTS = new ErrorCode(1_009_003_002, "报名订单不存在");
    ErrorCode ENROLLMENT_CANCEL_NOT_ALLOWED = new ErrorCode(1_009_003_003, "当前状态不允许取消报名");
    ErrorCode ENROLLMENT_CONFIRM_NOT_ALLOWED = new ErrorCode(1_009_003_004, "当前状态不允许人工确认报名");
    ErrorCode ORDER_ALREADY_PAID = new ErrorCode(1_009_003_005, "订单已支付或已关闭");
    ErrorCode ORDER_EXPIRED = new ErrorCode(1_009_003_006, "订单已过期");
    ErrorCode ENROLLMENT_STATUS_ERROR = new ErrorCode(1_009_003_007, "报名状态不正确，无法执行此操作");
    ErrorCode BALANCE_NOT_REQUIRED = new ErrorCode(1_009_003_008, "该报名无需支付尾款（已全额支付）");

    // ========== 拼团模块 1-009-004-000 ==========
    ErrorCode GROUPON_NOT_EXISTS = new ErrorCode(1_009_004_000, "拼团不存在");
    ErrorCode GROUPON_RECORD_NOT_EXISTS = new ErrorCode(1_009_004_004, "拼团记录不存在");
    ErrorCode GROUPON_MEMBER_NOT_EXISTS = new ErrorCode(1_009_004_005, "拼团成员不存在");
    ErrorCode GROUPON_EXPIRED = new ErrorCode(1_009_004_001, "拼团已超时");
    ErrorCode GROUPON_FULL = new ErrorCode(1_009_004_002, "拼团人数已满");
    ErrorCode GROUPON_ALREADY_JOINED = new ErrorCode(1_009_004_003, "已参与该拼团");
    ErrorCode GROUPON_NOT_ENABLED = new ErrorCode(1_009_004_006, "该班期未开启拼团");
    ErrorCode GROUPON_SESSION_MISMATCH = new ErrorCode(1_009_004_007, "邀请码不属于当前班期");
    ErrorCode GROUPON_NOT_IN_PROGRESS = new ErrorCode(1_009_004_008, "拼团已结束，无法加入");

    // ========== 推荐/佣金模块 1-009-005-000 ==========
    ErrorCode REFERRAL_CODE_NOT_EXISTS = new ErrorCode(1_009_005_000, "推荐码不存在");
    ErrorCode REFERRAL_SELF_NOT_ALLOWED = new ErrorCode(1_009_005_001, "不能使用自己的推荐码");
    ErrorCode REFERRAL_ALREADY_EXISTS = new ErrorCode(1_009_005_002, "已有推荐关系，不能重复绑定");
    ErrorCode COMMISSION_NOT_EXISTS = new ErrorCode(1_009_005_003, "佣金记录不存在");
    ErrorCode COMMISSION_RECORD_NOT_EXISTS = new ErrorCode(1_009_005_005, "佣金明细记录不存在");
    ErrorCode COMMISSION_ACCOUNT_NOT_EXISTS = new ErrorCode(1_009_005_006, "佣金账户不存在");
    ErrorCode REFERRAL_RELATION_NOT_EXISTS = new ErrorCode(1_009_005_007, "推荐关系不存在");
    ErrorCode COMMISSION_APPROVE_NOT_ALLOWED = new ErrorCode(1_009_005_004, "佣金当前状态不允许审核");

    // ========== 退款模块 1-009-006-000 ==========
    ErrorCode REFUND_APPLY_NOT_EXISTS = new ErrorCode(1_009_006_000, "退费申请不存在");
    ErrorCode REFUND_APPLY_DUPLICATE = new ErrorCode(1_009_006_001, "已有进行中的退费申请");
    ErrorCode REFUND_NOT_ALLOWED = new ErrorCode(1_009_006_002, "当前状态不允许发起退费");

    // ========== 提现模块 1-009-009-000 ==========
    ErrorCode WITHDRAWAL_NOT_EXISTS = new ErrorCode(1_009_009_000, "提现申请不存在");
    ErrorCode WITHDRAWAL_AMOUNT_TOO_SMALL = new ErrorCode(1_009_009_001, "提现金额不能低于100元");
    ErrorCode WITHDRAWAL_AMOUNT_TOO_LARGE = new ErrorCode(1_009_009_002, "单次提现金额不能超过1000元");
    ErrorCode WITHDRAWAL_INSUFFICIENT_BALANCE = new ErrorCode(1_009_009_003, "佣金余额不足");
    ErrorCode WITHDRAWAL_ACCOUNT_FROZEN = new ErrorCode(1_009_009_004, "账户已被冻结，无法提现");
    ErrorCode WITHDRAWAL_IN_FREEZE_PERIOD = new ErrorCode(1_009_009_005, "首次定金支付后7天内不可提现");
    ErrorCode WITHDRAWAL_MONTHLY_LIMIT_EXCEEDED = new ErrorCode(1_009_009_006, "本月提现次数已达上限（5次）");
    ErrorCode WITHDRAWAL_DAILY_LIMIT_EXCEEDED = new ErrorCode(1_009_009_007, "24小时内累计提现金额已达上限（10000元）");
    ErrorCode WITHDRAWAL_PENDING_EXISTS = new ErrorCode(1_009_009_008, "已有进行中的提现申请，请等待处理完成");
    ErrorCode COMMISSION_STATUS_ERROR = new ErrorCode(1_009_009_009, "佣金状态不符，无法执行此操作");

    // ========== 消息/公告模块 1-009-008-000 ==========
    ErrorCode ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_009_008_000, "公告不存在");
    ErrorCode USER_MESSAGE_NOT_EXISTS = new ErrorCode(1_009_008_001, "站内消息不存在");

    // ========== 风控模块 1-009-007-000 ==========
    ErrorCode RISK_BLACKLIST_EXISTS = new ErrorCode(1_009_007_000, "该手机号已在黑名单中");
    ErrorCode RISK_BLACKLIST_NOT_EXISTS = new ErrorCode(1_009_007_001, "黑名单记录不存在");
    ErrorCode RISK_ENROLLMENT_BLOCKED = new ErrorCode(1_009_007_002, "该手机号已被禁止报名");
    ErrorCode GROUPON_CREATE_FREQ_LIMIT = new ErrorCode(1_009_007_003, "操作过于频繁，请稍后再试");
    ErrorCode RISK_EVENT_NOT_EXISTS = new ErrorCode(1_009_007_004, "风控事件不存在");
    ErrorCode RISK_MEMBER_NOT_EXISTS = new ErrorCode(1_009_007_005, "用户不存在");

    // ========== 报表模块 1-009-011-000 ==========
    ErrorCode EXPORT_ROWS_EXCEEDED = new ErrorCode(1_009_011_000, "导出数据量超过上限 10000 行，请缩小筛选范围");
    ErrorCode EXPORT_PERIOD_INVALID = new ErrorCode(1_009_011_001, "统计周期只允许 WEEK / MONTH / ALL_TIME");

}
