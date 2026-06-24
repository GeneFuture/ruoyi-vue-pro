package cn.iocoder.yudao.module.maritime.controller.admin.stats.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 月度财务汇总 Response VO")
@Data
public class FinanceSummaryRespVO {

    @Schema(description = "统计月份（yyyy-MM）", example = "2026-06")
    private String month;

    // 收款
    @Schema(description = "定金收款总额（元）")
    private BigDecimal totalDeposit;

    @Schema(description = "尾款收款总额（元）")
    private BigDecimal totalBalance;

    @Schema(description = "合计收款（元）")
    private BigDecimal totalRevenue;

    // 退款
    @Schema(description = "退款总额（元）")
    private BigDecimal totalRefund;

    // 净收款
    @Schema(description = "净收款 = 总收款 - 退款（元）")
    private BigDecimal netRevenue;

    // 佣金支出
    @Schema(description = "佣金发放总额（元）")
    private BigDecimal totalCommissionPaid;

    @Schema(description = "佣金发放笔数")
    private Integer commissionPaidCount;

    // 各班期明细
    @Schema(description = "各班期收款明细")
    private List<SessionRevenueSummaryVO> sessionDetails;

    @Data
    public static class SessionRevenueSummaryVO {
        @Schema(description = "班期编号")
        private String sessionCode;

        @Schema(description = "班期名称")
        private String sessionName;

        @Schema(description = "定金收款（元）")
        private BigDecimal depositAmount;

        @Schema(description = "尾款收款（元）")
        private BigDecimal balanceAmount;

        @Schema(description = "退款金额（元）")
        private BigDecimal refundAmount;

        @Schema(description = "净收款（元）")
        private BigDecimal netAmount;

        @Schema(description = "报名人数")
        private Integer enrollmentCount;
    }

}
