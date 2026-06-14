package cn.iocoder.yudao.module.maritime.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 查询支付状态 Response VO")
@Data
public class AppPayOrderStatusRespVO {

    @Schema(description = "pay 模块支付单ID", example = "8888")
    private Long payOrderId;

    @Schema(description = "是否已支付成功（true=前端可跳转到报名详情）", example = "true")
    private Boolean paid;

    @Schema(description = "报名ID（支付成功后有值，供前端跳转详情页）", example = "1024")
    private Long enrollmentId;

}
