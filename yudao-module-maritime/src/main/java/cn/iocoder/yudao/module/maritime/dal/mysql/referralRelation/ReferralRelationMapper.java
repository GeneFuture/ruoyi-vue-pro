package cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import org.apache.ibatis.annotations.Mapper;
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

    /** 退款时将该报名的推荐关系标记为 INVALID */
    default int invalidateByEnrollmentId(Long enrollmentId, String invalidReason) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ReferralRelationDO>()
                .set(ReferralRelationDO::getRelationStatus, "INVALID")
                .set(ReferralRelationDO::getInvalidReason, invalidReason)
                .eq(ReferralRelationDO::getReferredEnrollmentId, enrollmentId)
                .eq(ReferralRelationDO::getRelationStatus, "ACTIVE")
                .eq(ReferralRelationDO::getDeleted, false));
    }

}