package cn.iocoder.yudao.module.maritime.convert.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface CourseConvert {

    CourseConvert INSTANCE = Mappers.getMapper(CourseConvert.class);

    CourseDO convert(CourseSaveReqVO reqVO);

    CourseRespVO convert(CourseDO course);

    void update(CourseSaveReqVO reqVO, @MappingTarget CourseDO course);

    PageResult<CourseRespVO> convertPage(PageResult<CourseDO> page);

}
