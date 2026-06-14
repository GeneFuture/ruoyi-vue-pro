package cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 申请提现 Response VO")
@Data
public class AppWithdrawalApplyRespVO {

    @Schema(description = "提现申请ID")
    private Long applyId;
    @Schema(description = "申请金额（元）")
    private BigDecimal applyAmount;
    @Schema(description = "代扣税额（元）")
    private BigDecimal taxAmount;
    @Schema(description = "实际到账金额（元）")
    private BigDecimal netAmount;
    @Schema(description = "预计到账说明")
    private String estimatedArrivalDesc;

}
