package cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 人工处理单 - 退费审核 Request VO
 *
 * @author Gene Ye
 */
@Schema(description = "管理后台 - 人工处理单退费审核 Request VO")
@Data
public class ManualTaskRefundResolveReqVO {

    @Schema(description = "人工处理单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "人工处理单ID不能为空")
    private Long id;

    @Schema(description = "审核结果（APPROVE 通过 / REJECT 拒绝）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "APPROVE")
    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVE|REJECT", message = "审核结果只能是 APPROVE 或 REJECT")
    private String result;

    @Schema(description = "实际退款金额（元），审核通过时必填", example = "500.00")
    @DecimalMin(value = "0.01", message = "退款金额必须大于 0")
    private BigDecimal refundAmount;

    @Schema(description = "管理员备注，审核通过时填写", example = "同意退款，原路退回")
    @Size(max = 500, message = "管理员备注不能超过 500 个字符")
    private String adminRemark;

    @Schema(description = "拒绝原因，审核拒绝时必填", example = "已开班，不符合退费政策")
    @Size(max = 500, message = "拒绝原因不能超过 500 个字符")
    private String rejectReason;

}
