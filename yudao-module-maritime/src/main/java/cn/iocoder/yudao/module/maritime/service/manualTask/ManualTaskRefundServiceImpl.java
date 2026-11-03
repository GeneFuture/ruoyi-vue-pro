package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundResolveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.manualTask.ManualTaskMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 人工处理单 - 退费审核 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class ManualTaskRefundServiceImpl implements ManualTaskRefundService {

    private static final String RESULT_APPROVE = "APPROVE";
    private static final String RESULT_REJECT = "REJECT";

    @Resource
    private ManualTaskMapper manualTaskMapper;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private RefundApplyService refundApplyService;

    @Override
    public ManualTaskRefundDetailRespVO getRefundDetail(Long taskId) {
        ManualTaskDO task = validateRefundTask(taskId);
        RefundApplyDO apply = validateRefundApply(task);

        ManualTaskRefundDetailRespVO vo = new ManualTaskRefundDetailRespVO();
        vo.setTaskId(task.getId());
        vo.setTaskNo(task.getTaskNo());
        vo.setTaskType(task.getTaskType());
        vo.setTaskStatus(task.getStatus());
        vo.setTaskReason(task.getReason());
        vo.setEnrollmentId(task.getEnrollmentId());
        vo.setMemberId(task.getMemberId());
        Long orderId = task.getOrderId() != null ? task.getOrderId() : apply.getOrderId();
        vo.setOrderId(orderId);

        if (orderId != null) {
            EnrollmentOrderDO order = enrollmentOrderMapper.selectById(orderId);
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
                vo.setOrderAmount(order.getAmount());
                vo.setOrderStatus(order.getOrderStatus());
            }
        }

        vo.setRefundApplyId(apply.getId());
        vo.setApplyStatus(apply.getApplyStatus());
        vo.setApplyReason(apply.getApplyReason());
        vo.setRefundAmount(apply.getRefundAmount());
        vo.setAdminRemark(apply.getAdminRemark());
        vo.setApproveTime(apply.getApproveTime());
        vo.setApproverId(apply.getApproverId());
        vo.setRefundTime(apply.getRefundTime());
        vo.setCreateTime(apply.getCreateTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveRefundTask(ManualTaskRefundResolveReqVO reqVO, Long handlerId) {
        ManualTaskDO task = validateOpenRefundTask(reqVO.getId());
        RefundApplyDO apply = validateRefundApply(task);

        if (RESULT_APPROVE.equals(reqVO.getResult())) {
            if (reqVO.getRefundAmount() == null || reqVO.getRefundAmount().signum() <= 0) {
                throw exception(MANUAL_TASK_REFUND_AMOUNT_REQUIRED);
            }
            refundApplyService.approveRefund(apply.getId(), reqVO.getRefundAmount(),
                    handlerId, reqVO.getAdminRemark());
            log.info("[resolveRefundTask] 退费审核通过, taskId={}, refundApplyId={}, amount={}",
                    task.getId(), apply.getId(), reqVO.getRefundAmount());
            return;
        }
        if (RESULT_REJECT.equals(reqVO.getResult())) {
            if (StrUtil.isBlank(reqVO.getRejectReason())) {
                throw exception(MANUAL_TASK_REJECT_REASON_REQUIRED);
            }
            refundApplyService.rejectRefund(apply.getId(), handlerId, reqVO.getRejectReason());
            log.info("[resolveRefundTask] 退费审核拒绝, taskId={}, refundApplyId={}",
                    task.getId(), apply.getId());
            return;
        }
        throw exception(MANUAL_TASK_REFUND_RESULT_INVALID);
    }

    /**
     * 校验人工单存在且属于退费类型
     */
    private ManualTaskDO validateRefundTask(Long taskId) {
        ManualTaskDO task = manualTaskMapper.selectById(taskId);
        if (task == null) {
            throw exception(MANUAL_TASK_NOT_EXISTS);
        }
        if (!ManualTaskTypeEnum.REFUND_APPLY.getType().equals(task.getTaskType())
                && !ManualTaskTypeEnum.REFUND_AMOUNT_CONFIRM.getType().equals(task.getTaskType())) {
            throw exception(MANUAL_TASK_NOT_REFUND);
        }
        return task;
    }

    /**
     * 校验人工单仍处于未关闭状态（审核操作要求）
     */
    private ManualTaskDO validateOpenRefundTask(Long taskId) {
        ManualTaskDO task = validateRefundTask(taskId);
        if (!ManualTaskStatusEnum.PENDING.getStatus().equals(task.getStatus())
                && !ManualTaskStatusEnum.PROCESSING.getStatus().equals(task.getStatus())) {
            throw exception(MANUAL_TASK_ALREADY_CLOSED);
        }
        return task;
    }

    /**
     * 校验人工单关联的退费申请
     */
    private RefundApplyDO validateRefundApply(ManualTaskDO task) {
        if (task.getSourceId() == null) {
            throw exception(MANUAL_TASK_REFUND_APPLY_MISSING);
        }
        RefundApplyDO apply = refundApplyMapper.selectById(task.getSourceId());
        if (apply == null) {
            throw exception(REFUND_APPLY_NOT_EXISTS);
        }
        return apply;
    }

}
