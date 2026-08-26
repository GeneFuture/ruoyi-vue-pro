package cn.iocoder.yudao.module.maritime.dal.mysql.feeTemplate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.FeeTemplatePageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 费用模板 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface FeeTemplateMapper extends BaseMapperX<FeeTemplateDO> {

    default PageResult<FeeTemplateDO> selectPage(FeeTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FeeTemplateDO>()
                .likeIfPresent(FeeTemplateDO::getName, reqVO.getName())
                .eqIfPresent(FeeTemplateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(FeeTemplateDO::getIsGrouponEnabled, reqVO.getIsGrouponEnabled())
                .betweenIfPresent(FeeTemplateDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FeeTemplateDO::getId));
    }

    /** 按名称查询模板（唯一性校验用） */
    default FeeTemplateDO selectByName(String name) {
        return selectOne(FeeTemplateDO::getName, name);
    }

}
