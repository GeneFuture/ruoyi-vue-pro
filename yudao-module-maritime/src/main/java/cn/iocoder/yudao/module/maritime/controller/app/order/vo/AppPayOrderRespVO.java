package cn.iocoder.yudao.module.maritime.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 发起定金支付 Response VO")
@Data
public class AppPayOrderRespVO {

    @Schema(description = "pay 模块支付单ID（前端凭此调用 /pay/order/submit）", example = "8888")
    private Long payOrderId;

    @Schema(description = "应付金额（元）", example = "500.00")
    private java.math.BigDecimal amount;

}
