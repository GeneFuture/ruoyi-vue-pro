package cn.iocoder.yudao.module.maritime.service.groupon;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.maritime.controller.app.groupon.vo.AppGrouponStatusRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 拼团业务 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class GrouponServiceImpl implements GrouponService {

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private GrouponMemberMapper grouponMemberMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GrouponProcessResult joinOrCreateGroupon(Long enrollmentId, Long memberId, String inviteCode,
                                                    Long sessionId, SessionFeeConfigDO feeConfig) {
        GrouponRecordDO grouponRecord;

        if (StrUtil.isNotBlank(inviteCode)) {
            // 加入已有拼团：CAS 递增保证原子性
            grouponRecord = grouponRecordMapper.selectByInviteCode(inviteCode);
            if (grouponRecord == null) {
                throw exception(GROUPON_NOT_EXISTS);
            }
            validateCanJoin(grouponRecord, sessionId, enrollmentId);

            int incremented = grouponRecordMapper.incrementCount(grouponRecord.getId());
            if (incremented == 0) {
                throw exception(GROUPON_FULL);
            }
        } else {
            // 新建拼团：发起人作为第 1 人，current_count = 1（与 DB DEFAULT 1 对齐）
            if (feeConfig.getIsGrouponEnabled() == null || !feeConfig.getIsGrouponEnabled()) {
                throw exception(GROUPON_NOT_ENABLED);
            }
            checkGrouponCreateLimit(memberId);
            grouponRecord = createGrouponRecord(enrollmentId, sessionId, feeConfig);
        }

        // 写入拼团成员（两条路径共用）
        GrouponMemberDO member = new GrouponMemberDO();
        member.setGrouponRecordId(grouponRecord.getId());
        member.setEnrollmentId(enrollmentId);
        member.setMemberId(memberId);
        member.setJoinTime(LocalDateTime.now());
        member.setMemberStatus("ACTIVE");
        grouponMemberMapper.insert(member);

        // 注意：不在此处触发成团。成团时机为"第 N 人支付定金"，由 handleMemberPaid 负责。

        return new GrouponProcessResult(grouponRecord.getId(), grouponRecord.getInviteCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleMemberPaid(Long enrollmentId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null || enrollment.getGrouponRecordId() == null) {
            return;
        }

        // 悲观锁序列化并发支付回调，避免多线程同时读到 paidCount < requiredCount 而漏触发成团
        GrouponRecordDO record = grouponRecordMapper.selectByIdForUpdate(enrollment.getGrouponRecordId());
        if (record == null || !"IN_PROGRESS".equals(record.getGrouponStatus())) {
            return;
        }

        // 记录定金支付时间（在锁内执行，对后续 count 查询可见）
        grouponMemberMapper.updateDepositPaidAt(enrollmentId, LocalDateTime.now());

        // 统计已支付人数（同一事务内可见本次写入）
        long paidCount = grouponMemberMapper.countPaidByGrouponRecordId(record.getId());
        if (paidCount >= record.getRequiredCount()) {
            // 复用 succeedGroupon 而非重复内联 CAS 逻辑，确保 T09 成团通知事件同样被发布
            succeedGroupon(record.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void succeedGroupon(Long grouponRecordId) {
        int updated = grouponRecordMapper.updateStatusAndSuccessTime(grouponRecordId, LocalDateTime.now());
        if (updated > 0) {
            log.info("[groupon] 拼团成功: recordId={}", grouponRecordId);
            GrouponRecordDO record = grouponRecordMapper.selectById(grouponRecordId);
            if (record != null) {
                eventPublisher.publishEvent(new GrouponSuccessEvent(
                        this, grouponRecordId, record.getSessionId(), getMemberIds(grouponRecordId)));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void degradeGroupon(Long grouponRecordId) {
        int updated = grouponRecordMapper.updateStatusIfInProgress(grouponRecordId, "DEGRADED");
        if (updated > 0) {
            log.info("[groupon] 拼团降级: recordId={}", grouponRecordId);
            eventPublisher.publishEvent(new GrouponDegradedEvent(this, grouponRecordId, getMemberIds(grouponRecordId)));
        }
    }

    /** 拼团成员 memberId 列表（用于通知事件） */
    private List<Long> getMemberIds(Long grouponRecordId) {
        return grouponMemberMapper.selectListByGrouponRecordId(grouponRecordId).stream()
                .map(GrouponMemberDO::getMemberId)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppActiveGrouponVO> getActiveGroupons(Long sessionId) {
        List<GrouponRecordDO> records = grouponRecordMapper.selectActiveBySessionId(sessionId);
        return records.stream().map(r -> {
            AppActiveGrouponVO vo = new AppActiveGrouponVO();
            vo.setId(r.getId());
            vo.setInviteCode(r.getInviteCode());
            vo.setCurrentCount(r.getCurrentCount());
            vo.setRequiredCount(r.getRequiredCount());
            vo.setRemainingCount(Math.max(0, r.getRequiredCount() - r.getCurrentCount()));
            vo.setExpireTime(r.getExpireTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AppGrouponStatusRespVO getMyGrouponStatus(Long enrollmentId, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        if (enrollment.getGrouponRecordId() == null) {
            throw exception(GROUPON_NOT_EXISTS);
        }

        GrouponRecordDO record = grouponRecordMapper.selectById(enrollment.getGrouponRecordId());
        if (record == null) {
            throw exception(GROUPON_NOT_EXISTS);
        }

        AppGrouponStatusRespVO vo = new AppGrouponStatusRespVO();
        vo.setGrouponRecordId(record.getId());
        vo.setInviteCode(record.getInviteCode());
        vo.setGrouponStatus(record.getGrouponStatus());
        vo.setCurrentCount(record.getCurrentCount());
        vo.setRequiredCount(record.getRequiredCount());
        vo.setRemainingCount(Math.max(0, record.getRequiredCount() - record.getCurrentCount()));
        vo.setExpireTime(record.getExpireTime());
        vo.setSuccessTime(record.getSuccessTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceSucceedGroupon(Long grouponRecordId) {
        GrouponRecordDO record = grouponRecordMapper.selectById(grouponRecordId);
        if (record == null) {
            throw exception(GROUPON_RECORD_NOT_EXISTS);
        }
        // 只有 IN_PROGRESS 状态可强制成团；DEGRADED / SUCCESS 状态明确报错，避免管理员误以为操作成功
        if (!"IN_PROGRESS".equals(record.getGrouponStatus())) {
            throw exception(GROUPON_NOT_IN_PROGRESS);
        }
        succeedGroupon(grouponRecordId);
        log.info("[groupon] 管理员强制成团: recordId={}", grouponRecordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeGroupon(Long grouponRecordId) {
        if (grouponRecordMapper.selectById(grouponRecordId) == null) {
            throw exception(GROUPON_RECORD_NOT_EXISTS);
        }
        // degradeGroupon 内部 CAS 幂等，对 SUCCESS / DEGRADED 状态静默跳过
        degradeGroupon(grouponRecordId);
        log.info("[groupon] 管理员关闭拼团: recordId={}", grouponRecordId);
    }

    // ========== 私有工具 ==========

    private void validateCanJoin(GrouponRecordDO record, Long sessionId, Long enrollmentId) {
        if (!"IN_PROGRESS".equals(record.getGrouponStatus())) {
            throw exception(GROUPON_NOT_IN_PROGRESS);
        }
        if (record.getExpireTime().isBefore(LocalDateTime.now())) {
            throw exception(GROUPON_EXPIRED);
        }
        if (!record.getSessionId().equals(sessionId)) {
            throw exception(GROUPON_SESSION_MISMATCH);
        }
        // DB CAS 是真正的并发门卫；此处预检仅减少无效 DB 压力（非原子，不作为最终依据）
        if (record.getCurrentCount() >= record.getRequiredCount()) {
            throw exception(GROUPON_FULL);
        }
        if (grouponMemberMapper.selectByGrouponAndEnrollment(record.getId(), enrollmentId) != null) {
            throw exception(GROUPON_ALREADY_JOINED);
        }
    }

    private GrouponRecordDO createGrouponRecord(Long initiatorEnrollmentId, Long sessionId,
                                                SessionFeeConfigDO feeConfig) {
        GrouponRecordDO record = new GrouponRecordDO();
        record.setSessionId(sessionId);
        record.setInviteCode(generateInviteCode());
        record.setInitiatorEnrollmentId(initiatorEnrollmentId);
        record.setRequiredCount(feeConfig.getGrouponRequiredCount());
        record.setCurrentCount(1); // 发起人即第 1 人，与 DB DEFAULT 1 对齐
        record.setGrouponStatus("IN_PROGRESS");
        int expireHours = feeConfig.getGrouponExpireHours() != null ? feeConfig.getGrouponExpireHours() : 24;
        record.setExpireTime(LocalDateTime.now().plusHours(expireHours));
        grouponRecordMapper.insert(record);
        return record;
    }

    private String generateInviteCode() {
        return "GRP-" + RandomUtil.randomStringUpper(8);
    }

    private void checkGrouponCreateLimit(Long memberId) {
        String key = "groupon:create:limit:" + memberId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
        if (count != null && count > 3) {
            throw exception(GROUPON_CREATE_FREQ_LIMIT);
        }
    }

}
