package cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人工处理单 - 退费审核详情 Response VO
 *
 * 退费申请已合并到人工处理，详情同时返回人工单与退费申请信息。
 *
 * @author Gene Ye
 */
@Schema(description = "管理后台 - 人工处理单退费审核详情 Response VO")
@Data
public class ManualTaskRefundDetailRespVO {

    @Schema(description = "人工处理单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "人工处理单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "MT202610270001")
    private String taskNo;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REFUND_APPLY")
    private String taskType;

    @Schema(description = "人工单状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    private String taskStatus;

    @Schema(description = "进入人工的原因", example = "学员申请退费：临时有事")
    private String taskReason;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long enrollmentId;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "247")
    private Long memberId;

    @Schema(description = "关联支付订单ID", example = "2")
    private Long orderId;

    @Schema(description = "关联支付订单号", example = "DEPOSIT-202610270001")
    private String orderNo;

    @Schema(description = "关联支付订单金额（元）", example = "500.00")
    private BigDecimal orderAmount;

    @Schema(description = "关联支付订单状态", example = "PAID")
    private String orderStatus;

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long refundApplyId;

    @Schema(description = "退费申请状态（PENDING/APPROVED/REJECTED/REFUNDED）", example = "PENDING")
    private String applyStatus;

    @Schema(description = "退费原因", example = "临时有事")
    private String applyReason;

    @Schema(description = "申请退款金额（元）", example = "500.00")
    private BigDecimal refundAmount;

    @Schema(description = "管理员备注", example = "同意退款，原路退回")
    private String adminRemark;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人（系统用户ID）", example = "1")
    private Long approverId;

    @Schema(description = "实际退款到账时间")
    private LocalDateTime refundTime;

    @Schema(description = "退费申请创建时间")
    private LocalDateTime createTime;

}
