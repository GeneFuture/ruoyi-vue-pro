package cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 佣金记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CommissionRecordRespVO {

    @Schema(description = "佣金记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17524")
    @ExcelProperty("佣金记录ID")
    private Long id;

    @Schema(description = "推荐人", requiredMode = Schema.RequiredMode.REQUIRED, example = "2427")
    @ExcelProperty("推荐人")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23688")
    @ExcelProperty("被推荐人的报名ID")
    private Long referredEnrollmentId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23773")
    @ExcelProperty("班期ID")
    private Long sessionId;

    @Schema(description = "佣金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("佣金金额（元）")
    private BigDecimal commissionAmount;

    @Schema(description = "状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)")
    private String commissionStatus;

    @Schema(description = "预计结算日期（= 开课日期 + 7天）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预计结算日期（= 开课日期 + 7天）")
    private LocalDate expectedSettleDate;

    @Schema(description = "是否风控标记", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否风控标记")
    private Boolean isRiskFlagged;

    @Schema(description = "风控检查详情（JSON）")
    @ExcelProperty("风控检查详情（JSON）")
    private String riskCheckResult;

    @Schema(description = "触发时间（全款支付时）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("触发时间（全款支付时）")
    private LocalDateTime triggerTime;

    @Schema(description = "开课7天检查执行时间")
    @ExcelProperty("开课7天检查执行时间")
    private LocalDateTime settleCheckTime;

    @Schema(description = "审核通过时间")
    @ExcelProperty("审核通过时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人", example = "31155")
    @ExcelProperty("审核人")
    private Long approverId;

    @Schema(description = "审核备注", example = "随便")
    @ExcelProperty("审核备注")
    private String approveRemark;

    @Schema(description = "发放时间")
    @ExcelProperty("发放时间")
    private LocalDateTime payoutTime;

    @Schema(description = "ruoyi pay_transfer.id（调用 PayTransferApi 后填写）", example = "16878")
    @ExcelProperty("ruoyi pay_transfer.id（调用 PayTransferApi 后填写）")
    private Long payTransferId;

    @Schema(description = "发放失败原因", example = "不对")
    @ExcelProperty("发放失败原因")
    private String failReason;

    @Schema(description = "自动发放失败后已重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20886")
    @ExcelProperty("自动发放失败后已重试次数")
    private Integer retryCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}