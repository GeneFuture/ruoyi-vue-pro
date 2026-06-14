package cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 拒绝退费 Request VO")
@Data
public class AdminRefundRejectReqVO {

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "退费申请ID不能为空")
    private Long id;

    @Schema(description = "拒绝原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "已开班，不符合退费政策")
    @NotBlank(message = "拒绝原因不能为空")
    private String rejectReason;

}
