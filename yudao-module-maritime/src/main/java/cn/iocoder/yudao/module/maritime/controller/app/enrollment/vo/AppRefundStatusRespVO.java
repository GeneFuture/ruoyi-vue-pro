package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 退费状态 Response VO")
@Data
public class AppRefundStatusRespVO {

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "申请状态（PENDING/APPROVED/REJECTED/REFUNDED）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String applyStatus;

    @Schema(description = "退费金额（元），审核通过后才有值")
    private BigDecimal refundAmount;

    @Schema(description = "申请时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "管理员备注（拒绝时含拒绝原因）")
    private String adminRemark;

}
