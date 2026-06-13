package cn.iocoder.yudao.module.maritime.dal.mysql.commissionAccount;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;

/**
 * 佣金账户 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface CommissionAccountMapper extends BaseMapperX<CommissionAccountDO> {

    default PageResult<CommissionAccountDO> selectPage(CommissionAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CommissionAccountDO>()
                .eqIfPresent(CommissionAccountDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(CommissionAccountDO::getTotalAmount, reqVO.getTotalAmount())
                .eqIfPresent(CommissionAccountDO::getPaidAmount, reqVO.getPaidAmount())
                .eqIfPresent(CommissionAccountDO::getPendingAmount, reqVO.getPendingAmount())
                .eqIfPresent(CommissionAccountDO::getFrozenAmount, reqVO.getFrozenAmount())
                .eqIfPresent(CommissionAccountDO::getRejectedAmount, reqVO.getRejectedAmount())
                .eqIfPresent(CommissionAccountDO::getTotalReferralCount, reqVO.getTotalReferralCount())
                .eqIfPresent(CommissionAccountDO::getVersion, reqVO.getVersion())
                .betweenIfPresent(CommissionAccountDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CommissionAccountDO::getId));
    }

}