package cn.iocoder.yudao.module.maritime.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 我的推荐信息 Response VO")
@Data
public class AppReferralInfoRespVO {

    @Schema(description = "推荐码", example = "REF-A1B2C3")
    private String referralCode;

    @Schema(description = "是否已获得推荐权（支付定金后开启）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasReferralRight;

    @Schema(description = "小程序分享路径", example = "/pages/course/list?ref=REF-A1B2C3")
    private String sharePath;

    @Schema(description = "累计推荐人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long totalReferralCount;

    @Schema(description = "推荐成功人数（全款支付且开课无退费）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long successCount;

    @Schema(description = "待发放佣金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal pendingCommissionAmount;

    @Schema(description = "已发放佣金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal paidCommissionAmount;
}
