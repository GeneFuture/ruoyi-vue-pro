package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskResolveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.manualTask.ManualTaskMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCancelledEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 人工处理单 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class ManualTaskServiceImpl implements ManualTaskService {

    private static final DateTimeFormatter TASK_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<String> RESOLVE_TARGET_STATUSES = Set.of(
            EnrollmentStatusEnum.DEPOSITED.getStatus(),
            EnrollmentStatusEnum.PAID_OFF.getStatus(),
            EnrollmentStatusEnum.COMPLETED.getStatus(),
            EnrollmentStatusEnum.CANCELLED.getStatus());

    @Resource
    private ManualTaskMapper manualTaskMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTaskIfAbsent(Long enrollmentId, Long memberId, Long orderId,
                                   String sourceType, Long sourceId, String taskType, String reason) {
        ManualTaskDO existing = manualTaskMapper.selectOpenByEnrollmentIdAndType(enrollmentId, taskType);
        if (existing != null) {
            return existing.getId();
        }
        ManualTaskDO task = ManualTaskDO.builder()
                .taskNo(generateTaskNo())
                .enrollmentId(enrollmentId)
                .memberId(memberId)
                .orderId(orderId)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .taskType(taskType)
                .status(ManualTaskStatusEnum.PENDING.getStatus())
                .priority(0)
                .reason(reason)
                .build();
        manualTaskMapper.insert(task);
        log.info("[createTaskIfAbsent] 创建人工处理单, id={}, enrollmentId={}, type={}",
                task.getId(), enrollmentId, taskType);
        return task.getId();
    }

    @Override
    public ManualTaskDO getTask(Long id) {
        return manualTaskMapper.selectById(id);
    }

    @Override
    public ManualTaskDO getOpenTask(Long enrollmentId) {
        List<ManualTaskDO> tasks = getOpenTasks(enrollmentId);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    @Override
    public List<ManualTaskDO> getOpenTasks(Long enrollmentId) {
        return manualTaskMapper.selectOpenByEnrollmentId(enrollmentId);
    }

    @Override
    public PageResult<ManualTaskDO> getPage(ManualTaskPageReqVO pageReqVO) {
        return manualTaskMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveTask(ManualTaskResolveReqVO reqVO, Long handlerId) {
        ManualTaskDO task = manualTaskMapper.selectById(reqVO.getId());
        if (task == null) {
            throw exception(MANUAL_TASK_NOT_EXISTS);
        }
        if (!ManualTaskStatusEnum.PENDING.getStatus().equals(task.getStatus())
                && !ManualTaskStatusEnum.PROCESSING.getStatus().equals(task.getStatus())) {
            throw exception(MANUAL_TASK_ALREADY_CLOSED);
        }
        // 退费类任务需要走 ManualTaskRefundService 的退费审核流程（更新退款明细、订单、名额等）
        if (ManualTaskTypeEnum.REFUND_APPLY.getType().equals(task.getTaskType())
                || ManualTaskTypeEnum.REFUND_AMOUNT_CONFIRM.getType().equals(task.getTaskType())) {
            throw exception(MANUAL_TASK_REFUND_HANDLE_ELSEWHERE);
        }
        String targetStatus = reqVO.getTargetEnrollmentStatus();
        if (StrUtil.isBlank(targetStatus) || !RESOLVE_TARGET_STATUSES.contains(targetStatus)) {
            throw exception(MANUAL_TASK_TARGET_STATUS_INVALID);
        }

        // 1. 关闭人工单
        ManualTaskDO updateTask = new ManualTaskDO();
        updateTask.setId(task.getId());
        updateTask.setStatus(ManualTaskStatusEnum.DONE.getStatus());
        updateTask.setHandlerId(handlerId);
        updateTask.setHandleResult(StrUtil.blankToDefault(reqVO.getRemark(), reqVO.getHandleResult()));
        updateTask.setHandleTime(LocalDateTime.now());
        manualTaskMapper.updateById(updateTask);

        // 2. 还有其它未关闭人工单时，报名继续保持 MANUAL_PROCESSING
        if (!manualTaskMapper.selectOpenByEnrollmentId(task.getEnrollmentId()).isEmpty()) {
            log.info("[resolveTask] 仍有未关闭人工单，报名状态保持不变, enrollmentId={}", task.getEnrollmentId());
            return;
        }

        // 3. 最后一条人工单关闭后，按目标状态推进报名
        EnrollmentDO enrollment = enrollmentMapper.selectById(task.getEnrollmentId());
        if (enrollment == null) {
            return;
        }
        if (EnrollmentStatusEnum.CANCELLED.getStatus().equals(targetStatus)) {
            cancelEnrollment(enrollment, "人工处理取消：" + reqVO.getHandleResult());
            return;
        }
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(targetStatus);
        enrollmentMapper.updateById(updateEnrollment);
        log.info("[resolveTask] 人工单处理完成, enrollmentId={}, targetStatus={}", enrollment.getId(), targetStatus);
    }

    @Override
    public void closeOpenTask(Long enrollmentId, String taskType, String handleResult, Long handlerId) {
        manualTaskMapper.closeOpenByEnrollmentIdAndType(enrollmentId, taskType, handleResult, handlerId);
    }

    @Override
    public void closeAllOpenTasks(Long enrollmentId, String handleResult, Long handlerId) {
        manualTaskMapper.closeAllOpenByEnrollmentId(enrollmentId, handleResult, handlerId);
    }

    /**
     * 人工取消报名：归还名额
     */
    private void cancelEnrollment(EnrollmentDO enrollment, String reason) {
        if (EnrollmentStatusEnum.CANCELLED.getStatus().equals(enrollment.getEnrollmentStatus())) {
            return;
        }
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
        updateEnrollment.setCancelReason(reason);
        enrollmentMapper.updateById(updateEnrollment);

        courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());

        eventPublisher.publishEvent(new EnrollmentCancelledEvent(this, enrollment.getId(),
                enrollment.getMemberId(), enrollment.getSessionId(), reason));
    }

    private String generateTaskNo() {
        return "MT" + LocalDateTime.now().format(TASK_NO_FMT)
                + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
    }

}
