package cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;

/**
 * 报名订单 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface EnrollmentOrderMapper extends BaseMapperX<EnrollmentOrderDO> {

    default PageResult<EnrollmentOrderDO> selectPage(EnrollmentOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eqIfPresent(EnrollmentOrderDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(EnrollmentOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(EnrollmentOrderDO::getOrderType, reqVO.getOrderType())
                .eqIfPresent(EnrollmentOrderDO::getAmount, reqVO.getAmount())
                .eqIfPresent(EnrollmentOrderDO::getPayOrderId, reqVO.getPayOrderId())
                .eqIfPresent(EnrollmentOrderDO::getPayChannel, reqVO.getPayChannel())
                .eqIfPresent(EnrollmentOrderDO::getOrderStatus, reqVO.getOrderStatus())
                .betweenIfPresent(EnrollmentOrderDO::getPayTime, reqVO.getPayTime())
                .betweenIfPresent(EnrollmentOrderDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(EnrollmentOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnrollmentOrderDO::getId));
    }

    /** 查报名下所有订单，按创建时间倒序 */
    default List<EnrollmentOrderDO> selectListByEnrollmentId(Long enrollmentId) {
        return selectList(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eq(EnrollmentOrderDO::getEnrollmentId, enrollmentId)
                .orderByDesc(EnrollmentOrderDO::getCreateTime));
    }

    /** 查报名下待支付订单（PENDING 状态） */
    default List<EnrollmentOrderDO> selectPendingByEnrollmentId(Long enrollmentId) {
        return selectList(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eq(EnrollmentOrderDO::getEnrollmentId, enrollmentId)
                .eq(EnrollmentOrderDO::getOrderStatus, "PENDING"));
    }

    /** 按订单号查（支付回调用） */
    default EnrollmentOrderDO selectByOrderNo(String orderNo) {
        return selectOne(EnrollmentOrderDO::getOrderNo, orderNo);
    }

    /** 按报名ID + 订单类型查唯一订单 */
    default EnrollmentOrderDO selectByEnrollmentIdAndType(Long enrollmentId, String orderType) {
        return selectOne(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eq(EnrollmentOrderDO::getEnrollmentId, enrollmentId)
                .eq(EnrollmentOrderDO::getOrderType, orderType));
    }

    /** 按报名ID + 订单类型 + 订单状态查（退款时找已支付订单） */
    default EnrollmentOrderDO selectByEnrollmentIdAndTypeAndStatus(Long enrollmentId, String orderType, String orderStatus) {
        return selectOne(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eq(EnrollmentOrderDO::getEnrollmentId, enrollmentId)
                .eq(EnrollmentOrderDO::getOrderType, orderType)
                .eq(EnrollmentOrderDO::getOrderStatus, orderStatus));
    }

    /** 查已过期的 PENDING 订单（定时任务用） */
    default List<EnrollmentOrderDO> selectExpiredPendingOrders(LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eq(EnrollmentOrderDO::getOrderStatus, "PENDING")
                .lt(EnrollmentOrderDO::getExpireTime, now));
    }

    /** CAS 将 PENDING → newStatus（0 表示已被处理，忽略即可） */
    @Update("UPDATE maritime_enrollment_order SET order_status = #{newStatus} WHERE id = #{id} AND order_status = 'PENDING' AND deleted = 0")
    int updateStatusIfPending(@Param("id") Long id, @Param("newStatus") String newStatus);

    /** 绑定 pay 模块订单ID */
    @Update("UPDATE maritime_enrollment_order SET pay_order_id = #{payOrderId} WHERE id = #{id} AND deleted = 0")
    int updatePayOrderId(@Param("id") Long id, @Param("payOrderId") Long payOrderId);

    /** 按 pay 模块订单ID 查（前端支付状态轮询用） */
    default EnrollmentOrderDO selectByPayOrderId(Long payOrderId) {
        return selectOne(EnrollmentOrderDO::getPayOrderId, payOrderId);
    }

}