package cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo.*;

import java.util.Collection;

/**
 * 班期费用配置 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface SessionFeeConfigMapper extends BaseMapperX<SessionFeeConfigDO> {

    default PageResult<SessionFeeConfigDO> selectPage(SessionFeeConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SessionFeeConfigDO>()
                .eqIfPresent(SessionFeeConfigDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(SessionFeeConfigDO::getTuitionAmount, reqVO.getTuitionAmount())
                .eqIfPresent(SessionFeeConfigDO::getDepositAmount, reqVO.getDepositAmount())
                .eqIfPresent(SessionFeeConfigDO::getIsGrouponEnabled, reqVO.getIsGrouponEnabled())
                .eqIfPresent(SessionFeeConfigDO::getGrouponDiscountAmount, reqVO.getGrouponDiscountAmount())
                .eqIfPresent(SessionFeeConfigDO::getGrouponRequiredCount, reqVO.getGrouponRequiredCount())
                .eqIfPresent(SessionFeeConfigDO::getGrouponExpireHours, reqVO.getGrouponExpireHours())
                .eqIfPresent(SessionFeeConfigDO::getGrouponFailDiscountAmount, reqVO.getGrouponFailDiscountAmount())
                .eqIfPresent(SessionFeeConfigDO::getReferralCommissionAmount, reqVO.getReferralCommissionAmount())
                .eqIfPresent(SessionFeeConfigDO::getIsReferralCommissionEnabled, reqVO.getIsReferralCommissionEnabled())
                .eqIfPresent(SessionFeeConfigDO::getIsDepositRefundable, reqVO.getIsDepositRefundable())
                .eqIfPresent(SessionFeeConfigDO::getBalanceDueDaysBeforeStart, reqVO.getBalanceDueDaysBeforeStart())
                .eqIfPresent(SessionFeeConfigDO::getRefundPolicyText, reqVO.getRefundPolicyText())
                .betweenIfPresent(SessionFeeConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SessionFeeConfigDO::getId));
    }

    /** 按班期ID查询费用配置（一个班期只有一条） */
    default SessionFeeConfigDO selectBySessionId(Long sessionId) {
        return selectOne(SessionFeeConfigDO::getSessionId, sessionId);
    }

    /** 批量按班期ID查询费用配置 */
    default List<SessionFeeConfigDO> selectBySessionIds(Collection<Long> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<SessionFeeConfigDO>()
                .in(SessionFeeConfigDO::getSessionId, sessionIds));
    }

}