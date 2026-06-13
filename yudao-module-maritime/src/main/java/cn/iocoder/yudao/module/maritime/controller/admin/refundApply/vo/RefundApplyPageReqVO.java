package cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 退费申请分页 Request VO")
@Data
public class RefundApplyPageReqVO extends PageParam {

    @Schema(description = "报名ID", example = "9118")
    private Long enrollmentId;

    @Schema(description = "关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）", example = "16538")
    private Long orderId;

    @Schema(description = "申请人", example = "20041")
    private Long memberId;

    @Schema(description = "退费原因", example = "不对")
    private String applyReason;

    @Schema(description = "退费金额（元，审核后填写）")
    private BigDecimal refundAmount;

    @Schema(description = "申请状态（PENDING/APPROVED/REJECTED/REFUNDED）", example = "2")
    private String applyStatus;

    @Schema(description = "管理员备注", example = "随便")
    private String adminRemark;

    @Schema(description = "审核时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] approveTime;

    @Schema(description = "审核人（系统用户ID）", example = "28298")
    private Long approverId;

    @Schema(description = "ruoyi pay_refund.id（退款发起后填写）", example = "7337")
    private Long payRefundId;

    @Schema(description = "实际退款到账时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] refundTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}