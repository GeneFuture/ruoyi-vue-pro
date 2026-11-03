package cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 人工处理单处理 Request VO")
@Data
public class ManualTaskResolveReqVO {

    @Schema(description = "人工处理单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "人工处理单ID不能为空")
    private Long id;

    @Schema(description = "处理结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "学员已补缴尾款")
    @NotBlank(message = "处理结果不能为空")
    @Size(max = 500, message = "处理结果不能超过 500 个字符")
    private String handleResult;

    @Schema(description = "处理后的报名状态；退费类任务请使用 /resolve-refund 审核", example = "PAID_OFF")
    private String targetEnrollmentStatus;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

}
