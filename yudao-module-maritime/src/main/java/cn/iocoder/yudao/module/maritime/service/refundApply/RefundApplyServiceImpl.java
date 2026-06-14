package cn.iocoder.yudao.module.maritime.service.refundApply;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.pay.api.refund.PayRefundApi;
import cn.iocoder.yudao.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
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

    private static final DateTimeFormatter REFUND_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String PAY_APP_KEY = "maritime";

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private CommissionRecordMapper commissionRecordMapper;

    @Resource
    private ReferralRelationMapper referralRelationMapper;

    @Resource
    private PayRefundApi payRefundApi;

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

        // 2. 调用微信退款
        String refundNo = "RF" + LocalDateTime.now().format(REFUND_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        PayRefundCreateReqDTO refundReq = new PayRefundCreateReqDTO();
        refundReq.setAppKey(PAY_APP_KEY);
        refundReq.setUserIp(getClientIP());
        refundReq.setUserId(enrollment.getMemberId());
        refundReq.setUserType(UserTypeEnum.MEMBER.getValue());
        refundReq.setMerchantOrderId(targetOrder.getOrderNo());
        refundReq.setMerchantRefundId(refundNo);
        refundReq.setReason(adminRemark != null ? adminRemark : "退费申请审核通过");
        refundReq.setPrice(refundAmount.multiply(BigDecimal.valueOf(100)).intValue());
        Long payRefundId = payRefundApi.createRefund(refundReq);

        // 3. 更新退费申请
        RefundApplyDO updateApply = new RefundApplyDO();
        updateApply.setId(id);
        updateApply.setApplyStatus("REFUNDED");
        updateApply.setRefundAmount(refundAmount);
        updateApply.setApproverId(approverId);
        updateApply.setApproveTime(LocalDateTime.now());
        updateApply.setAdminRemark(adminRemark);
        updateApply.setPayRefundId(payRefundId);
        updateApply.setRefundTime(LocalDateTime.now());
        refundApplyMapper.updateById(updateApply);

        // 4. 更新报名状态 → CANCELLED
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
        updateEnrollment.setCancelReason("退费申请通过，退款金额：" + refundAmount + " 元");
        enrollmentMapper.updateById(updateEnrollment);

        // 5. 归还班期名额
        courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());

        // 6. 标记推荐关系无效
        int invalidated = referralRelationMapper.invalidateByEnrollmentId(
                enrollment.getId(), "退费取消，推荐关系无效");
        if (invalidated > 0) {
            log.info("[approveRefund] 推荐关系已标记无效, enrollmentId={}", enrollment.getId());
        }

        // 7. 冻结对应佣金（仅 WAITING_FOR_CLASS / PENDING_REVIEW 状态）
        CommissionRecordDO commission = commissionRecordMapper.selectByReferredEnrollmentId(enrollment.getId());
        if (commission != null) {
            String commStatus = commission.getCommissionStatus();
            if ("WAITING_FOR_CLASS".equals(commStatus) || "PENDING_REVIEW".equals(commStatus)) {
                commissionRecordMapper.updateStatusById(commission.getId(), "FROZEN");
                log.info("[approveRefund] 佣金已冻结, commissionId={}, enrollmentId={}",
                        commission.getId(), enrollment.getId());
            }
        }
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
    }

}
