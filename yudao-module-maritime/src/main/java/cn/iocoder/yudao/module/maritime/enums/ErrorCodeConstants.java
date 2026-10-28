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
    ErrorCode SESSION_FEE_DEPOSIT_INVALID = new ErrorCode(1_009_002_006, "定金不能大于学费");
    ErrorCode SESSION_FEE_GROUPON_DEPOSIT_INVALID = new ErrorCode(1_009_002_007,
            "全款报名不支持拼团，开启拼团时定金必须大于 0 且小于学费");

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
    ErrorCode GROUPON_CREATE_FREQ_LIMIT = new ErrorCode(1_009_004_009, "操作过于频繁，请稍后再试");

    // ========== 退款模块 1-009-006-000 ==========
    ErrorCode REFUND_APPLY_NOT_EXISTS = new ErrorCode(1_009_006_000, "退费申请不存在");
    ErrorCode REFUND_APPLY_DUPLICATE = new ErrorCode(1_009_006_001, "已有进行中的退费申请");
    ErrorCode REFUND_NOT_ALLOWED = new ErrorCode(1_009_006_002, "当前状态不允许发起退费");

    // ========== 消息模块 1-009-008-000 ==========
    ErrorCode USER_MESSAGE_NOT_EXISTS = new ErrorCode(1_009_008_001, "站内消息不存在");

    // ========== 费用模板 1-009-008-000 ==========
    ErrorCode FEE_TEMPLATE_NOT_EXISTS = new ErrorCode(1_009_008_000, "费用模板不存在");
    ErrorCode FEE_TEMPLATE_NAME_DUPLICATE = new ErrorCode(1_009_008_001, "费用模板名称已存在");
    ErrorCode FEE_TEMPLATE_DISABLED = new ErrorCode(1_009_008_002, "费用模板已停用，不能用于新增班期");

    // ========== 导出模块 1-009-011-000 ==========
    ErrorCode EXPORT_ROWS_EXCEEDED = new ErrorCode(1_009_011_000, "导出数据量超过上限 10000 行，请缩小筛选范围");

    // ========== 主页配置模块 1-009-012-000 ==========
    ErrorCode HOME_CONFIG_NOT_EXISTS = new ErrorCode(1_009_012_000, "主页配置不存在");

    // ========== 学校库模块 1-009-013-000 ==========
    ErrorCode SCHOOL_NOT_EXISTS = new ErrorCode(1_009_013_000, "学校不存在");
    ErrorCode SCHOOL_NAME_DUPLICATE = new ErrorCode(1_009_013_001, "学校名称已存在");
    ErrorCode SCHOOL_DISABLED = new ErrorCode(1_009_013_002, "学校已关闭，不能用于新增班期");

    // ========== 人工处理单 1-009-014-000 ==========
    ErrorCode MANUAL_TASK_NOT_EXISTS = new ErrorCode(1_009_014_000, "人工处理单不存在");
    ErrorCode MANUAL_TASK_ALREADY_CLOSED = new ErrorCode(1_009_014_001, "人工处理单已关闭");
    ErrorCode MANUAL_TASK_REFUND_HANDLE_ELSEWHERE = new ErrorCode(1_009_014_002, "退费类人工单请到退费审核页处理");
    ErrorCode MANUAL_TASK_TARGET_STATUS_INVALID = new ErrorCode(1_009_014_003, "人工处理目标状态不正确");

}
