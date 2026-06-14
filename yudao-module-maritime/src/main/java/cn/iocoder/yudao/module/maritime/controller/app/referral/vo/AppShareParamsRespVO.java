package cn.iocoder.yudao.module.maritime.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 分享参数 Response VO")
@Data
public class AppShareParamsRespVO {

    @Schema(description = "小程序分享路径", requiredMode = Schema.RequiredMode.REQUIRED)
    private String path;

    @Schema(description = "分享标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "推荐码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String referralCode;
}
