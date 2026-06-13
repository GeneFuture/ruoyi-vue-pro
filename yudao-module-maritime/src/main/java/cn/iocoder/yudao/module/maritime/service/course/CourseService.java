package cn.iocoder.yudao.module.maritime.service.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;

import java.util.List;

/**
 * 课程管理 Service 接口
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param saveReqVO 创建信息
     * @return 课程编号
     */
    Long createCourse(CourseSaveReqVO saveReqVO);

    /**
     * 更新课程
     *
     * @param saveReqVO 更新信息
     */
    void updateCourse(CourseSaveReqVO saveReqVO);

    /**
     * 删除课程
     *
     * @param id 编号
     */
    void deleteCourse(Long id);

    /**
     * 获得课程，不存在则抛出异常
     *
     * @param id 编号
     * @return 课程
     */
    CourseDO getCourse(Long id);

    /**
     * 获得课程分页（管理端）
     *
     * @param pageReqVO 分页查询
     * @return 课程分页
     */
    PageResult<CourseDO> getCoursePage(CoursePageReqVO pageReqVO);

    /**
     * 获得已上架的课程列表（小程序端）
     *
     * @return 课程列表
     */
    List<CourseDO> getPublishedCourseList();

}
