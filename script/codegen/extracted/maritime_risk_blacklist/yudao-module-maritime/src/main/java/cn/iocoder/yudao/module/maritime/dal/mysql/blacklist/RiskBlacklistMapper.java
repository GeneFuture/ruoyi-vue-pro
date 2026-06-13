package cn.iocoder.yudao.module.maritime.dal.mysql.blacklist;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.blacklist.RiskBlacklistDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo.*;

/**
 * 风控黑名单 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface RiskBlacklistMapper extends BaseMapperX<RiskBlacklistDO> {

    default PageResult<RiskBlacklistDO> selectPage(RiskBlacklistPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RiskBlacklistDO>()
                .eqIfPresent(RiskBlacklistDO::getPhone, reqVO.getPhone())
                .eqIfPresent(RiskBlacklistDO::getReason, reqVO.getReason())
                .eqIfPresent(RiskBlacklistDO::getOperatorId, reqVO.getOperatorId())
                .betweenIfPresent(RiskBlacklistDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RiskBlacklistDO::getId));
    }

}