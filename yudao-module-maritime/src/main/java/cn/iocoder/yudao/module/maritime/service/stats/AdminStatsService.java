package cn.iocoder.yudao.module.maritime.service.stats;

import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.AdminDashboardRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.EnrollmentExcelVO;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.FinanceSummaryRespVO;

import java.time.LocalDate;
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
     * 获取月度财务汇总
     *
     * @param month 目标月份
     */
    FinanceSummaryRespVO getFinanceSummary(YearMonth month);

}
