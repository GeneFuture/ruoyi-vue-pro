package cn.iocoder.yudao.module.maritime.service.groupon;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember.GrouponMemberMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.mq.event.GrouponDegradedEvent;
import cn.iocoder.yudao.module.maritime.mq.event.GrouponSuccessEvent;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link GrouponServiceImpl} 的单元测试类
 *
 * 重点覆盖：拼团 CAS 并发安全、强制成团/关闭的状态机校验、T09 事件发布时机与幂等
 *
 * 注：事件发布通过 {@code @RecordApplicationEvents} + {@link ApplicationEvents} 断言，
 * 而非 mock ApplicationEventPublisher——后者会被 Spring 容器自身的 resolvableDependency
 * 抢先注入，导致 Mock 永远收不到调用。
 */
@Import(GrouponServiceImpl.class)
@RecordApplicationEvents
public class GrouponServiceImplTest extends BaseDbUnitTest {

    @Resource
    private GrouponServiceImpl grouponService;
    @Resource
    private GrouponRecordMapper grouponRecordMapper;
    @Resource
    private GrouponMemberMapper grouponMemberMapper;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private ApplicationEvents events;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    private GrouponRecordDO insertGrouponRecord(Long sessionId, int requiredCount, int currentCount, String status) {
        GrouponRecordDO record = GrouponRecordDO.builder()
                .sessionId(sessionId)
                .inviteCode("G" + ThreadLocalRandom.current().nextInt(1000000, 9999999))
                .initiatorEnrollmentId(randomLongId())
                .requiredCount(requiredCount)
                .currentCount(currentCount)
                .grouponStatus(status)
                .expireTime(LocalDateTime.now().plusHours(24))
                .build();
        grouponRecordMapper.insert(record);
        return record;
    }

    private void addMember(Long grouponRecordId, Long enrollmentId, Long memberId, LocalDateTime depositPaidAt) {
        GrouponMemberDO member = GrouponMemberDO.builder()
                .grouponRecordId(grouponRecordId)
                .enrollmentId(enrollmentId)
                .memberId(memberId)
                .joinTime(LocalDateTime.now())
                .memberStatus("ACTIVE")
                .depositPaidAt(depositPaidAt)
                .build();
        grouponMemberMapper.insert(member);
    }

    private EnrollmentDO insertEnrollmentWithGroupon(Long sessionId, Long grouponRecordId) {
        EnrollmentDO enrollment = EnrollmentDO.builder()
                .enrollmentNo("EN" + randomLongId())
                .memberId(randomLongId())
                .sessionId(sessionId)
                .grouponRecordId(grouponRecordId)
                .realName("王五")
                .idCard("110101199003033456")
                .phone("13700137000")
                .totalAmount(java.math.BigDecimal.TEN)
                .depositAmountSnapshot(java.math.BigDecimal.ONE)
                .balanceAmount(java.math.BigDecimal.ZERO)
                .paidAmount(java.math.BigDecimal.ZERO)
                .enrollmentStatus("PENDING_DEPOSIT")
                .referralRightGranted(false)
                .build();
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    @Test
    public void testSucceedGroupon_publishesEventWithMemberIds() {
        Long sessionId = randomLongId();
        GrouponRecordDO record = insertGrouponRecord(sessionId, 3, 3, "IN_PROGRESS");
        addMember(record.getId(), randomLongId(), 1001L, LocalDateTime.now());
        addMember(record.getId(), randomLongId(), 1002L, LocalDateTime.now());

        grouponService.succeedGroupon(record.getId());

        GrouponRecordDO updated = grouponRecordMapper.selectById(record.getId());
        assertEquals("SUCCESS", updated.getGrouponStatus());
        assertNotNull(updated.getSuccessTime());

        List<GrouponSuccessEvent> published = events.stream(GrouponSuccessEvent.class).toList();
        assertEquals(1, published.size());
        assertEquals(record.getId(), published.get(0).getGrouponRecordId());
        assertEquals(sessionId, published.get(0).getSessionId());
        assertEquals(2, published.get(0).getMemberIds().size());
    }

    @Test
    public void testSucceedGroupon_idempotent_secondCallNoOp() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 3, "IN_PROGRESS");

        grouponService.succeedGroupon(record.getId());
        grouponService.succeedGroupon(record.getId()); // 重复调用（如并发回调）

        assertEquals(1, events.stream(GrouponSuccessEvent.class).count());
    }

    @Test
    public void testDegradeGroupon_publishesEvent() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 1, "IN_PROGRESS");
        addMember(record.getId(), randomLongId(), 2001L, null);

        grouponService.degradeGroupon(record.getId());

        assertEquals("DEGRADED", grouponRecordMapper.selectById(record.getId()).getGrouponStatus());
        List<GrouponDegradedEvent> published = events.stream(GrouponDegradedEvent.class).toList();
        assertEquals(1, published.size());
        assertEquals(record.getId(), published.get(0).getGrouponRecordId());
        assertEquals(1, published.get(0).getMemberIds().size());
    }

    @Test
    public void testDegradeGroupon_idempotent_alreadySuccess_noEvent() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 3, "SUCCESS"); // 已成团

        grouponService.degradeGroupon(record.getId());

        assertEquals("SUCCESS", grouponRecordMapper.selectById(record.getId()).getGrouponStatus()); // 状态未被覆盖
        assertEquals(0, events.stream(GrouponDegradedEvent.class).count());
    }

    @Test
    public void testForceSucceedGroupon_wrongStatus_throws() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 1, "DEGRADED");

        assertServiceException(() -> grouponService.forceSucceedGroupon(record.getId()), GROUPON_NOT_IN_PROGRESS);
        assertEquals(0, events.stream(GrouponSuccessEvent.class).count());
    }

    @Test
    public void testForceSucceedGroupon_success() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 1, "IN_PROGRESS"); // 人数不足也可被管理员特批

        grouponService.forceSucceedGroupon(record.getId());

        assertEquals("SUCCESS", grouponRecordMapper.selectById(record.getId()).getGrouponStatus());
        assertEquals(1, events.stream(GrouponSuccessEvent.class).count());
    }

    @Test
    public void testForceSucceedGroupon_notExists_throws() {
        assertServiceException(() -> grouponService.forceSucceedGroupon(randomLongId()), GROUPON_RECORD_NOT_EXISTS);
    }

    @Test
    public void testCloseGroupon_success() {
        GrouponRecordDO record = insertGrouponRecord(randomLongId(), 3, 2, "IN_PROGRESS");

        grouponService.closeGroupon(record.getId());

        assertEquals("DEGRADED", grouponRecordMapper.selectById(record.getId()).getGrouponStatus());
        assertEquals(1, events.stream(GrouponDegradedEvent.class).count());
    }

    @Test
    public void testHandleMemberPaid_reachesThreshold_succeeds() {
        Long sessionId = randomLongId();
        GrouponRecordDO record = insertGrouponRecord(sessionId, 2, 2, "IN_PROGRESS");
        EnrollmentDO enrollment1 = insertEnrollmentWithGroupon(sessionId, record.getId());
        EnrollmentDO enrollment2 = insertEnrollmentWithGroupon(sessionId, record.getId());
        addMember(record.getId(), enrollment1.getId(), enrollment1.getMemberId(), LocalDateTime.now()); // 已支付
        addMember(record.getId(), enrollment2.getId(), enrollment2.getMemberId(), null); // 待支付

        grouponService.handleMemberPaid(enrollment2.getId()); // 第二人支付，达成成团条件

        assertEquals("SUCCESS", grouponRecordMapper.selectById(record.getId()).getGrouponStatus());
        assertEquals(1, events.stream(GrouponSuccessEvent.class).count());
    }

    @Test
    public void testHandleMemberPaid_belowThreshold_staysInProgress() {
        Long sessionId = randomLongId();
        GrouponRecordDO record = insertGrouponRecord(sessionId, 3, 1, "IN_PROGRESS");
        EnrollmentDO enrollment = insertEnrollmentWithGroupon(sessionId, record.getId());
        addMember(record.getId(), enrollment.getId(), enrollment.getMemberId(), null);

        grouponService.handleMemberPaid(enrollment.getId());

        assertEquals("IN_PROGRESS", grouponRecordMapper.selectById(record.getId()).getGrouponStatus());
        assertEquals(0, events.stream(GrouponSuccessEvent.class).count());
    }

    @Test
    public void testJoinOrCreateGroupon_joinExisting_success() {
        Long sessionId = randomLongId();
        GrouponRecordDO record = insertGrouponRecord(sessionId, 3, 1, "IN_PROGRESS");
        SessionFeeConfigDO feeConfig = SessionFeeConfigDO.builder().isGrouponEnabled(true).build();

        GrouponProcessResult result = grouponService.joinOrCreateGroupon(
                randomLongId(), randomLongId(), record.getInviteCode(), sessionId, feeConfig);

        assertEquals(record.getId(), result.grouponRecordId());
        assertEquals(2, grouponRecordMapper.selectById(record.getId()).getCurrentCount());
    }

    @Test
    public void testJoinOrCreateGroupon_full_throws() {
        Long sessionId = randomLongId();
        GrouponRecordDO record = insertGrouponRecord(sessionId, 2, 2, "IN_PROGRESS"); // 已满
        SessionFeeConfigDO feeConfig = SessionFeeConfigDO.builder().isGrouponEnabled(true).build();

        assertServiceException(() -> grouponService.joinOrCreateGroupon(
                randomLongId(), randomLongId(), record.getInviteCode(), sessionId, feeConfig), GROUPON_FULL);
    }

    @Test
    public void testJoinOrCreateGroupon_createNew_success() {
        Long sessionId = randomLongId();
        SessionFeeConfigDO feeConfig = SessionFeeConfigDO.builder()
                .isGrouponEnabled(true).grouponRequiredCount(3).grouponExpireHours(24).build();

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(any(String.class))).thenReturn(1L);

        GrouponProcessResult result = grouponService.joinOrCreateGroupon(
                randomLongId(), randomLongId(), null, sessionId, feeConfig);

        assertNotNull(result.grouponRecordId());
        GrouponRecordDO created = grouponRecordMapper.selectById(result.grouponRecordId());
        assertEquals("IN_PROGRESS", created.getGrouponStatus());
        assertEquals(1, created.getCurrentCount());
    }

}
