package cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 退费申请 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RefundApplyRespVO {

    @Schema(description = "退费申请ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11446")
    @ExcelProperty("退费申请ID")
    private Long id;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "9118")
    @ExcelProperty("报名ID")
    private Long enrollmentId;

    @Schema(description = "关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）", example = "16538")
    @ExcelProperty("关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）")
    private Long orderId;

    @Schema(description = "申请人", requiredMode = Schema.RequiredMode.REQUIRED, example = "20041")
    @ExcelProperty("申请人")
    private Long memberId;

    @Schema(description = "退费原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不对")
    @ExcelProperty("退费原因")
    private String applyReason;

    @Schema(description = "退费金额（元，审核后填写）")
    @ExcelProperty("退费金额（元，审核后填写）")
    private BigDecimal refundAmount;

    @Schema(description = "申请状态（PENDING/APPROVED/REJECTED/REFUNDED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("申请状态（PENDING/APPROVED/REJECTED/REFUNDED）")
    private String applyStatus;

    @Schema(description = "管理员备注", example = "随便")
    @ExcelProperty("管理员备注")
    private String adminRemark;

    @Schema(description = "审核时间")
    @ExcelProperty("审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人（系统用户ID）", example = "28298")
    @ExcelProperty("审核人（系统用户ID）")
    private Long approverId;

    @Schema(description = "ruoyi pay_refund.id（退款发起后填写）", example = "7337")
    @ExcelProperty("ruoyi pay_refund.id（退款发起后填写）")
    private Long payRefundId;

    @Schema(description = "实际退款到账时间")
    @ExcelProperty("实际退款到账时间")
    private LocalDateTime refundTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}