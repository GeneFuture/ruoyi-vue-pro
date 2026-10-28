package cn.iocoder.yudao.module.maritime.service.refundApply;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.maritime.mq.event.RefundResultEvent;
import cn.iocoder.yudao.module.maritime.service.manualTask.ManualTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 退费申请 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class RefundApplyServiceImpl implements RefundApplyService {

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private ManualTaskService manualTaskService;

    // ========== Admin CRUD ==========

    @Override
    public Long createRefundApply(RefundApplySaveReqVO createReqVO) {
        RefundApplyDO refundApply = BeanUtils.toBean(createReqVO, RefundApplyDO.class);
        refundApplyMapper.insert(refundApply);
        return refundApply.getId();
    }

    @Override
    public void updateRefundApply(RefundApplySaveReqVO updateReqVO) {
        validateRefundApplyExists(updateReqVO.getId());
        RefundApplyDO updateObj = BeanUtils.toBean(updateReqVO, RefundApplyDO.class);
        refundApplyMapper.updateById(updateObj);
    }

    @Override
    public void deleteRefundApply(Long id) {
        validateRefundApplyExists(id);
        refundApplyMapper.deleteById(id);
    }

    @Override
    public void deleteRefundApplyListByIds(List<Long> ids) {
        refundApplyMapper.deleteByIds(ids);
    }

    private void validateRefundApplyExists(Long id) {
        if (refundApplyMapper.selectById(id) == null) {
            throw exception(REFUND_APPLY_NOT_EXISTS);
        }
    }

    @Override
    public RefundApplyDO getRefundApply(Long id) {
        return refundApplyMapper.selectById(id);
    }

    @Override
    public PageResult<RefundApplyDO> getRefundApplyPage(RefundApplyPageReqVO pageReqVO) {
        return refundApplyMapper.selectPage(pageReqVO);
    }

    // ========== 管理端 T07 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long id, BigDecimal refundAmount, Long approverId, String adminRemark) {
        RefundApplyDO apply = refundApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(REFUND_APPLY_NOT_EXISTS);
        }
        if (!"PENDING".equals(apply.getApplyStatus())) {
            throw exception(REFUND_NOT_ALLOWED);
        }

        EnrollmentDO enrollment = enrollmentMapper.selectById(apply.getEnrollmentId());
        if (enrollment == null) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        // 1. 找要退款的订单
        EnrollmentOrderDO targetOrder = enrollmentOrderMapper.selectById(apply.getOrderId());
        if (targetOrder == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }

        // 2. 更新退费申请（退款由人工在微信商户平台完成后，后台登记结果）
        RefundApplyDO updateApply = new RefundApplyDO();
        updateApply.setId(id);
        updateApply.setApplyStatus("REFUNDED");
        updateApply.setRefundAmount(refundAmount);
        updateApply.setApproverId(approverId);
        updateApply.setApproveTime(LocalDateTime.now());
        updateApply.setAdminRemark(adminRemark);
        updateApply.setRefundTime(LocalDateTime.now());
        refundApplyMapper.updateById(updateApply);

        // 3. 标记订单已退款：全额退款标记所有已支付订单，部分退款标记对应订单
        List<EnrollmentOrderDO> orders = enrollmentOrderMapper.selectListByEnrollmentId(enrollment.getId());
        boolean refundAll = refundAmount.compareTo(enrollment.getPaidAmount()) >= 0;
        for (EnrollmentOrderDO order : orders) {
            if (!OrderStatusEnum.PAID.getStatus().equals(order.getOrderStatus())) {
                continue;
            }
            boolean shouldMark = refundAll
                    || (order.getId().equals(targetOrder.getId())
                    && refundAmount.compareTo(order.getAmount()) >= 0);
            if (shouldMark) {
                EnrollmentOrderDO updateOrder = new EnrollmentOrderDO();
                updateOrder.setId(order.getId());
                updateOrder.setOrderStatus(OrderStatusEnum.REFUNDED.getStatus());
                enrollmentOrderMapper.updateById(updateOrder);
            }
        }

        // 4. 更新报名状态 → CANCELLED
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
        updateEnrollment.setCancelReason("退费申请通过，退款金额：" + refundAmount + " 元");
        enrollmentMapper.updateById(updateEnrollment);

        // 5. 归还班期名额
        courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());

        // 6. 关闭该报名下的人工处理单
        manualTaskService.closeAllOpenTasks(enrollment.getId(),
                "退费已处理，退款金额 " + refundAmount + " 元", approverId);

        eventPublisher.publishEvent(new RefundResultEvent(this, enrollment.getId(),
                enrollment.getMemberId(), enrollment.getSessionId(), refundAmount, true, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(Long id, Long approverId, String rejectReason) {
        RefundApplyDO apply = refundApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(REFUND_APPLY_NOT_EXISTS);
        }
        if (!"PENDING".equals(apply.getApplyStatus())) {
            throw exception(REFUND_NOT_ALLOWED);
        }

        RefundApplyDO updateApply = new RefundApplyDO();
        updateApply.setId(id);
        updateApply.setApplyStatus("REJECTED");
        updateApply.setApproverId(approverId);
        updateApply.setApproveTime(LocalDateTime.now());
        updateApply.setAdminRemark(rejectReason);
        refundApplyMapper.updateById(updateApply);

        // 关闭退费人工单；若还有其它未关闭人工单，报名继续保持 MANUAL_PROCESSING
        EnrollmentDO enrollment = enrollmentMapper.selectById(apply.getEnrollmentId());
        if (enrollment == null) {
            return;
        }
        manualTaskService.closeOpenTask(enrollment.getId(), ManualTaskTypeEnum.REFUND_APPLY.getType(),
                "退费申请被拒绝：" + rejectReason, approverId);
        eventPublisher.publishEvent(new RefundResultEvent(this, enrollment.getId(),
                enrollment.getMemberId(), enrollment.getSessionId(),
                apply.getRefundAmount(), false, rejectReason));
        if (!manualTaskService.getOpenTasks(enrollment.getId()).isEmpty()) {
            return;
        }
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(deriveEnrollmentStatus(enrollment.getId()));
        enrollmentMapper.updateById(updateEnrollment);
    }

    /**
     * 根据已支付订单推导报名应恢复的状态
     */
    private String deriveEnrollmentStatus(Long enrollmentId) {
        if (enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollmentId, OrderTypeEnum.BALANCE.getType(), OrderStatusEnum.PAID.getStatus()) != null) {
            return EnrollmentStatusEnum.PAID_OFF.getStatus();
        }
        if (enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollmentId, OrderTypeEnum.FULL.getType(), OrderStatusEnum.PAID.getStatus()) != null) {
            return EnrollmentStatusEnum.PAID_OFF.getStatus();
        }
        if (enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollmentId, OrderTypeEnum.DEPOSIT.getType(), OrderStatusEnum.PAID.getStatus()) != null) {
            return EnrollmentStatusEnum.DEPOSITED.getStatus();
        }
        return EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus();
    }

}
