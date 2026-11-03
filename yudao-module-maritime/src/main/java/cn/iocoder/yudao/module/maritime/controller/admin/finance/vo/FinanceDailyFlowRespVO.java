package cn.iocoder.yudao.module.maritime.controller.admin.finance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 管理后台 - 资金流水 Response VO
 *
 * @author Gene Ye
 */
@Schema(description = "管理后台 - 资金流水 Response VO")
@Data
public class FinanceDailyFlowRespVO {

    @Schema(description = "资金汇总")
    private SummaryVO summary;

    @Schema(description = "每日资金流")
    private List<DailyFlowVO> dailyList;

    @Schema(description = "资金汇总")
    @Data
    public static class SummaryVO {

        @Schema(description = "总流入（元）", example = "126000.00")
        private BigDecimal totalInflow;

        @Schema(description = "定金收入（元）", example = "32000.00")
        private BigDecimal depositAmount;

        @Schema(description = "尾款收入（元）", example = "84000.00")
        private BigDecimal balanceAmount;

        @Schema(description = "全款收入（元）", example = "10000.00")
        private BigDecimal fullAmount;

        @Schema(description = "退款支出（元）", example = "2600.00")
        private BigDecimal totalRefund;

        @Schema(description = "净流入（元）", example = "123400.00")
        private BigDecimal netFlow;

        @Schema(description = "日均净流入（元）", example = "4113.33")
        private BigDecimal avgDailyNet;

        @Schema(description = "支付订单数", example = "86")
        private Integer paidOrderCount;

        @Schema(description = "退款笔数", example = "3")
        private Integer refundCount;

    }

    @Schema(description = "每日资金流")
    @Data
    public static class DailyFlowVO {

        @Schema(description = "日期", example = "2026-10-01")
        private LocalDate date;

        @Schema(description = "定金收入（元）", example = "5000.00")
        private BigDecimal depositAmount;

        @Schema(description = "尾款收入（元）", example = "12000.00")
        private BigDecimal balanceAmount;

        @Schema(description = "全款收入（元）", example = "0.00")
        private BigDecimal fullAmount;

        @Schema(description = "总流入（元）", example = "17000.00")
        private BigDecimal totalInflow;

        @Schema(description = "退款支出（元）", example = "0.00")
        private BigDecimal refundAmount;

        @Schema(description = "净流入（元）", example = "17000.00")
        private BigDecimal netFlow;

        @Schema(description = "支付订单数", example = "12")
        private Integer paidOrderCount;

        @Schema(description = "退款笔数", example = "0")
        private Integer refundCount;

    }

}
