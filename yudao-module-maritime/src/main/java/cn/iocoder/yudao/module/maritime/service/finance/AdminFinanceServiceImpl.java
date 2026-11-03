package cn.iocoder.yudao.module.maritime.service.finance;

import cn.iocoder.yudao.module.maritime.controller.admin.finance.vo.FinanceDailyFlowRespVO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.FINANCE_DATE_RANGE_INVALID;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.FINANCE_DATE_RANGE_TOO_LARGE;

/**
 * 管理后台 - 资金流水 Service 实现类
 *
 * 资金口径：
 *   * 流入 = 状态为 PAID / REFUNDED 的支付订单金额，按 pay_time 归日；
 *   * 流出 = 已退款退费申请的退款金额，按 refund_time 归日；
 *   * 退款订单的原始收款仍计入支付日流入，退款单独计入退款日流出。
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminFinanceServiceImpl implements AdminFinanceService {

    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 366;

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Override
    public FinanceDailyFlowRespVO getDailyFlow(LocalDate startDate, LocalDate endDate) {
        // 1. 默认近 30 天，并校验范围
        LocalDate today = LocalDate.now();
        if (endDate == null) {
            endDate = today;
        }
        if (startDate == null) {
            startDate = endDate.minusDays(DEFAULT_DAYS - 1L);
        }
        if (startDate.isAfter(endDate)) {
            throw exception(FINANCE_DATE_RANGE_INVALID);
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_DAYS) {
            throw exception(FINANCE_DATE_RANGE_TOO_LARGE);
        }

        // 2. 先铺满日期，保证没有流水的日期也返回 0
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
        Map<LocalDate, FinanceDailyFlowRespVO.DailyFlowVO> dailyMap = new TreeMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dailyMap.put(date, emptyDaily(date));
        }

        // 3. 按日汇总流入与退款
        for (Map<String, Object> row : enrollmentOrderMapper.sumPaidAmountGroupByDateBetween(start, endExclusive)) {
            LocalDate date = toLocalDate(row.get("statDate"));
            FinanceDailyFlowRespVO.DailyFlowVO daily = dailyMap.get(date);
            if (daily == null) {
                continue;
            }
            daily.setDepositAmount(toBigDecimal(row.get("depositAmount")));
            daily.setBalanceAmount(toBigDecimal(row.get("balanceAmount")));
            daily.setFullAmount(toBigDecimal(row.get("fullAmount")));
            daily.setTotalInflow(toBigDecimal(row.get("totalInflow")));
            daily.setPaidOrderCount(toInt(row.get("paidOrderCount")));
        }
        for (Map<String, Object> row : refundApplyMapper.sumRefundedAmountGroupByDateBetween(start, endExclusive)) {
            LocalDate date = toLocalDate(row.get("statDate"));
            FinanceDailyFlowRespVO.DailyFlowVO daily = dailyMap.get(date);
            if (daily == null) {
                continue;
            }
            daily.setRefundAmount(toBigDecimal(row.get("refundAmount")));
            daily.setRefundCount(toInt(row.get("refundCount")));
        }

        // 4. 计算每天净流入 & 汇总
        FinanceDailyFlowRespVO.SummaryVO summary = new FinanceDailyFlowRespVO.SummaryVO();
        summary.setTotalInflow(BigDecimal.ZERO);
        summary.setDepositAmount(BigDecimal.ZERO);
        summary.setBalanceAmount(BigDecimal.ZERO);
        summary.setFullAmount(BigDecimal.ZERO);
        summary.setTotalRefund(BigDecimal.ZERO);
        summary.setNetFlow(BigDecimal.ZERO);
        summary.setPaidOrderCount(0);
        summary.setRefundCount(0);

        List<FinanceDailyFlowRespVO.DailyFlowVO> dailyList = new ArrayList<>(dailyMap.values());
        for (FinanceDailyFlowRespVO.DailyFlowVO daily : dailyList) {
            daily.setNetFlow(daily.getTotalInflow().subtract(daily.getRefundAmount()));
            summary.setTotalInflow(summary.getTotalInflow().add(daily.getTotalInflow()));
            summary.setDepositAmount(summary.getDepositAmount().add(daily.getDepositAmount()));
            summary.setBalanceAmount(summary.getBalanceAmount().add(daily.getBalanceAmount()));
            summary.setFullAmount(summary.getFullAmount().add(daily.getFullAmount()));
            summary.setTotalRefund(summary.getTotalRefund().add(daily.getRefundAmount()));
            summary.setNetFlow(summary.getNetFlow().add(daily.getNetFlow()));
            summary.setPaidOrderCount(summary.getPaidOrderCount() + daily.getPaidOrderCount());
            summary.setRefundCount(summary.getRefundCount() + daily.getRefundCount());
        }
        summary.setAvgDailyNet(summary.getNetFlow()
                .divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP));

        FinanceDailyFlowRespVO result = new FinanceDailyFlowRespVO();
        result.setSummary(summary);
        result.setDailyList(dailyList);
        return result;
    }

    private FinanceDailyFlowRespVO.DailyFlowVO emptyDaily(LocalDate date) {
        FinanceDailyFlowRespVO.DailyFlowVO daily = new FinanceDailyFlowRespVO.DailyFlowVO();
        daily.setDate(date);
        daily.setDepositAmount(BigDecimal.ZERO);
        daily.setBalanceAmount(BigDecimal.ZERO);
        daily.setFullAmount(BigDecimal.ZERO);
        daily.setTotalInflow(BigDecimal.ZERO);
        daily.setRefundAmount(BigDecimal.ZERO);
        daily.setNetFlow(BigDecimal.ZERO);
        daily.setPaidOrderCount(0);
        daily.setRefundCount(0);
        return daily;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return new BigDecimal(value.toString());
    }

    private static Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        return LocalDate.parse(value.toString().substring(0, 10));
    }

}
