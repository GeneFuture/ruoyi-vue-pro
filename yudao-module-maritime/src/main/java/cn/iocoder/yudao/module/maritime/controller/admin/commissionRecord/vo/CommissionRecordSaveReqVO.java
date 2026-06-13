package cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 佣金记录新增/修改 Request VO")
@Data
public class CommissionRecordSaveReqVO {

    @Schema(description = "佣金记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17524")
    private Long id;

    @Schema(description = "推荐人", requiredMode = Schema.RequiredMode.REQUIRED, example = "2427")
    @NotNull(message = "推荐人不能为空")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23688")
    @NotNull(message = "被推荐人的报名ID不能为空")
    private Long referredEnrollmentId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23773")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "佣金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "佣金金额（元）不能为空")
    private BigDecimal commissionAmount;

    @Schema(description = "状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)不能为空")
    private String commissionStatus;

    @Schema(description = "预计结算日期（= 开课日期 + 7天）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预计结算日期（= 开课日期 + 7天）不能为空")
    private LocalDate expectedSettleDate;

    @Schema(description = "是否风控标记", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否风控标记不能为空")
    private Boolean isRiskFlagged;

    @Schema(description = "风控检查详情（JSON）")
    private String riskCheckResult;

    @Schema(description = "触发时间（全款支付时）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "触发时间（全款支付时）不能为空")
    private LocalDateTime triggerTime;

    @Schema(description = "开课7天检查执行时间")
    private LocalDateTime settleCheckTime;

    @Schema(description = "审核通过时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人", example = "31155")
    private Long approverId;

    @Schema(description = "审核备注", example = "随便")
    private String approveRemark;

    @Schema(description = "发放时间")
    private LocalDateTime payoutTime;

    @Schema(description = "ruoyi pay_transfer.id（调用 PayTransferApi 后填写）", example = "16878")
    private Long payTransferId;

    @Schema(description = "发放失败原因", example = "不对")
    private String failReason;

    @Schema(description = "自动发放失败后已重试次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20886")
    @NotNull(message = "自动发放失败后已重试次数不能为空")
    private Integer retryCount;

}