package cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 佣金记录分页 Request VO")
@Data
public class CommissionRecordPageReqVO extends PageParam {

    @Schema(description = "推荐人", example = "2427")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", example = "23688")
    private Long referredEnrollmentId;

    @Schema(description = "班期ID", example = "23773")
    private Long sessionId;

    @Schema(description = "佣金金额（元）")
    private BigDecimal commissionAmount;

    @Schema(description = "状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)", example = "2")
    private String commissionStatus;

    @Schema(description = "预计结算日期（= 开课日期 + 7天）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expectedSettleDate;

    @Schema(description = "是否风控标记")
    private Boolean isRiskFlagged;

    @Schema(description = "风控检查详情（JSON）")
    private String riskCheckResult;

    @Schema(description = "触发时间（全款支付时）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] triggerTime;

    @Schema(description = "开课7天检查执行时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] settleCheckTime;

    @Schema(description = "审核通过时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] approveTime;

    @Schema(description = "审核人", example = "31155")
    private Long approverId;

    @Schema(description = "审核备注", example = "随便")
    private String approveRemark;

    @Schema(description = "发放时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] payoutTime;

    @Schema(description = "ruoyi pay_transfer.id（调用 PayTransferApi 后填写）", example = "16878")
    private Long payTransferId;

    @Schema(description = "发放失败原因", example = "不对")
    private String failReason;

    @Schema(description = "自动发放失败后已重试次数", example = "20886")
    private Integer retryCount;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}