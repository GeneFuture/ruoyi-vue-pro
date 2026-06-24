package cn.iocoder.yudao.module.maritime.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 封禁/解封会员 Request VO")
@Data
public class AdminMemberBanReqVO {

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @Schema(description = "是否禁止推荐（true=禁推，false=解除禁推）", example = "true")
    private Boolean referralBanned;

    @Schema(description = "封禁原因", example = "批量推荐刷单")
    private String reason;

}
