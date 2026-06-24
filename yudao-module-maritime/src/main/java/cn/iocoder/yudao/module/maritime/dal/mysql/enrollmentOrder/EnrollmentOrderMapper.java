package cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;

import java.math.BigDecimal;

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

    /** 批量查指定报名ID的已付订单（导出用，避免 N+1） */
    default List<EnrollmentOrderDO> selectPaidByEnrollmentIds(Collection<Long> enrollmentIds) {
        if (enrollmentIds == null || enrollmentIds.isEmpty()) return Collections.emptyList();
        return selectList(new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .in(EnrollmentOrderDO::getEnrollmentId, enrollmentIds)
                .eq(EnrollmentOrderDO::getOrderStatus, "PAID"));
    }

    /** 统计指定时间段内指定订单类型的已付金额合计（Dashboard/财务汇总用） */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM maritime_enrollment_order " +
            "WHERE order_type = #{orderType} AND order_status = 'PAID' " +
            "AND pay_time >= #{start} AND pay_time <= #{end} AND deleted = 0")
    BigDecimal sumPaidAmountByTypeAndPayTimeBetween(@Param("orderType") String orderType,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    /** 汇总所有班期的各类型已付金额（全历史，Dashboard/非月份用途） */
    @Select("SELECT e.session_id AS sessionId, o.order_type AS orderType, COALESCE(SUM(o.amount), 0) AS total " +
            "FROM maritime_enrollment_order o " +
            "INNER JOIN maritime_enrollment e ON e.id = o.enrollment_id AND e.deleted = 0 " +
            "WHERE o.order_status = 'PAID' AND o.deleted = 0 " +
            "GROUP BY e.session_id, o.order_type")
    List<Map<String, Object>> sumAllPaidAmountGroupBySessionAndType();

    /** 汇总指定月份内各班期的各类型已付金额（财务汇总月度明细用） */
    @Select("SELECT e.session_id AS sessionId, o.order_type AS orderType, COALESCE(SUM(o.amount), 0) AS total " +
            "FROM maritime_enrollment_order o " +
            "INNER JOIN maritime_enrollment e ON e.id = o.enrollment_id AND e.deleted = 0 " +
            "WHERE o.order_status = 'PAID' AND o.deleted = 0 " +
            "AND o.pay_time >= #{start} AND o.pay_time <= #{end} " +
            "GROUP BY e.session_id, o.order_type")
    List<Map<String, Object>> sumPaidAmountGroupBySessionAndTypeBetween(@Param("start") LocalDateTime start,
                                                                         @Param("end") LocalDateTime end);

    /** 汇总所有班期的退款金额（全历史） */
    @Select("SELECT e.session_id AS sessionId, COALESCE(SUM(o.amount), 0) AS total " +
            "FROM maritime_enrollment_order o " +
            "INNER JOIN maritime_enrollment e ON e.id = o.enrollment_id AND e.deleted = 0 " +
            "WHERE o.order_status = 'REFUNDED' AND o.deleted = 0 " +
            "GROUP BY e.session_id")
    List<Map<String, Object>> sumAllRefundedAmountGroupBySession();

    /** 汇总指定月份内各班期的退款金额（财务汇总月度明细用） */
    @Select("SELECT e.session_id AS sessionId, COALESCE(SUM(o.amount), 0) AS total " +
            "FROM maritime_enrollment_order o " +
            "INNER JOIN maritime_enrollment e ON e.id = o.enrollment_id AND e.deleted = 0 " +
            "WHERE o.order_status = 'REFUNDED' AND o.deleted = 0 " +
            "AND o.update_time >= #{start} AND o.update_time <= #{end} " +
            "GROUP BY e.session_id")
    List<Map<String, Object>> sumRefundedAmountGroupBySessionBetween(@Param("start") LocalDateTime start,
                                                                      @Param("end") LocalDateTime end);

    /** 批量汇总指定班期列表的各类型已付金额（Dashboard SessionProgress 批量用，避免 N+1） */
    @Select("<script>SELECT e.session_id AS sessionId, o.order_type AS orderType, COALESCE(SUM(o.amount), 0) AS total " +
            "FROM maritime_enrollment_order o " +
            "INNER JOIN maritime_enrollment e ON e.id = o.enrollment_id AND e.deleted = 0 " +
            "WHERE e.session_id IN " +
            "<foreach collection='sessionIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND o.order_status = 'PAID' AND o.deleted = 0 " +
            "GROUP BY e.session_id, o.order_type</script>")
    List<Map<String, Object>> sumPaidAmountGroupBySessionAndTypeForSessionIds(@Param("sessionIds") List<Long> sessionIds);

    /** 按班期ID汇总各类型已付金额（Session Progress 单班期用） */
    @Select("SELECT order_type, COALESCE(SUM(amount), 0) AS total " +
            "FROM maritime_enrollment_order " +
            "WHERE enrollment_id IN (SELECT id FROM maritime_enrollment WHERE session_id = #{sessionId} AND deleted = 0) " +
            "AND order_status = 'PAID' AND deleted = 0 GROUP BY order_type")
    List<Map<String, Object>> sumPaidAmountBySessionIdGroupByType(@Param("sessionId") Long sessionId);

}