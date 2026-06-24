package cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.*;

/**
 * 推荐关系 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface ReferralRelationMapper extends BaseMapperX<ReferralRelationDO> {

    default PageResult<ReferralRelationDO> selectPage(ReferralRelationPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReferralRelationDO>()
                .eqIfPresent(ReferralRelationDO::getReferrerMemberId, reqVO.getReferrerMemberId())
                .eqIfPresent(ReferralRelationDO::getReferredEnrollmentId, reqVO.getReferredEnrollmentId())
                .eqIfPresent(ReferralRelationDO::getReferredMemberId, reqVO.getReferredMemberId())
                .eqIfPresent(ReferralRelationDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(ReferralRelationDO::getRelationStatus, reqVO.getRelationStatus())
                .eqIfPresent(ReferralRelationDO::getInvalidReason, reqVO.getInvalidReason())
                .eqIfPresent(ReferralRelationDO::getFirstClickAt, reqVO.getFirstClickAt())
                .eqIfPresent(ReferralRelationDO::getReferralLockedAt, reqVO.getReferralLockedAt())
                .betweenIfPresent(ReferralRelationDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ReferralRelationDO::getId));
    }

    default ReferralRelationDO selectByReferredEnrollmentId(Long enrollmentId) {
        return selectOne(ReferralRelationDO::getReferredEnrollmentId, enrollmentId);
    }

    /** 推荐人的全部推荐数（含 ACTIVE + INVALID） */
    default long countByReferrerMemberId(Long memberId) {
        return selectCount(ReferralRelationDO::getReferrerMemberId, memberId);
    }

    /** 推荐人的推荐记录分页（App 端） */
    default cn.iocoder.yudao.framework.common.pojo.PageResult<ReferralRelationDO> selectPageByReferrerMemberId(
            Long memberId, cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<ReferralRelationDO>()
                .eq(ReferralRelationDO::getReferrerMemberId, memberId)
                .orderByDesc(ReferralRelationDO::getId));
    }

    /** 统计推荐人在指定时间之后建立的有效推荐数（Rule 3 批量推荐检测用） */
    default long countActiveByReferrerSince(Long referrerId, LocalDateTime since) {
        return selectCount(new LambdaQueryWrapperX<ReferralRelationDO>()
                .eq(ReferralRelationDO::getReferrerMemberId, referrerId)
                .eq(ReferralRelationDO::getRelationStatus, "ACTIVE")
                .ge(ReferralRelationDO::getCreateTime, since)
                .eq(ReferralRelationDO::getDeleted, false));
    }

    /** 退款时将该报名的推荐关系标记为 INVALID */
    default int invalidateByEnrollmentId(Long enrollmentId, String invalidReason) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ReferralRelationDO>()
                .set(ReferralRelationDO::getRelationStatus, "INVALID")
                .set(ReferralRelationDO::getInvalidReason, invalidReason)
                .eq(ReferralRelationDO::getReferredEnrollmentId, enrollmentId)
                .eq(ReferralRelationDO::getRelationStatus, "ACTIVE")
                .eq(ReferralRelationDO::getDeleted, false));
    }

    /** 统计指定时间段内新增的有效推荐数（Dashboard 本月推荐数用） */
    @Select("SELECT COUNT(*) FROM maritime_referral_relation " +
            "WHERE relation_status = 'ACTIVE' AND create_time >= #{start} AND create_time <= #{end} AND deleted = 0")
    int countActiveByCreateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 批量查指定报名ID的推荐关系（导出用，避免 N+1） */
    default List<ReferralRelationDO> selectListByReferredEnrollmentIds(Collection<Long> enrollmentIds) {
        if (enrollmentIds == null || enrollmentIds.isEmpty()) return Collections.emptyList();
        return selectList(new LambdaQueryWrapperX<ReferralRelationDO>()
                .in(ReferralRelationDO::getReferredEnrollmentId, enrollmentIds));
    }

}