package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "小程序 - 创建报名 Response VO")
@Data
public class AppEnrollmentCreateRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long enrollmentId;

    @Schema(description = "定金订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long orderId;

    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "OD20240101000001")
    private String orderNo;

    @Schema(description = "应付定金（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal depositAmount;

    @Schema(description = "订单过期时间（30分钟后）", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expireTime;

}
