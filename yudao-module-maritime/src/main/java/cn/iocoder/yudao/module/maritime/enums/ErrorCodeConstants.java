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
    ErrorCode COURSE_SESSION_NOT_EXISTS = new ErrorCode(1_009_002_004, "班期不存在");
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

    // ========== 拼团模块 1-009-004-000 ==========
    ErrorCode GROUPON_NOT_EXISTS = new ErrorCode(1_009_004_000, "拼团不存在");
    ErrorCode GROUPON_RECORD_NOT_EXISTS = new ErrorCode(1_009_004_004, "拼团记录不存在");
    ErrorCode GROUPON_MEMBER_NOT_EXISTS = new ErrorCode(1_009_004_005, "拼团成员不存在");
    ErrorCode GROUPON_EXPIRED = new ErrorCode(1_009_004_001, "拼团已超时");
    ErrorCode GROUPON_FULL = new ErrorCode(1_009_004_002, "拼团人数已满");
    ErrorCode GROUPON_ALREADY_JOINED = new ErrorCode(1_009_004_003, "已参与该拼团");

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

    // ========== 消息/公告模块 1-009-008-000 ==========
    ErrorCode ANNOUNCEMENT_NOT_EXISTS = new ErrorCode(1_009_008_000, "公告不存在");
    ErrorCode USER_MESSAGE_NOT_EXISTS = new ErrorCode(1_009_008_001, "站内消息不存在");

    // ========== 风控模块 1-009-007-000 ==========
    ErrorCode RISK_BLACKLIST_EXISTS = new ErrorCode(1_009_007_000, "该手机号已在黑名单中");
    ErrorCode RISK_BLACKLIST_NOT_EXISTS = new ErrorCode(1_009_007_001, "黑名单记录不存在");
    ErrorCode RISK_ENROLLMENT_BLOCKED = new ErrorCode(1_009_007_002, "该手机号已被禁止报名");

}
