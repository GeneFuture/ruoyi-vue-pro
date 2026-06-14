package cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 提现记录 Response VO")
@Data
public class AppWithdrawalRecordRespVO {

    @Schema(description = "提现ID")
    private Long id;
    @Schema(description = "申请金额（元）")
    private BigDecimal applyAmount;
    @Schema(description = "代扣税额（元）")
    private BigDecimal taxAmount;
    @Schema(description = "实际到账金额（元）")
    private BigDecimal netAmount;
    @Schema(description = "提现渠道")
    private String channel;
    @Schema(description = "状态（PENDING/PROCESSING/SUCCESS/FAILED/REJECTED）")
    private String applyStatus;
    @Schema(description = "到账时间")
    private LocalDateTime successTime;
    @Schema(description = "申请时间")
    private LocalDateTime createTime;
    @Schema(description = "失败或拒绝原因")
    private String failReason;

}
