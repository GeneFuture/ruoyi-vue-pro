package cn.iocoder.yudao.module.maritime.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 发起定金支付 Request VO")
@Data
public class AppPayDepositReqVO {

    @Schema(description = "定金订单ID（EnrollmentOrder.id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

}
