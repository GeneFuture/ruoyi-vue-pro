package cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 审核通过退费 Request VO")
@Data
public class AdminRefundApproveReqVO {

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "退费申请ID不能为空")
    private Long id;

    @Schema(description = "实际退款金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "500.00")
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    @Schema(description = "管理员备注", example = "同意退款，原路退回")
    private String adminRemark;

}
