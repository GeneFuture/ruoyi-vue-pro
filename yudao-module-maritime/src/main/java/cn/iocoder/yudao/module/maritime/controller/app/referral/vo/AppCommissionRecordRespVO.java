package cn.iocoder.yudao.module.maritime.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 佣金记录 Response VO")
@Data
public class AppCommissionRecordRespVO {

    @Schema(description = "佣金记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "佣金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commissionAmount;

    @Schema(description = "佣金状态", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "WAITING_FOR_CLASS / PENDING_REVIEW / PENDING_PAYOUT / PAID / FROZEN / REJECTED")
    private String commissionStatus;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    @Schema(description = "班期名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionName;

    @Schema(description = "触发时间（全款支付时）", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime triggerTime;

    @Schema(description = "预计结算日期（开课日期 + 7天）")
    private LocalDate expectedSettleDate;
}
