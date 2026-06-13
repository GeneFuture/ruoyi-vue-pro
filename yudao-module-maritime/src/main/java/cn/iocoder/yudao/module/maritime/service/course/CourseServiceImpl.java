package cn.iocoder.yudao.module.maritime.service.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.convert.course.CourseConvert;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.COURSE_NOT_EXISTS;

/**
 * 课程管理 Service 实现类
 */
@Service
@Validated
@Slf4j
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseSaveReqVO saveReqVO) {
        CourseDO course = CourseConvert.INSTANCE.convert(saveReqVO);
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseSaveReqVO saveReqVO) {
        CourseDO course = getCourse(saveReqVO.getId());
        CourseConvert.INSTANCE.update(saveReqVO, course);
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        getCourse(id);
        courseMapper.deleteById(id);
    }

    @Override
    public CourseDO getCourse(Long id) {
        CourseDO course = courseMapper.selectById(id);
        if (course == null) {
            throw exception(COURSE_NOT_EXISTS);
        }
        return course;
    }

    @Override
    public PageResult<CourseDO> getCoursePage(CoursePageReqVO pageReqVO) {
        return courseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CourseDO> getPublishedCourseList() {
        return courseMapper.selectPublishedList();
    }

}
