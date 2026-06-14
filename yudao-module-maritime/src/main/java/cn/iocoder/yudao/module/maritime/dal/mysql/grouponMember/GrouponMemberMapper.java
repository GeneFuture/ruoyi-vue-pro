package cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.*;

/**
 * 拼团成员 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface GrouponMemberMapper extends BaseMapperX<GrouponMemberDO> {

    default PageResult<GrouponMemberDO> selectPage(GrouponMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GrouponMemberDO>()
                .eqIfPresent(GrouponMemberDO::getGrouponRecordId, reqVO.getGrouponRecordId())
                .eqIfPresent(GrouponMemberDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(GrouponMemberDO::getMemberId, reqVO.getMemberId())
                .betweenIfPresent(GrouponMemberDO::getJoinTime, reqVO.getJoinTime())
                .eqIfPresent(GrouponMemberDO::getMemberStatus, reqVO.getMemberStatus())
                .eqIfPresent(GrouponMemberDO::getDepositPaidAt, reqVO.getDepositPaidAt())
                .betweenIfPresent(GrouponMemberDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GrouponMemberDO::getId));
    }

    /** 查某拼团下所有成员，按加入时间升序 */
    default List<GrouponMemberDO> selectListByGrouponRecordId(Long grouponRecordId) {
        return selectList(new LambdaQueryWrapperX<GrouponMemberDO>()
                .eq(GrouponMemberDO::getGrouponRecordId, grouponRecordId)
                .eq(GrouponMemberDO::getMemberStatus, "ACTIVE")
                .orderByAsc(GrouponMemberDO::getJoinTime));
    }

    /** 统计拼团中已支付定金的人数 */
    default long countPaidByGrouponRecordId(Long grouponRecordId) {
        return selectCount(new LambdaQueryWrapperX<GrouponMemberDO>()
                .eq(GrouponMemberDO::getGrouponRecordId, grouponRecordId)
                .eq(GrouponMemberDO::getMemberStatus, "ACTIVE")
                .isNotNull(GrouponMemberDO::getDepositPaidAt));
    }

    /** 按拼团+报名查成员记录（去重校验用） */
    default GrouponMemberDO selectByGrouponAndEnrollment(Long grouponRecordId, Long enrollmentId) {
        return selectOne(new LambdaQueryWrapperX<GrouponMemberDO>()
                .eq(GrouponMemberDO::getGrouponRecordId, grouponRecordId)
                .eq(GrouponMemberDO::getEnrollmentId, enrollmentId));
    }

    /** 记录定金支付时间 */
    @Update("UPDATE maritime_groupon_member SET deposit_paid_at = #{paidAt} WHERE enrollment_id = #{enrollmentId} AND member_status = 'ACTIVE' AND deleted = 0")
    int updateDepositPaidAt(@Param("enrollmentId") Long enrollmentId, @Param("paidAt") LocalDateTime paidAt);

}