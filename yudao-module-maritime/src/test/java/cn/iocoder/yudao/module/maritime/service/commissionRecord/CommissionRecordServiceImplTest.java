package cn.iocoder.yudao.module.maritime.service.commissionRecord;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.mq.event.CommissionPaidEvent;
import cn.iocoder.yudao.module.maritime.mq.event.ReferralSuccessEvent;
import cn.iocoder.yudao.module.maritime.service.commissionAccount.CommissionAccountService;
import cn.iocoder.yudao.module.maritime.service.tax.TaxService;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link CommissionRecordServiceImpl} 的单元测试类
 *
 * 重点覆盖：佣金触发幂等、审核 CAS 状态机、发放成功/失败路径、T09 事件发布时机
 *
 * 注：事件发布通过 {@code @RecordApplicationEvents} + {@link ApplicationEvents} 断言，
 * 而非 mock ApplicationEventPublisher——后者会被 Spring 容器自身的 resolvableDependency
 * 抢先注入，导致 Mock 永远收不到调用。
 */
@Import(CommissionRecordServiceImpl.class)
@RecordApplicationEvents
public class CommissionRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CommissionRecordServiceImpl commissionRecordService;
    @Resource
    private CommissionRecordMapper commissionRecordMapper;
    @Resource
    private ReferralRelationMapper referralRelationMapper;
    @Resource
    private CourseSessionMapper courseSessionMapper;
    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;
    @Resource
    private cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper enrollmentMapper;
    @Autowired
    private ApplicationEvents events;

    @MockitoBean
    private CommissionAccountService commissionAccountService;
    @MockitoBean
    private TaxService taxService;
    @MockitoBean
    private MemberUserService memberUserService;
    @MockitoBean
    private PayTransferApi payTransferApi;

    private ReferralRelationDO insertActiveRelation(Long referrerMemberId, Long referredEnrollmentId, Long sessionId) {
        ReferralRelationDO relation = ReferralRelationDO.builder()
                .referrerMemberId(referrerMemberId)
                .referredEnrollmentId(referredEnrollmentId)
                .referredMemberId(randomLongId())
                .sessionId(sessionId)
                .relationStatus("ACTIVE")
                .build();
        referralRelationMapper.insert(relation);
        return relation;
    }

    private CourseSessionDO insertSession() {
        CourseSessionDO session = CourseSessionDO.builder()
                .courseId(randomLongId())
                .sessionCode("SC-" + randomLongId())
                .location("上海")
                .durationDays(5)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(15))
                .maxStudents(30)
                .enrolledCount(0)
                .sessionStatus("OPEN")
                .build();
        courseSessionMapper.insert(session);
        return session;
    }

    private void insertFeeConfig(Long sessionId, boolean commissionEnabled, BigDecimal commissionAmount) {
        SessionFeeConfigDO feeConfig = SessionFeeConfigDO.builder()
                .sessionId(sessionId)
                .tuitionAmount(new BigDecimal("5000"))
                .depositAmount(new BigDecimal("1000"))
                .isGrouponEnabled(false)
                .grouponDiscountAmount(BigDecimal.ZERO)
                .grouponRequiredCount(3)
                .grouponExpireHours(24)
                .grouponFailDiscountAmount(BigDecimal.ZERO)
                .isReferralCommissionEnabled(commissionEnabled)
                .referralCommissionAmount(commissionAmount)
                .isDepositRefundable(true)
                .balanceDueDaysBeforeStart(7)
                .build();
        sessionFeeConfigMapper.insert(feeConfig);
    }

    private cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO insertEnrollment(Long sessionId) {
        cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO enrollment =
                cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO.builder()
                .enrollmentNo("EN" + randomLongId())
                .memberId(randomLongId())
                .sessionId(sessionId)
                .realName("赵六")
                .idCard("110101199005055678")
                .phone("13600136000")
                .totalAmount(new BigDecimal("5000"))
                .depositAmountSnapshot(new BigDecimal("1000"))
                .balanceAmount(new BigDecimal("4000"))
                .paidAmount(BigDecimal.ZERO)
                .enrollmentStatus("DEPOSITED")
                .referralRightGranted(true)
                .build();
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    @Test
    public void testTriggerCommissionIfEligible_success_publishesEvent() {
        CourseSessionDO session = insertSession();
        insertFeeConfig(session.getId(), true, new BigDecimal("200.00"));
        Long referrerMemberId = randomLongId();
        Long enrollmentId = insertEnrollment(session.getId()).getId();
        insertActiveRelation(referrerMemberId, enrollmentId, session.getId());

        commissionRecordService.triggerCommissionIfEligible(enrollmentId);

        CommissionRecordDO record = commissionRecordMapper.selectByReferredEnrollmentId(enrollmentId);
        assertNotNull(record);
        assertEquals(referrerMemberId, record.getReferrerMemberId());
        assertEquals(new BigDecimal("200.00"), record.getCommissionAmount());
        assertEquals("WAITING_FOR_CLASS", record.getCommissionStatus());
        assertEquals(session.getStartDate().plusDays(7), record.getExpectedSettleDate());

        List<ReferralSuccessEvent> published = events.stream(ReferralSuccessEvent.class).toList();
        assertEquals(1, published.size());
        assertEquals(referrerMemberId, published.get(0).getReferrerMemberId());
        assertEquals(record.getId(), published.get(0).getCommissionRecordId());
    }

    @Test
    public void testTriggerCommissionIfEligible_idempotent_secondCallNoOp() {
        CourseSessionDO session = insertSession();
        insertFeeConfig(session.getId(), true, new BigDecimal("200.00"));
        Long enrollmentId = insertEnrollment(session.getId()).getId();
        insertActiveRelation(randomLongId(), enrollmentId, session.getId());

        commissionRecordService.triggerCommissionIfEligible(enrollmentId);
        commissionRecordService.triggerCommissionIfEligible(enrollmentId); // 重复触发（定金+尾款都可能调用）

        assertEquals(1, events.stream(ReferralSuccessEvent.class).count());
    }

    @Test
    public void testTriggerCommissionIfEligible_noActiveRelation_noRecordCreated() {
        Long enrollmentId = randomLongId();
        // 不插入推荐关系

        commissionRecordService.triggerCommissionIfEligible(enrollmentId);

        assertNull(commissionRecordMapper.selectByReferredEnrollmentId(enrollmentId));
        assertEquals(0, events.stream(ReferralSuccessEvent.class).count());
    }

    @Test
    public void testTriggerCommissionIfEligible_commissionDisabled_noRecordCreated() {
        CourseSessionDO session = insertSession();
        insertFeeConfig(session.getId(), false, BigDecimal.ZERO); // 未开启佣金
        Long enrollmentId = insertEnrollment(session.getId()).getId();
        insertActiveRelation(randomLongId(), enrollmentId, session.getId());

        commissionRecordService.triggerCommissionIfEligible(enrollmentId);

        assertNull(commissionRecordMapper.selectByReferredEnrollmentId(enrollmentId));
        assertEquals(0, events.stream(ReferralSuccessEvent.class).count());
    }

    private CommissionRecordDO insertCommissionRecord(String status, BigDecimal amount, Long referrerMemberId) {
        CommissionRecordDO record = CommissionRecordDO.builder()
                .referrerMemberId(referrerMemberId)
                .referredEnrollmentId(randomLongId())
                .sessionId(randomLongId())
                .commissionAmount(amount)
                .commissionStatus(status)
                .expectedSettleDate(LocalDate.now())
                .isRiskFlagged(false)
                .triggerTime(LocalDateTime.now())
                .retryCount(0)
                .build();
        commissionRecordMapper.insert(record);
        return record;
    }

    @Test
    public void testApproveCommission_success() {
        CommissionRecordDO record = insertCommissionRecord("PENDING_REVIEW", new BigDecimal("200.00"), randomLongId());

        commissionRecordService.approveCommission(record.getId(), 999L, "审核通过");

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("PENDING_PAYOUT", updated.getCommissionStatus());
        assertEquals(999L, updated.getApproverId());
        assertNotNull(updated.getApproveTime());
    }

    @Test
    public void testApproveCommission_wrongStatus_throws() {
        CommissionRecordDO record = insertCommissionRecord("WAITING_FOR_CLASS", new BigDecimal("200.00"), randomLongId());

        assertServiceException(() -> commissionRecordService.approveCommission(record.getId(), 999L, null),
                COMMISSION_APPROVE_NOT_ALLOWED);
    }

    @Test
    public void testRejectCommission_success() {
        CommissionRecordDO record = insertCommissionRecord("PENDING_REVIEW", new BigDecimal("200.00"), randomLongId());

        commissionRecordService.rejectCommission(record.getId(), 999L, "存在异常推荐行为", "备注");

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("REJECTED", updated.getCommissionStatus());
        assertTrue(updated.getApproveRemark().contains("存在异常推荐行为"));
    }

    @Test
    public void testPayoutCommission_success_publishesEvent() {
        Long referrerMemberId = randomLongId();
        CommissionRecordDO record = insertCommissionRecord("PENDING_PAYOUT", new BigDecimal("200.00"), referrerMemberId);

        MemberUserDO referrer = MemberUserDO.builder().id(referrerMemberId).openId("wx-openid-1").nickname("推荐人").build();
        when(memberUserService.getUser(referrerMemberId)).thenReturn(referrer);
        when(taxService.calculateAndRecordTax(eq(referrerMemberId), eq(new BigDecimal("200.00"))))
                .thenReturn(new BigDecimal("0.00"));
        PayTransferCreateRespDTO transferResp = new PayTransferCreateRespDTO();
        transferResp.setId(8888L);
        when(payTransferApi.createTransfer(any())).thenReturn(transferResp);

        commissionRecordService.payoutCommission(record.getId());

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("PAID", updated.getCommissionStatus());
        assertEquals(8888L, updated.getPayTransferId());

        List<CommissionPaidEvent> published = events.stream(CommissionPaidEvent.class).toList();
        assertEquals(1, published.size());
        assertEquals(referrerMemberId, published.get(0).getReferrerMemberId());
        assertEquals(new BigDecimal("200.00"), published.get(0).getNetAmount());
        verify(commissionAccountService).confirmPayout(referrerMemberId, new BigDecimal("200.00"));
    }

    @Test
    public void testPayoutCommission_noOpenId_marksFailedWithoutEvent() {
        Long referrerMemberId = randomLongId();
        CommissionRecordDO record = insertCommissionRecord("PENDING_PAYOUT", new BigDecimal("200.00"), referrerMemberId);
        when(memberUserService.getUser(referrerMemberId)).thenReturn(MemberUserDO.builder().id(referrerMemberId).build()); // 无 openId

        commissionRecordService.payoutCommission(record.getId());

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("FAILED", updated.getCommissionStatus());
        assertEquals(0, events.stream(CommissionPaidEvent.class).count());
        verify(payTransferApi, never()).createTransfer(any());
    }

    @Test
    public void testPayoutCommission_transferThrows_marksFailed() {
        Long referrerMemberId = randomLongId();
        CommissionRecordDO record = insertCommissionRecord("PENDING_PAYOUT", new BigDecimal("200.00"), referrerMemberId);
        when(memberUserService.getUser(referrerMemberId))
                .thenReturn(MemberUserDO.builder().id(referrerMemberId).openId("wx-openid-2").build());
        when(taxService.calculateAndRecordTax(any(), any())).thenReturn(BigDecimal.ZERO);
        when(payTransferApi.createTransfer(any())).thenThrow(new RuntimeException("微信企业付款失败"));

        commissionRecordService.payoutCommission(record.getId());

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("FAILED", updated.getCommissionStatus());
        assertEquals("微信企业付款失败", updated.getFailReason());
        assertEquals(0, events.stream(CommissionPaidEvent.class).count());
    }

    @Test
    public void testManualPayout_wrongStatus_throws() {
        CommissionRecordDO record = insertCommissionRecord("PENDING_PAYOUT", new BigDecimal("200.00"), randomLongId()); // 非 FAILED

        assertServiceException(() -> commissionRecordService.manualPayout(record.getId()), COMMISSION_STATUS_ERROR);
    }

    @Test
    public void testManualPayout_success_resetsAndRetriesPayout() {
        Long referrerMemberId = randomLongId();
        CommissionRecordDO record = insertCommissionRecord("FAILED", new BigDecimal("200.00"), referrerMemberId);
        when(memberUserService.getUser(referrerMemberId))
                .thenReturn(MemberUserDO.builder().id(referrerMemberId).openId("wx-openid-3").build());
        when(taxService.calculateAndRecordTax(any(), any())).thenReturn(BigDecimal.ZERO);
        PayTransferCreateRespDTO transferResp = new PayTransferCreateRespDTO();
        transferResp.setId(9999L);
        when(payTransferApi.createTransfer(any())).thenReturn(transferResp);

        commissionRecordService.manualPayout(record.getId());

        CommissionRecordDO updated = commissionRecordMapper.selectById(record.getId());
        assertEquals("PAID", updated.getCommissionStatus());
        assertEquals(1, events.stream(CommissionPaidEvent.class).count());
    }

}
