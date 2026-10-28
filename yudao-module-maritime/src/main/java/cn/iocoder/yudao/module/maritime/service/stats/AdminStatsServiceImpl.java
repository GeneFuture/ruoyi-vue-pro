package cn.iocoder.yudao.module.maritime.service.stats;

import cn.iocoder.yudao.module.maritime.controller.admin.stats.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

@Service
@Validated
@Slf4j
public class AdminStatsServiceImpl implements AdminStatsService {

    private static final int EXPORT_MAX_ROWS = 10_000;

    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;
    @Resource
    private GrouponRecordMapper grouponRecordMapper;
    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardRespVO getDashboard(LocalDate date) {
        LocalDate today = (date != null) ? date : LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

        AdminDashboardRespVO vo = new AdminDashboardRespVO();

        // 今日数据
        vo.setTodayNewEnrollments(enrollmentMapper.countByCreateTimeBetween(todayStart, todayEnd));
        vo.setTodayDepositAmount(
                nullSafe(enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("DEPOSIT", todayStart, todayEnd)));
        vo.setTodayBalanceAmount(
                nullSafe(enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("BALANCE", todayStart, todayEnd)));

        // 本月数据
        vo.setMonthNewEnrollments(enrollmentMapper.countByCreateTimeBetween(monthStart, monthEnd));
        BigDecimal monthDeposit = nullSafe(
                enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("DEPOSIT", monthStart, monthEnd));
        BigDecimal monthBalance = nullSafe(
                enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("BALANCE", monthStart, monthEnd));
        vo.setMonthTotalRevenue(monthDeposit.add(monthBalance));
        vo.setMonthSuccessGroupons(grouponRecordMapper.countSucceededByTimeBetween(monthStart, monthEnd));

        // 班期招生进度
        vo.setOpenSessionProgress(buildSessionProgress());

        // 近7天报名趋势
        vo.setEnrollmentTrend(buildEnrollmentTrend());

        // 快速待办
        vo.setPendingRefundApplies(refundApplyMapper.countByApplyStatus("PENDING"));

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentExcelVO> getEnrollmentExportData(Long sessionId) {
        List<EnrollmentDO> enrollments = enrollmentMapper.selectListBySessionIdForExport(sessionId);
        if (enrollments.size() > EXPORT_MAX_ROWS) {
            throw exception(EXPORT_ROWS_EXCEEDED);
        }
        CourseSessionDO session = courseSessionMapper.selectById(sessionId);
        if (session == null) {
            throw exception(SESSION_NOT_EXISTS);
        }
        String sessionCode = session.getSessionCode();

        List<Long> enrollmentIds = enrollments.stream().map(EnrollmentDO::getId).collect(Collectors.toList());

        // 批量查 PAID 订单，按报名ID + 订单类型分组
        Map<Long, Map<String, BigDecimal>> paidOrderMap = buildPaidOrderMap(enrollmentIds);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<EnrollmentExcelVO> result = new ArrayList<>();
        for (int i = 0; i < enrollments.size(); i++) {
            EnrollmentDO e = enrollments.get(i);
            EnrollmentExcelVO vo = new EnrollmentExcelVO();
            vo.setIndex(i + 1);
            vo.setRealName(maskName(e.getRealName()));
            vo.setIdCard(maskIdCard(e.getIdCard()));
            vo.setPhone(maskPhone(e.getPhone()));
            vo.setSessionCode(sessionCode);
            vo.setCreateTime(e.getCreateTime() != null ? e.getCreateTime().format(dtf) : "");
            vo.setEnrollmentStatus(e.getEnrollmentStatus());

            Map<String, BigDecimal> orderAmounts = paidOrderMap.getOrDefault(e.getId(), Collections.emptyMap());
            vo.setDepositAmount(orderAmounts.getOrDefault("DEPOSIT", BigDecimal.ZERO));
            vo.setBalanceAmount(orderAmounts.getOrDefault("BALANCE", BigDecimal.ZERO));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceSummaryRespVO getFinanceSummary(YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

        FinanceSummaryRespVO vo = new FinanceSummaryRespVO();
        vo.setMonth(month.toString());

        BigDecimal totalDeposit = nullSafe(
                enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("DEPOSIT", start, end));
        BigDecimal totalBalance = nullSafe(
                enrollmentOrderMapper.sumPaidAmountByTypeAndPayTimeBetween("BALANCE", start, end));
        BigDecimal totalRefund = nullSafe(refundApplyMapper.sumRefundedAmountByRefundTimeBetween(start, end));

        vo.setTotalDeposit(totalDeposit);
        vo.setTotalBalance(totalBalance);
        vo.setTotalRevenue(totalDeposit.add(totalBalance));
        vo.setTotalRefund(totalRefund);
        vo.setNetRevenue(totalDeposit.add(totalBalance).subtract(totalRefund));

        // 各班期明细：按月份聚合查询，避免 N+2 per-session 循环，且只统计本月数据
        List<Map<String, Object>> allPaidRows = enrollmentOrderMapper.sumPaidAmountGroupBySessionAndTypeBetween(start, end);
        List<Map<String, Object>> allRefundRows = enrollmentOrderMapper.sumRefundedAmountGroupBySessionBetween(start, end);

        // 按 sessionId 聚合: sessionId → { orderType → amount }
        Map<Long, Map<String, BigDecimal>> paidBySession = new HashMap<>();
        for (Map<String, Object> row : allPaidRows) {
            Long sid = toLong(row.get("sessionId"));
            String orderType = (String) row.get("orderType");
            BigDecimal total = toBigDecimal(row.get("total"));
            paidBySession.computeIfAbsent(sid, k -> new HashMap<>()).put(orderType, total);
        }

        Map<Long, BigDecimal> refundBySession = new HashMap<>();
        for (Map<String, Object> row : allRefundRows) {
            Long sid = toLong(row.get("sessionId"));
            refundBySession.put(sid, toBigDecimal(row.get("total")));
        }

        // 收集所有涉及的班期ID，批量查班期信息
        Set<Long> involvedSessionIds = new HashSet<>(paidBySession.keySet());
        involvedSessionIds.addAll(refundBySession.keySet());

        List<FinanceSummaryRespVO.SessionRevenueSummaryVO> sessionDetails = new ArrayList<>();
        if (!involvedSessionIds.isEmpty()) {
            List<CourseSessionDO> sessions = courseSessionMapper.selectBatchIds(involvedSessionIds);
            for (CourseSessionDO session : sessions) {
                Map<String, BigDecimal> typeAmounts = paidBySession.getOrDefault(session.getId(), Collections.emptyMap());
                BigDecimal depositAmt = typeAmounts.getOrDefault("DEPOSIT", BigDecimal.ZERO);
                BigDecimal balanceAmt = typeAmounts.getOrDefault("BALANCE", BigDecimal.ZERO);
                BigDecimal refundAmt = refundBySession.getOrDefault(session.getId(), BigDecimal.ZERO);

                if (depositAmt.compareTo(BigDecimal.ZERO) == 0
                        && balanceAmt.compareTo(BigDecimal.ZERO) == 0
                        && refundAmt.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }

                FinanceSummaryRespVO.SessionRevenueSummaryVO detail = new FinanceSummaryRespVO.SessionRevenueSummaryVO();
                detail.setSessionCode(session.getSessionCode());
                detail.setSessionName(session.getSessionName());
                detail.setDepositAmount(depositAmt);
                detail.setBalanceAmount(balanceAmt);
                detail.setRefundAmount(refundAmt);
                detail.setNetAmount(depositAmt.add(balanceAmt).subtract(refundAmt));
                detail.setEnrollmentCount(session.getEnrolledCount());
                sessionDetails.add(detail);
            }
        }
        vo.setSessionDetails(sessionDetails);
        return vo;
    }

    // ── 私有工具方法 ────────────────────────────────────────────────────────────

    private List<AdminDashboardRespVO.SessionProgressVO> buildSessionProgress() {
        List<CourseSessionDO> openSessions = courseSessionMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<CourseSessionDO>()
                        .eq(CourseSessionDO::getSessionStatus, "OPEN")
                        .orderByAsc(CourseSessionDO::getStartDate));
        if (openSessions.isEmpty()) return Collections.emptyList();

        List<Long> sessionIds = openSessions.stream().map(CourseSessionDO::getId).collect(Collectors.toList());

        // 批量查各班期已完成报名数，避免 N+1
        Map<Long, Integer> completedCountMap = new HashMap<>();
        for (Map<String, Object> row : enrollmentMapper.countCompletedGroupBySessionIds(sessionIds)) {
            Long sid = toLong(row.get("sessionId"));
            Object cnt = row.get("cnt");
            if (sid != null && cnt != null) completedCountMap.put(sid, ((Number) cnt).intValue());
        }

        // 批量查各班期已付金额，避免 N+1
        Map<Long, BigDecimal> revenueBySession = new HashMap<>();
        for (Map<String, Object> row : enrollmentOrderMapper.sumPaidAmountGroupBySessionAndTypeForSessionIds(sessionIds)) {
            Long sid = toLong(row.get("sessionId"));
            if (sid != null) revenueBySession.merge(sid, toBigDecimal(row.get("total")), BigDecimal::add);
        }

        List<AdminDashboardRespVO.SessionProgressVO> result = new ArrayList<>();
        for (CourseSessionDO session : openSessions) {
            AdminDashboardRespVO.SessionProgressVO progressVO = new AdminDashboardRespVO.SessionProgressVO();
            progressVO.setSessionCode(session.getSessionCode());
            progressVO.setSessionName(session.getSessionName());
            progressVO.setStartDate(session.getStartDate());
            progressVO.setMaxStudents(session.getMaxStudents());
            progressVO.setEnrolledCount(session.getEnrolledCount());
            progressVO.setCompletedCount(completedCountMap.getOrDefault(session.getId(), 0));
            progressVO.setConfirmedRevenue(revenueBySession.getOrDefault(session.getId(), BigDecimal.ZERO));
            result.add(progressVO);
        }
        return result;
    }

    private List<AdminDashboardRespVO.DailyStatVO> buildEnrollmentTrend() {
        LocalDate today = LocalDate.now();
        LocalDateTime since = today.minusDays(6).atStartOfDay();
        List<Map<String, Object>> rawList = enrollmentMapper.selectDailyEnrollmentCounts(since);

        // 生成完整7天日期集，缺失天补零；null-safe 处理 MyBatis 列名大小写差异
        Map<String, Integer> countByDate = rawList.stream().collect(
                Collectors.toMap(
                        row -> String.valueOf(row.get("stat_date")),
                        row -> {
                            Object cnt = row.get("cnt");
                            return cnt == null ? 0 : ((Number) cnt).intValue();
                        }
                )
        );

        List<AdminDashboardRespVO.DailyStatVO> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String dateStr = today.minusDays(i).toString();
            AdminDashboardRespVO.DailyStatVO stat = new AdminDashboardRespVO.DailyStatVO();
            stat.setDate(dateStr);
            stat.setCount(countByDate.getOrDefault(dateStr, 0));
            result.add(stat);
        }
        return result;
    }

    private Map<Long, Map<String, BigDecimal>> buildPaidOrderMap(List<Long> enrollmentIds) {
        if (enrollmentIds.isEmpty()) return Collections.emptyMap();
        List<EnrollmentOrderDO> paidOrders = enrollmentOrderMapper.selectPaidByEnrollmentIds(enrollmentIds);
        Map<Long, Map<String, BigDecimal>> result = new HashMap<>();
        for (EnrollmentOrderDO order : paidOrders) {
            result.computeIfAbsent(order.getEnrollmentId(), k -> new HashMap<>())
                    .put(order.getOrderType(), order.getAmount());
        }
        return result;
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal bd) return bd;
        return new BigDecimal(obj.toString());
    }

    private static Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        return ((Number) obj).longValue();
    }

    private static String maskName(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.charAt(0) + "*".repeat(Math.max(1, name.length() - 1));
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return "***";
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

}
