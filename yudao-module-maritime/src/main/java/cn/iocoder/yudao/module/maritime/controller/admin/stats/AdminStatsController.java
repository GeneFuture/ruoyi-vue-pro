package cn.iocoder.yudao.module.maritime.controller.admin.stats;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.*;
import cn.iocoder.yudao.module.maritime.service.stats.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Pattern;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据统计与报表")
@RestController
@RequestMapping("/maritime/stats")
@Validated
public class AdminStatsController {

    @Resource
    private AdminStatsService adminStatsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard 汇总数据")
    @PreAuthorize("@ss.hasPermission('maritime:dashboard:view')")
    public CommonResult<AdminDashboardRespVO> getDashboard(
            @Parameter(description = "查询日期（yyyy-MM-dd），默认今天")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return success(adminStatsService.getDashboard(date));
    }

    @GetMapping("/export-enrollment")
    @Operation(summary = "导出班期报名名单（Excel）")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:export')")
    public void exportEnrollment(
            @Parameter(description = "班期ID", required = true) @RequestParam Long sessionId,
            HttpServletResponse response) throws IOException {
        List<EnrollmentExcelVO> data = adminStatsService.getEnrollmentExportData(sessionId);
        ExcelUtils.write(response, "报名名单.xls", "报名数据", EnrollmentExcelVO.class, data);
    }

    @GetMapping("/export-commission")
    @Operation(summary = "导出佣金发放明细（Excel）")
    @PreAuthorize("@ss.hasPermission('maritime:commission:export')")
    public void exportCommission(
            @Parameter(description = "开始时间（yyyy-MM-dd HH:mm:ss）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "结束时间（yyyy-MM-dd HH:mm:ss）")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @Parameter(description = "佣金状态，不传则查全部") @RequestParam(required = false) String status,
            HttpServletResponse response) throws IOException {
        List<CommissionExcelVO> data = adminStatsService.getCommissionExportData(startDate, endDate, status);
        ExcelUtils.write(response, "佣金明细.xls", "佣金数据", CommissionExcelVO.class, data);
    }

    @GetMapping("/finance-summary")
    @Operation(summary = "月度财务汇总")
    @PreAuthorize("@ss.hasPermission('maritime:finance:view')")
    public CommonResult<FinanceSummaryRespVO> getFinanceSummary(
            @Parameter(description = "目标月份（yyyy-MM）", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return success(adminStatsService.getFinanceSummary(month));
    }

    @GetMapping("/referral-leaderboard")
    @Operation(summary = "推荐人排行榜（Top 20）")
    @PreAuthorize("@ss.hasPermission('maritime:dashboard:view')")
    public CommonResult<List<ReferralLeaderboardVO>> getReferralLeaderboard(
            @Parameter(description = "统计周期：WEEK / MONTH / ALL_TIME", required = true)
            @RequestParam(defaultValue = "MONTH")
            @Pattern(regexp = "^(WEEK|MONTH|ALL_TIME)$", message = "统计周期只允许 WEEK / MONTH / ALL_TIME") String period) {
        return success(adminStatsService.getReferralLeaderboard(period));
    }

}
