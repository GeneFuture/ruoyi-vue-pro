package cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 退费申请新增/修改 Request VO")
@Data
public class RefundApplySaveReqVO {

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11446")
    private Long id;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9118")
    @NotNull(message = "报名ID不能为空")
    private Long enrollmentId;

    @Schema(description = "关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）", example = "16538")
    private Long orderId;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED, example = "20041")
    @NotNull(message = "申请人不能为空")
    private Long memberId;

    @Schema(description = "退费原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不对")
    @NotEmpty(message = "退费原因不能为空")
    private String applyReason;

    @Schema(description = "退费金额（元，审核后填写）")
    private BigDecimal refundAmount;

    @Schema(description = "申请状态（PENDING/APPROVED/REJECTED/REFUNDED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "申请状态（PENDING/APPROVED/REJECTED/REFUNDED）不能为空")
    private String applyStatus;

    @Schema(description = "管理员备注", example = "随便")
    private String adminRemark;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人（系统用户ID）", example = "28298")
    private Long approverId;

    @Schema(description = "ruoyi pay_refund.id（退款发起后填写）", example = "7337")
    private Long payRefundId;

    @Schema(description = "实际退款到账时间")
    private LocalDateTime refundTime;

}