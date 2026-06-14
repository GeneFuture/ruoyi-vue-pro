package cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 提现税额预览 Response VO")
@Data
public class AppTaxPreviewRespVO {

    @Schema(description = "申请提现金额（元）")
    private BigDecimal applyAmount;
    @Schema(description = "预计代扣税额（元）")
    private BigDecimal taxAmount;
    @Schema(description = "预计实际到账金额（元）")
    private BigDecimal netAmount;
    @Schema(description = "当月已累计佣金（元）")
    private BigDecimal monthIncome;
    @Schema(description = "本次提现后当月累计佣金（元）")
    private BigDecimal monthIncomeAfter;
    @Schema(description = "税额说明")
    private String taxDesc;

}
