package cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 报名订单分页 Request VO")
@Data
public class EnrollmentOrderPageReqVO extends PageParam {

    @Schema(description = "报名ID", example = "15831")
    private Long enrollmentId;

    @Schema(description = "订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）")
    private String orderNo;

    @Schema(description = "订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）", example = "2")
    private String orderType;

    @Schema(description = "应付金额（元）")
    private BigDecimal amount;

    @Schema(description = "ruoyi pay模块订单ID", example = "9804")
    private Long payOrderId;

    @Schema(description = "支付渠道（wx_lite等）")
    private String payChannel;

    @Schema(description = "订单状态（PENDING/PAID/CLOSED/REFUNDED）", example = "1")
    private String orderStatus;

    @Schema(description = "支付时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] payTime;

    @Schema(description = "订单过期时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}