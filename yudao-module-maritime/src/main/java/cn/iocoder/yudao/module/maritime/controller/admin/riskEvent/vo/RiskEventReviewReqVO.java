package cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 审核风控事件 Request VO")
@Data
public class RiskEventReviewReqVO {

    @Schema(description = "风控事件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "风控事件ID不能为空")
    private Long id;

    @Schema(description = "审核结果（NORMAL正常 / ABNORMAL异常）", requiredMode = Schema.RequiredMode.REQUIRED, example = "NORMAL")
    @NotBlank(message = "审核结果不能为空")
    private String reviewResult;

    @Schema(description = "审核备注", example = "正常推荐行为，解除禁推")
    private String reviewRemark;

}
