package cn.iocoder.yudao.module.maritime.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 佣金账户汇总 Response VO")
@Data
public class AppCommissionAccountRespVO {

    @Schema(description = "累计佣金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalAmount;

    @Schema(description = "已发放佣金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal paidAmount;

    @Schema(description = "待发放佣金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal pendingAmount;

    @Schema(description = "冻结中佣金（元，退费保护期）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal frozenAmount;

    @Schema(description = "累计推荐人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalReferralCount;

    @Schema(description = "是否已获得推荐权（支付定金后开启）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean hasReferralRight;
}
