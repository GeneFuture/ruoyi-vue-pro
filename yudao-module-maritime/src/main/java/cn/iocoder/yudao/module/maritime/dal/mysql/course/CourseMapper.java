package cn.iocoder.yudao.module.maritime.dal.mysql.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapperX<CourseDO> {

    default PageResult<CourseDO> selectPage(CoursePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourseDO>()
                .likeIfPresent(CourseDO::getName, reqVO.getName())
                .eqIfPresent(CourseDO::getCertificateType, reqVO.getCertificateType())
                .eqIfPresent(CourseDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CourseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CourseDO::getSortOrder)
                .orderByDesc(CourseDO::getId));
    }

    /** 查询已上架课程列表（status=1 表示上架，0 表示下架） */
    default List<CourseDO> selectPublishedList() {
        return selectList(new LambdaQueryWrapperX<CourseDO>()
                .eq(CourseDO::getStatus, 1)
                .orderByDesc(CourseDO::getSortOrder)
                .orderByDesc(CourseDO::getId));
    }

}
