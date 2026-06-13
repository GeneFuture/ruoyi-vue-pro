package cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 报名订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EnrollmentOrderRespVO {

    @Schema(description = "订单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27778")
    @ExcelProperty("订单ID")
    private Long id;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "15831")
    @ExcelProperty("报名ID")
    private Long enrollmentId;

    @Schema(description = "订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）")
    private String orderNo;

    @Schema(description = "订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）")
    private String orderType;

    @Schema(description = "应付金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("应付金额（元）")
    private BigDecimal amount;

    @Schema(description = "ruoyi pay模块订单ID", example = "9804")
    @ExcelProperty("ruoyi pay模块订单ID")
    private Long payOrderId;

    @Schema(description = "支付渠道（wx_lite等）")
    @ExcelProperty("支付渠道（wx_lite等）")
    private String payChannel;

    @Schema(description = "订单状态（PENDING/PAID/CLOSED/REFUNDED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("订单状态（PENDING/PAID/CLOSED/REFUNDED）")
    private String orderStatus;

    @Schema(description = "支付时间")
    @ExcelProperty("支付时间")
    private LocalDateTime payTime;

    @Schema(description = "订单过期时间")
    @ExcelProperty("订单过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}