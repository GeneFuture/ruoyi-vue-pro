package cn.iocoder.yudao.module.maritime.dal.mysql.school;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.school.SchoolDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学校库 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface SchoolMapper extends BaseMapperX<SchoolDO> {

    default PageResult<SchoolDO> selectPage(SchoolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SchoolDO>()
                .likeIfPresent(SchoolDO::getName, reqVO.getName())
                .eqIfPresent(SchoolDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SchoolDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SchoolDO::getSortOrder)
                .orderByDesc(SchoolDO::getId));
    }

    /** 按名称查询学校（唯一性校验用） */
    default SchoolDO selectByName(String name) {
        return selectOne(SchoolDO::getName, name);
    }

    /**
     * 查询指定状态的学校列表（班期表单下拉用），按排序倒序
     */
    default List<SchoolDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<SchoolDO>()
                .eq(SchoolDO::getStatus, status)
                .orderByDesc(SchoolDO::getSortOrder)
                .orderByDesc(SchoolDO::getId));
    }

}
