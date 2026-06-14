package cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 提现申请 Response VO")
@Data
public class AdminWithdrawalRespVO {

    @Schema(description = "提现申请ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "申请人会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "申请金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal applyAmount;

    @Schema(description = "代扣税额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal taxAmount;

    @Schema(description = "实际到账金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal netAmount;

    @Schema(description = "提现渠道", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channel;

    @Schema(description = "状态（PENDING/PROCESSING/SUCCESS/FAILED/REJECTED）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String applyStatus;

    @Schema(description = "关联转账ID")
    private Long payTransferId;

    @Schema(description = "拒绝原因")
    private String rejectReason;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "到账时间")
    private LocalDateTime successTime;

    @Schema(description = "申请时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
