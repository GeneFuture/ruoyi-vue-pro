package cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 佣金审核通过 Request VO")
@Data
public class AdminCommissionApproveReqVO {

    @Schema(description = "佣金记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "佣金记录ID不能为空")
    private Long id;

    @Schema(description = "审核备注")
    private String remark;
}
