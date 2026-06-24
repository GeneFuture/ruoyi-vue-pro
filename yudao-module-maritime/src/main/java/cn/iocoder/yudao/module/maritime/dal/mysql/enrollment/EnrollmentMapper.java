package cn.iocoder.yudao.module.maritime.dal.mysql.enrollment;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;
import java.time.LocalDateTime;

/**
 * 报名管理 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface EnrollmentMapper extends BaseMapperX<EnrollmentDO> {

    default PageResult<EnrollmentDO> selectPage(EnrollmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnrollmentDO>()
                .eqIfPresent(EnrollmentDO::getEnrollmentNo, reqVO.getEnrollmentNo())
                .eqIfPresent(EnrollmentDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(EnrollmentDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(EnrollmentDO::getGrouponRecordId, reqVO.getGrouponRecordId())
                .eqIfPresent(EnrollmentDO::getReferredByMemberId, reqVO.getReferredByMemberId())
                .eqIfPresent(EnrollmentDO::getReferralCodeUsed, reqVO.getReferralCodeUsed())
                .likeIfPresent(EnrollmentDO::getRealName, reqVO.getRealName())
                .eqIfPresent(EnrollmentDO::getPhone, reqVO.getPhone())
                .eqIfPresent(EnrollmentDO::getEnrollmentStatus, reqVO.getEnrollmentStatus())
                .betweenIfPresent(EnrollmentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnrollmentDO::getId));
    }

    /** 按加密后的身份证 + 班期ID 查（用于重复报名检查） */
    default EnrollmentDO selectByIdCardAndSession(String encryptedIdCard, Long sessionId) {
        return selectOne(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getIdCard, encryptedIdCard)
                .eq(EnrollmentDO::getSessionId, sessionId)
                .ne(EnrollmentDO::getEnrollmentStatus, "CANCELLED"));
    }

    /** 查某学员的所有报名，按创建时间倒序 */
    default List<EnrollmentDO> selectListByMemberId(Long memberId) {
        return selectList(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getMemberId, memberId)
                .orderByDesc(EnrollmentDO::getCreateTime));
    }

    /** 绑定拼团记录ID */
    @Update("UPDATE maritime_enrollment SET groupon_record_id = #{grouponRecordId} WHERE id = #{id} AND deleted = 0")
    int updateGrouponRecordId(@Param("id") Long id, @Param("grouponRecordId") Long grouponRecordId);

    /** 查询会员是否已获得推荐权（支付定金后 referral_right_granted=1） */
    default boolean hasReferralRight(Long memberId) {
        return selectCount(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getMemberId, memberId)
                .eq(EnrollmentDO::getReferralRightGranted, true)) > 0;
    }

    /** 更新报名状态（幂等，CAS 由调用方保证） */
    @Update("UPDATE maritime_enrollment SET enrollment_status = #{newStatus} WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("newStatus") String newStatus);

    /** 查询 DEPOSITED 状态且班期开始日期在指定日期之前的报名（尾款催缴用） */
    default List<EnrollmentDO> selectDepositedWithBalanceDueBefore(LocalDate dueDate) {
        return selectList(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getEnrollmentStatus, "DEPOSITED")
                .le(EnrollmentDO::getBalanceDueDate, dueDate));
    }

    /** 获取会员最早完成定金支付的时间（提现冻结期校验用） */
    @Select("SELECT MIN(o.pay_time) FROM maritime_enrollment e " +
            "INNER JOIN maritime_enrollment_order o ON o.enrollment_id = e.id " +
            "WHERE e.member_id = #{memberId} AND o.order_type = 'DEPOSIT' " +
            "AND o.order_status = 'PAID' AND e.deleted = 0 AND o.deleted = 0")
    LocalDateTime getFirstDepositTime(@Param("memberId") Long memberId);

    /** 统计指定时间段内的新报名数（Dashboard 用） */
    @Select("SELECT COUNT(*) FROM maritime_enrollment WHERE create_time >= #{start} AND create_time <= #{end} AND deleted = 0")
    int countByCreateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 统计指定班期的指定状态报名数（Dashboard SessionProgress 用，单班期） */
    @Select("SELECT COUNT(*) FROM maritime_enrollment WHERE session_id = #{sessionId} AND enrollment_status = #{status} AND deleted = 0")
    int countBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") String status);

    /** 批量统计多班期的已完成报名数（Dashboard SessionProgress 批量用，避免 N+1） */
    @Select("<script>SELECT session_id AS sessionId, COUNT(*) AS cnt " +
            "FROM maritime_enrollment WHERE session_id IN " +
            "<foreach collection='sessionIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND enrollment_status = 'COMPLETED' AND deleted = 0 " +
            "GROUP BY session_id</script>")
    List<Map<String, Object>> countCompletedGroupBySessionIds(@Param("sessionIds") List<Long> sessionIds);

    /** 查询近 N 天每日报名数（趋势图用），返回 Map 列表，key: stat_date, value: cnt */
    @Select("SELECT DATE(create_time) AS stat_date, COUNT(*) AS cnt " +
            "FROM maritime_enrollment WHERE create_time >= #{since} AND deleted = 0 " +
            "GROUP BY DATE(create_time) ORDER BY stat_date ASC")
    List<Map<String, Object>> selectDailyEnrollmentCounts(@Param("since") LocalDateTime since);

    /** 查某班期全部有效报名（导出用，排除已取消），LIMIT 10001 防止全表扫描 */
    default List<EnrollmentDO> selectListBySessionIdForExport(Long sessionId) {
        return selectList(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getSessionId, sessionId)
                .ne(EnrollmentDO::getEnrollmentStatus, "CANCELLED")
                .orderByAsc(EnrollmentDO::getId)
                .last("LIMIT 10001"));
    }

}