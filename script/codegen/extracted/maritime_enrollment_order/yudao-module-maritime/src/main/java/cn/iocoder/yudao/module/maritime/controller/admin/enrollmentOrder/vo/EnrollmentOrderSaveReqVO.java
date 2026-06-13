package cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 报名订单新增/修改 Request VO")
@Data
public class EnrollmentOrderSaveReqVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27778")
    private Long id;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15831")
    @NotNull(message = "报名ID不能为空")
    private Long enrollmentId;

    @Schema(description = "订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）不能为空")
    private String orderNo;

    @Schema(description = "订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）不能为空")
    private String orderType;

    @Schema(description = "应付金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应付金额（元）不能为空")
    private BigDecimal amount;

    @Schema(description = "ruoyi pay模块订单ID", example = "9804")
    private Long payOrderId;

    @Schema(description = "支付渠道（wx_lite等）")
    private String payChannel;

    @Schema(description = "订单状态（PENDING/PAID/CLOSED/REFUNDED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "订单状态（PENDING/PAID/CLOSED/REFUNDED）不能为空")
    private String orderStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "订单过期时间")
    private LocalDateTime expireTime;

}