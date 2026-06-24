package cn.iocoder.yudao.module.maritime.service.stats;

import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.AdminDashboardRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.CommissionExcelVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.EnrollmentExcelVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.FinanceSummaryRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.ReferralLeaderboardVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface AdminStatsService {

    /**
     * 获取 Dashboard 汇总数据
     *
     * @param date 查询日期（null 表示今天）
     */
    AdminDashboardRespVO getDashboard(LocalDate date);

    /**
     * 获取班期报名名单（供 Excel 导出）
     *
     * @param sessionId 班期ID
     */
    List<EnrollmentExcelVO> getEnrollmentExportData(Long sessionId);

    /**
     * 获取佣金明细（供 Excel 导出）
     *
     * @param startDate 开始时间（含）
     * @param endDate   结束时间（含）
     * @param status    佣金状态，null 表示全部
     */
    List<CommissionExcelVO> getCommissionExportData(LocalDateTime startDate, LocalDateTime endDate, String status);

    /**
     * 获取月度财务汇总
     *
     * @param month 目标月份
     */
    FinanceSummaryRespVO getFinanceSummary(YearMonth month);

    /**
     * 获取推荐人排行榜（Top 20）
     *
     * @param period WEEK / MONTH / ALL_TIME
     */
    List<ReferralLeaderboardVO> getReferralLeaderboard(String period);

}
