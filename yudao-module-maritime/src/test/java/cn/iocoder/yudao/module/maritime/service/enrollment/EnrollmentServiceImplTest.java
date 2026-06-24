package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.mq.event.DepositPaidEvent;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCreatedEvent;
import cn.iocoder.yudao.module.maritime.service.commissionAccount.CommissionAccountService;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponService;
import cn.iocoder.yudao.module.maritime.service.referralRelation.ReferralRelationService;
import cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
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

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link EnrollmentServiceImpl} 的单元测试类
 *
 * 重点覆盖：定金支付幂等（CAS）、超卖防护、报名重复校验、T09 事件发布时机
 *
 * 注：ApplicationEventPublisher 不能用 {@code @MockitoBean} 模拟——Spring 容器把
 * ApplicationContext 自身注册为该类型的 resolvableDependency，按类型自动装配时会优先于普通 Mock Bean
 * 注入到被测 Service，导致 verify() 永远收不到调用。改用官方推荐的 {@code @RecordApplicationEvents}
 * + {@link ApplicationEvents} 来断言事件是否被发布。
 */
@Import(EnrollmentServiceImpl.class)
@RecordApplicationEvents
public class EnrollmentServiceImplTest extends BaseDbUnitTest {

    @Resource
    private EnrollmentServiceImpl enrollmentService;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;
    @Resource
    private CourseSessionMapper courseSessionMapper;
    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;
    @Autowired
    private ApplicationEvents events;

    @MockitoBean
    private PayOrderApi payOrderApi;
    @MockitoBean
    private CommissionAccountService commissionAccountService;
    @MockitoBean
    private ReferralRelationService referralRelationService;
    @MockitoBean
    private CommissionRecordService commissionRecordService;
    @MockitoBean
    private GrouponService grouponService;
    @MockitoBean
    private RefundApplyService refundApplyService;

    private CourseSessionDO insertOpenSession(int maxStudents, int enrolledCount) {
        CourseSessionDO session = CourseSessionDO.builder()
                .courseId(randomLongId())
                .sessionCode("SC-" + randomLongId())
                .location("上海")
                .durationDays(5)
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(35))
                .maxStudents(maxStudents)
                .enrolledCount(enrolledCount)
                .sessionStatus("OPEN")
                .build();
        courseSessionMapper.insert(session);
        return session;
    }

    private SessionFeeConfigDO insertFeeConfig(Long sessionId, BigDecimal tuition, BigDecimal deposit) {
        SessionFeeConfigDO feeConfig = SessionFeeConfigDO.builder()
                .sessionId(sessionId)
                .tuitionAmount(tuition)
                .depositAmount(deposit)
                .isGrouponEnabled(false)
                .grouponDiscountAmount(BigDecimal.ZERO)
                .grouponRequiredCount(3)
                .grouponExpireHours(24)
                .grouponFailDiscountAmount(BigDecimal.ZERO)
                .referralCommissionAmount(BigDecimal.ZERO)
                .isReferralCommissionEnabled(false)
                .isDepositRefundable(true)
                .balanceDueDaysBeforeStart(7)
                .build();
        sessionFeeConfigMapper.insert(feeConfig);
        return feeConfig;
    }

    private AppEnrollmentCreateReqVO buildCreateReqVO(Long sessionId) {
        AppEnrollmentCreateReqVO reqVO = new AppEnrollmentCreateReqVO();
        reqVO.setSessionId(sessionId);
        reqVO.setRealName("张三");
        reqVO.setIdCard("110101199001011234");
        reqVO.setPhone("13800138000");
        reqVO.setJoinGroupon(false);
        return reqVO;
    }

    @Test
    public void testCreateEnrollmentForApp_success() {
        CourseSessionDO session = insertOpenSession(30, 5);
        insertFeeConfig(session.getId(), new BigDecimal("5000.00"), new BigDecimal("1000.00"));
        Long memberId = randomLongId();

        AppEnrollmentCreateRespVO respVO = enrollmentService.createEnrollmentForApp(buildCreateReqVO(session.getId()), memberId);

        assertNotNull(respVO.getEnrollmentId());
        assertNotNull(respVO.getOrderId());
        assertEquals(new BigDecimal("1000.00"), respVO.getDepositAmount());

        EnrollmentDO enrollment = enrollmentMapper.selectById(respVO.getEnrollmentId());
        assertEquals("PENDING_DEPOSIT", enrollment.getEnrollmentStatus());
        assertEquals(memberId, enrollment.getMemberId());
        assertFalse(enrollment.getReferralRightGranted());

        EnrollmentOrderDO order = enrollmentOrderMapper.selectById(respVO.getOrderId());
        assertEquals("DEPOSIT", order.getOrderType());
        assertEquals("PENDING", order.getOrderStatus());
        assertEquals(new BigDecimal("1000.00"), order.getAmount());

        // 名额 CAS 自增
        CourseSessionDO updatedSession = courseSessionMapper.selectById(session.getId());
        assertEquals(6, updatedSession.getEnrolledCount());

        // T09: 报名创建事件已发布
        assertEquals(1, events.stream(EnrollmentCreatedEvent.class).count());
    }

    @Test
    public void testCreateEnrollmentForApp_sessionNotOpen() {
        CourseSessionDO session = insertOpenSession(30, 0);
        courseSessionMapper.updateById(CourseSessionDO.builder().id(session.getId()).sessionStatus("DRAFT").build());
        insertFeeConfig(session.getId(), new BigDecimal("5000"), new BigDecimal("1000"));

        assertServiceException(() -> enrollmentService.createEnrollmentForApp(buildCreateReqVO(session.getId()), randomLongId()),
                SESSION_NOT_OPEN);
    }

    @Test
    public void testCreateEnrollmentForApp_sessionFull() {
        CourseSessionDO session = insertOpenSession(10, 10); // 已满
        insertFeeConfig(session.getId(), new BigDecimal("5000"), new BigDecimal("1000"));

        assertServiceException(() -> enrollmentService.createEnrollmentForApp(buildCreateReqVO(session.getId()), randomLongId()),
                SESSION_FULL);
        // 名额已满时不应再次写入
        assertEquals(0, events.stream(EnrollmentCreatedEvent.class).count());
    }

    @Test
    public void testCreateEnrollmentForApp_feeConfigMissing() {
        CourseSessionDO session = insertOpenSession(30, 0);
        // 不插入费用配置

        assertServiceException(() -> enrollmentService.createEnrollmentForApp(buildCreateReqVO(session.getId()), randomLongId()),
                SESSION_FEE_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testCreateEnrollmentForApp_duplicateIdCard() {
        CourseSessionDO session = insertOpenSession(30, 0);
        insertFeeConfig(session.getId(), new BigDecimal("5000"), new BigDecimal("1000"));
        AppEnrollmentCreateReqVO reqVO = buildCreateReqVO(session.getId());

        // 第一次报名成功
        enrollmentService.createEnrollmentForApp(reqVO, randomLongId());

        // 同一身份证 + 同一班期再次报名，应被拒绝
        assertServiceException(() -> enrollmentService.createEnrollmentForApp(reqVO, randomLongId()),
                ENROLLMENT_DUPLICATE);
    }

    private EnrollmentDO insertEnrollment(Long sessionId, BigDecimal totalAmount, BigDecimal depositSnapshot,
                                          BigDecimal balanceAmount, BigDecimal paidAmount, Long memberId) {
        EnrollmentDO enrollment = EnrollmentDO.builder()
                .enrollmentNo("EN" + randomLongId())
                .memberId(memberId)
                .sessionId(sessionId)
                .realName("李四")
                .idCard("110101199002022345")
                .phone("13900139000")
                .totalAmount(totalAmount)
                .depositAmountSnapshot(depositSnapshot)
                .balanceAmount(balanceAmount)
                .paidAmount(paidAmount)
                .enrollmentStatus("PENDING_DEPOSIT")
                .referralRightGranted(false)
                .build();
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    private EnrollmentOrderDO insertDepositOrder(Long enrollmentId, BigDecimal amount, String status) {
        EnrollmentOrderDO order = EnrollmentOrderDO.builder()
                .enrollmentId(enrollmentId)
                .orderNo("OD" + randomLongId())
                .orderType("DEPOSIT")
                .amount(amount)
                .orderStatus(status)
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();
        enrollmentOrderMapper.insert(order);
        return order;
    }

    @Test
    public void testHandleDepositPaid_success_noBalance_triggersCommission() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, randomLongId());
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");

        PayOrderRespDTO payOrderResp = new PayOrderRespDTO();
        payOrderResp.setPrice(100000); // 1000.00 元 = 100000 分
        when(payOrderApi.getOrder(eq(888L))).thenReturn(payOrderResp);

        enrollmentService.handleDepositPaid(order.getOrderNo(), 888L);

        EnrollmentOrderDO updatedOrder = enrollmentOrderMapper.selectById(order.getId());
        assertEquals("PAID", updatedOrder.getOrderStatus());
        assertNotNull(updatedOrder.getPayTime());

        EnrollmentDO updatedEnrollment = enrollmentMapper.selectById(enrollment.getId());
        assertEquals("DEPOSITED", updatedEnrollment.getEnrollmentStatus());
        assertTrue(updatedEnrollment.getReferralRightGranted());
        assertEquals(new BigDecimal("1000.00"), updatedEnrollment.getPaidAmount());

        assertEquals(1, events.stream(DepositPaidEvent.class).count());
        // 无尾款时，定金支付即触发佣金判定
        verify(commissionRecordService).triggerCommissionIfEligible(enrollment.getId());
        verify(grouponService).handleMemberPaid(enrollment.getId());
    }

    @Test
    public void testHandleDepositPaid_withBalance_doesNotTriggerCommissionYet() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("5000.00"), new BigDecimal("1000.00"),
                new BigDecimal("4000.00"), BigDecimal.ZERO, randomLongId());
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");

        when(payOrderApi.getOrder(any())).thenReturn(null); // 跳过金额校验分支

        enrollmentService.handleDepositPaid(order.getOrderNo(), 999L);

        verify(commissionRecordService, never()).triggerCommissionIfEligible(any());
        assertEquals(1, events.stream(DepositPaidEvent.class).count());
    }

    @Test
    public void testHandleDepositPaid_idempotent_secondCallIsNoOp() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, randomLongId());
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");
        when(payOrderApi.getOrder(any())).thenReturn(null);

        enrollmentService.handleDepositPaid(order.getOrderNo(), 1L);
        enrollmentService.handleDepositPaid(order.getOrderNo(), 1L); // 重复回调

        // CAS 幂等：事件只应发布一次，佣金/拼团只应处理一次
        assertEquals(1, events.stream(DepositPaidEvent.class).count());
        verify(grouponService, times(1)).handleMemberPaid(enrollment.getId());
    }

    @Test
    public void testHandleDepositPaid_amountMismatch_skipsProcessing() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, randomLongId());
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");

        PayOrderRespDTO payOrderResp = new PayOrderRespDTO();
        payOrderResp.setPrice(50000); // 实付 500 元，与应付 1000 元不符
        when(payOrderApi.getOrder(any())).thenReturn(payOrderResp);

        enrollmentService.handleDepositPaid(order.getOrderNo(), 2L);

        EnrollmentOrderDO updatedOrder = enrollmentOrderMapper.selectById(order.getId());
        assertEquals("PENDING", updatedOrder.getOrderStatus());
        assertEquals(0, events.stream(DepositPaidEvent.class).count());
    }

    @Test
    public void testPayDeposit_idempotent_secondCallDoesNotRecreatePayOrder() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, 100L);
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");
        when(payOrderApi.createOrder(any())).thenReturn(12345L);

        AppPayOrderRespVO first = enrollmentService.payDeposit(order.getId(), 100L);
        AppPayOrderRespVO second = enrollmentService.payDeposit(order.getId(), 100L);

        assertEquals(12345L, first.getPayOrderId());
        assertEquals(12345L, second.getPayOrderId());
        verify(payOrderApi, times(1)).createOrder(any());
    }

    @Test
    public void testCancelEnrollmentForApp_success() {
        CourseSessionDO session = insertOpenSession(30, 5);
        EnrollmentDO enrollment = insertEnrollment(session.getId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, 200L);
        EnrollmentOrderDO order = insertDepositOrder(enrollment.getId(), new BigDecimal("1000.00"), "PENDING");

        enrollmentService.cancelEnrollmentForApp(enrollment.getId(), 200L);

        assertEquals("CANCELLED", enrollmentMapper.selectById(enrollment.getId()).getEnrollmentStatus());
        assertEquals("CLOSED", enrollmentOrderMapper.selectById(order.getId()).getOrderStatus());
        assertEquals(4, courseSessionMapper.selectById(session.getId()).getEnrolledCount());
    }

    @Test
    public void testCancelEnrollmentForApp_wrongStatus_throws() {
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), new BigDecimal("1000.00"), new BigDecimal("1000.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, 300L);
        enrollmentMapper.updateById(EnrollmentDO.builder().id(enrollment.getId()).enrollmentStatus("DEPOSITED").build());

        assertServiceException(() -> enrollmentService.cancelEnrollmentForApp(enrollment.getId(), 300L),
                ENROLLMENT_CANCEL_NOT_ALLOWED);
    }

}
