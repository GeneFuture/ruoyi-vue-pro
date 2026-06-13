package cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 风控黑名单新增/修改 Request VO")
@Data
public class RiskBlacklistSaveReqVO {

    @Schema(description = "黑名单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "970")
    private Long id;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "手机号不能为空")
    private String phone;

    @Schema(description = "加入黑名单原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不好")
    @NotEmpty(message = "加入黑名单原因不能为空")
    private String reason;

    @Schema(description = "操作人（管理员）", example = "2478")
    private Long operatorId;

}