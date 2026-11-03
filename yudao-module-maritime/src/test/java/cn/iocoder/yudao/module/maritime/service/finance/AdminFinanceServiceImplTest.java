package cn.iocoder.yudao.module.maritime.service.finance;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.finance.vo.FinanceDailyFlowRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.FINANCE_DATE_RANGE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AdminFinanceServiceImpl} 的单元测试
 */
@Import(AdminFinanceServiceImpl.class)
public class AdminFinanceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminFinanceServiceImpl adminFinanceService;
    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;
    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Test
    public void testGetDailyFlow_success() {
        insertOrder("DEPOSIT", "500.00", "PAID", LocalDateTime.of(2026, 10, 1, 10, 0));
        insertOrder("BALANCE", "3300.00", "REFUNDED", LocalDateTime.of(2026, 10, 2, 11, 0));
        insertOrder("FULL", "3800.00", "PAID", LocalDateTime.of(2026, 10, 2, 12, 0));

        RefundApplyDO refund = RefundApplyDO.builder()
                .enrollmentId(100L).memberId(200L).applyReason("临时有事")
                .refundAmount(new BigDecimal("2600.00")).applyStatus("REFUNDED")
                .refundTime(LocalDateTime.of(2026, 10, 2, 15, 0)).build();
        refundApplyMapper.insert(refund);

        FinanceDailyFlowRespVO result = adminFinanceService.getDailyFlow(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));

        assertEquals(3, result.getDailyList().size());
        assertEquals(0, result.getSummary().getTotalInflow().compareTo(new BigDecimal("7600.00")));
        assertEquals(0, result.getSummary().getDepositAmount().compareTo(new BigDecimal("500.00")));
        assertEquals(0, result.getSummary().getBalanceAmount().compareTo(new BigDecimal("3300.00")));
        assertEquals(0, result.getSummary().getFullAmount().compareTo(new BigDecimal("3800.00")));
        assertEquals(0, result.getSummary().getTotalRefund().compareTo(new BigDecimal("2600.00")));
        assertEquals(0, result.getSummary().getNetFlow().compareTo(new BigDecimal("5000.00")));
        assertEquals(3, result.getSummary().getPaidOrderCount());
        assertEquals(1, result.getSummary().getRefundCount());

        // 10-01 只有定金 500
        assertEquals(0, result.getDailyList().get(0).getNetFlow().compareTo(new BigDecimal("500.00")));
        // 10-02 流入 7100，退款 2600，净流入 4500
        assertEquals(0, result.getDailyList().get(1).getTotalInflow().compareTo(new BigDecimal("7100.00")));
        assertEquals(0, result.getDailyList().get(1).getNetFlow().compareTo(new BigDecimal("4500.00")));
        // 10-03 无流水，补 0
        assertEquals(0, result.getDailyList().get(2).getNetFlow().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testGetDailyFlow_invalidRange() {
        assertServiceException(() -> adminFinanceService.getDailyFlow(
                        LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 1)),
                FINANCE_DATE_RANGE_INVALID);
    }

    private void insertOrder(String type, String amount, String status, LocalDateTime payTime) {
        EnrollmentOrderDO order = EnrollmentOrderDO.builder()
                .enrollmentId(100L).orderNo("ORDER-" + type + "-" + payTime.getDayOfMonth())
                .orderType(type).amount(new BigDecimal(amount)).orderStatus(status)
                .payTime(payTime).build();
        enrollmentOrderMapper.insert(order);
    }

}
