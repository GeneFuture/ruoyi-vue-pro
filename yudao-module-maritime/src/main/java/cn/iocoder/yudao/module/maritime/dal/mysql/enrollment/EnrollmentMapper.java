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

}