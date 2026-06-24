package cn.iocoder.yudao.module.maritime.controller.admin.stats.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - Dashboard 数据 Response VO")
@Data
public class AdminDashboardRespVO {

    // ── 今日数据 ──
    @Schema(description = "今日新报名数")
    private Integer todayNewEnrollments;

    @Schema(description = "今日收款（定金，元）")
    private BigDecimal todayDepositAmount;

    @Schema(description = "今日收款（尾款，元）")
    private BigDecimal todayBalanceAmount;

    // ── 本月数据 ──
    @Schema(description = "本月新报名数")
    private Integer monthNewEnrollments;

    @Schema(description = "本月总收款（元）")
    private BigDecimal monthTotalRevenue;

    @Schema(description = "本月佣金发放总额（元）")
    private BigDecimal monthCommissionPaid;

    @Schema(description = "本月成团数")
    private Integer monthSuccessGroupons;

    @Schema(description = "本月推荐成功数（ACTIVE 推荐关系）")
    private Integer monthReferralCount;

    // ── 班期招生进度（当前开放报名的班期）──
    @Schema(description = "开放班期招生进度")
    private List<SessionProgressVO> openSessionProgress;

    // ── 趋势图（近7天报名量）──
    @Schema(description = "近7天每日报名趋势")
    private List<DailyStatVO> enrollmentTrend;

    // ── 快速待办 ──
    @Schema(description = "待审核佣金数")
    private Integer pendingCommissionReviews;

    @Schema(description = "待处理退费申请数")
    private Integer pendingRefundApplies;

    @Schema(description = "发放失败佣金数")
    private Integer failedPayouts;

    @Schema(description = "未处理风控事件数")
    private Integer pendingRiskEvents;

    // ── 嵌套 VO ──

    @Data
    public static class SessionProgressVO {
        @Schema(description = "班期编号")
        private String sessionCode;

        @Schema(description = "班期名称")
        private String sessionName;

        @Schema(description = "开班日期")
        private LocalDate startDate;

        @Schema(description = "最大招生人数")
        private Integer maxStudents;

        @Schema(description = "已报名人数（缓存）")
        private Integer enrolledCount;

        @Schema(description = "已完成报名数（尾款已付 COMPLETED）")
        private Integer completedCount;

        @Schema(description = "已确认收款金额（元）")
        private BigDecimal confirmedRevenue;
    }

    @Data
    public static class DailyStatVO {
        @Schema(description = "日期（yyyy-MM-dd）")
        private String date;

        @Schema(description = "新报名数")
        private Integer count;
    }

}
