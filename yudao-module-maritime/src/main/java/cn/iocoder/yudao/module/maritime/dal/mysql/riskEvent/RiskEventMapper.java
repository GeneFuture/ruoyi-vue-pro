package cn.iocoder.yudao.module.maritime.dal.mysql.riskEvent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo.RiskEventPageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.riskEvent.RiskEventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RiskEventMapper extends BaseMapperX<RiskEventDO> {

    default PageResult<RiskEventDO> selectPage(RiskEventPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RiskEventDO>()
                .eqIfPresent(RiskEventDO::getEventType, reqVO.getEventType())
                .eqIfPresent(RiskEventDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(RiskEventDO::getIsReviewed, reqVO.getIsReviewed())
                .betweenIfPresent(RiskEventDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RiskEventDO::getId));
    }

    /** 统计未审核风控事件数（Dashboard 待办用） */
    @Select("SELECT COUNT(*) FROM maritime_risk_event WHERE is_reviewed = 0 AND deleted = 0")
    int countUnreviewed();

}
