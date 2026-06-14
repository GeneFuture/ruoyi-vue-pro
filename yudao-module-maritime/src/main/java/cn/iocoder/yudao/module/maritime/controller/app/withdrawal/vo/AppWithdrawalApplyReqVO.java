package cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 申请提现 Request VO")
@Data
public class AppWithdrawalApplyReqVO {

    @Schema(description = "申请提现金额（元，100~1000）", requiredMode = Schema.RequiredMode.REQUIRED, example = "200.00")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "提现渠道（WX_BALANCE）", requiredMode = Schema.RequiredMode.REQUIRED, example = "WX_BALANCE")
    @NotBlank(message = "提现渠道不能为空")
    private String channel;

}
