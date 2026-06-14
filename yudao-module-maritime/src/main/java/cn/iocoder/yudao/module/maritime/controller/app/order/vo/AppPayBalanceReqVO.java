package cn.iocoder.yudao.module.maritime.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 发起尾款支付 Request VO")
@Data
public class AppPayBalanceReqVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "报名ID不能为空")
    private Long enrollmentId;

}
