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

}