package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 申请转课 Request VO")
@Data
public class AppTransferApplyReqVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "报名ID不能为空")
    private Long enrollmentId;

    @Schema(description = "转课原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "希望转到下一期班级")
    @NotBlank(message = "转课原因不能为空")
    private String applyReason;

}
