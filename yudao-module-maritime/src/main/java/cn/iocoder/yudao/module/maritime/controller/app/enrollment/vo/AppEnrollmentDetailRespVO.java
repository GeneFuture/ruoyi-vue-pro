package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "小程序 - 报名详情 Response VO")
@Data
public class AppEnrollmentDetailRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "报名单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enrollmentNo;

    // 班期信息
    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "班期编号")
    private String sessionCode;

    @Schema(description = "班级名称")
    private String sessionName;

    @Schema(description = "开班日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "学校所在城市", example = "青岛市")
    private String city;

    // 个人信息（脱敏）
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Schema(description = "身份证号（脱敏，前4后4中间*）")
    private String idCardMasked;

    @Schema(description = "手机号（脱敏，显示前3后4）")
    private String phoneMasked;

    // 费用信息
    @Schema(description = "学费总额（元）")
    private BigDecimal totalAmount;

    @Schema(description = "定金（元）")
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "尾款（元）")
    private BigDecimal balanceAmount;

    @Schema(description = "拼团优惠（元，仅成团后生效，已从尾款中抵扣）")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "是否全款报名（定金为 0 或 ≥ 学费，无尾款）")
    private Boolean fullPayment;

    @Schema(description = "已支付金额（元）")
    private BigDecimal paidAmount;

    @Schema(description = "尾款截止日期")
    private LocalDate balanceDueDate;

    // 状态
    @Schema(description = "报名状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enrollmentStatus;

    // 人工处理信息（仅 MANUAL_PROCESSING 状态有值）
    @Schema(description = "人工处理单ID")
    private Long manualTaskId;

    @Schema(description = "人工处理类型（BALANCE_OVERDUE/REFUND_APPLY/REFUND_AMOUNT_CONFIRM/OTHER）")
    private String manualTaskType;

    @Schema(description = "人工处理状态（PENDING/PROCESSING）")
    private String manualTaskStatus;

    @Schema(description = "人工处理原因")
    private String manualTaskReason;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // 订单列表
    @Schema(description = "订单列表")
    private List<OrderItem> orders;

    @Schema(description = "订单信息")
    @Data
    public static class OrderItem {
        @Schema(description = "订单ID")
        private Long id;
        @Schema(description = "订单号")
        private String orderNo;
        @Schema(description = "订单类型（DEPOSIT/BALANCE/FULL）")
        private String orderType;
        @Schema(description = "应付金额（元）")
        private BigDecimal amount;
        @Schema(description = "订单状态（PENDING/PAID/CLOSED/REFUNDED）")
        private String orderStatus;
        @Schema(description = "支付时间")
        private LocalDateTime payTime;
        @Schema(description = "过期时间")
        private LocalDateTime expireTime;
    }

}
