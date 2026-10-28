package cn.iocoder.yudao.module.maritime.enums;

/**
 * 站内信模板编码常量
 * 需在管理后台「消息管理 → 站内信模板」提前创建对应模板
 */
public interface NotifyTemplateCodeConstants {

    // ========== 报名 / 支付 ==========
    /** 报名提交成功（支付定金前发送） */
    String ENROLL_SUCCESS     = "MARITIME_ENROLL_SUCCESS";
    String DEPOSIT_PAID       = "MARITIME_DEPOSIT_PAID";
    String FULL_PAID          = "MARITIME_FULL_PAID";
    String BALANCE_PAID       = "MARITIME_BALANCE_PAID";
    String BALANCE_REMINDER   = "MARITIME_BALANCE_REMINDER";
    String BALANCE_OVERDUE    = "MARITIME_BALANCE_OVERDUE";
    String ENROLL_CANCELLED   = "MARITIME_ENROLL_CANCELLED";
    String ENROLL_COMPLETED   = "MARITIME_ENROLL_COMPLETED";

    // ========== 拼团 ==========
    String GROUPON_SUCCESS    = "MARITIME_GROUPON_SUCCESS";
    String GROUPON_DEGRADED   = "MARITIME_GROUPON_DEGRADED";

    // ========== 退费 ==========
    String REFUND_APPLIED     = "MARITIME_REFUND_APPLIED";
    String REFUND_APPROVED    = "MARITIME_REFUND_APPROVED";
    String REFUND_REJECTED    = "MARITIME_REFUND_REJECTED";

}
