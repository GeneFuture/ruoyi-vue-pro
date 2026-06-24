package cn.iocoder.yudao.module.maritime.dal.mysql.refundApply;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退费申请 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface RefundApplyMapper extends BaseMapperX<RefundApplyDO> {

    default PageResult<RefundApplyDO> selectPage(RefundApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RefundApplyDO>()
                .eqIfPresent(RefundApplyDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(RefundApplyDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(RefundApplyDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(RefundApplyDO::getApplyReason, reqVO.getApplyReason())
                .eqIfPresent(RefundApplyDO::getRefundAmount, reqVO.getRefundAmount())
                .eqIfPresent(RefundApplyDO::getApplyStatus, reqVO.getApplyStatus())
                .eqIfPresent(RefundApplyDO::getAdminRemark, reqVO.getAdminRemark())
                .betweenIfPresent(RefundApplyDO::getApproveTime, reqVO.getApproveTime())
                .eqIfPresent(RefundApplyDO::getApproverId, reqVO.getApproverId())
                .eqIfPresent(RefundApplyDO::getPayRefundId, reqVO.getPayRefundId())
                .betweenIfPresent(RefundApplyDO::getRefundTime, reqVO.getRefundTime())
                .betweenIfPresent(RefundApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RefundApplyDO::getId));
    }

    /** 查报名下的活跃退费申请（PENDING/APPROVED/REFUNDED，用于防重复提交） */
    default RefundApplyDO selectActiveByEnrollmentId(Long enrollmentId) {
        return selectOne(new LambdaQueryWrapperX<RefundApplyDO>()
                .eq(RefundApplyDO::getEnrollmentId, enrollmentId)
                .in(RefundApplyDO::getApplyStatus, "PENDING", "APPROVED", "REFUNDED"));
    }

    /** 查报名下待审核的退费申请 */
    default RefundApplyDO selectPendingByEnrollmentId(Long enrollmentId) {
        return selectOne(new LambdaQueryWrapperX<RefundApplyDO>()
                .eq(RefundApplyDO::getEnrollmentId, enrollmentId)
                .eq(RefundApplyDO::getApplyStatus, "PENDING"));
    }

    /** 开课 7 天窗口内是否存在退费申请（用于结算检查） */
    @Select("SELECT COUNT(*) > 0 FROM maritime_refund_apply " +
            "WHERE enrollment_id = #{enrollmentId} AND deleted = 0 AND create_time < #{beforeDate}")
    boolean existsByEnrollmentIdAndCreateTimeBefore(@Param("enrollmentId") Long enrollmentId,
                                                    @Param("beforeDate") LocalDate beforeDate);

    /** 统计指定状态的退费申请数（Dashboard 待办用） */
    @Select("SELECT COUNT(*) FROM maritime_refund_apply WHERE apply_status = #{status} AND deleted = 0")
    int countByApplyStatus(@Param("status") String status);

    /** 统计指定时间段内已退款金额合计（财务汇总用） */
    @Select("SELECT COALESCE(SUM(refund_amount), 0) FROM maritime_refund_apply " +
            "WHERE apply_status = 'REFUNDED' AND refund_time >= #{start} AND refund_time <= #{end} AND deleted = 0")
    BigDecimal sumRefundedAmountByRefundTimeBetween(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

}