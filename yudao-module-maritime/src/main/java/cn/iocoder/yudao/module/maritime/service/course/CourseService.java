package cn.iocoder.yudao.module.maritime.service.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseRespVO;
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

    /**
     * 获得课程分页（小程序端，含最近班期和学费区间）
     *
     * @param pageReqVO 分页查询
     * @return 课程分页（含班期摘要）
     */
    PageResult<AppCourseRespVO> getCoursePageForApp(AppCoursePageReqVO pageReqVO);

    /**
     * 获得课程详情（小程序端，含所有 OPEN 班期和费用信息）
     *
     * @param id 课程编号
     * @return 课程详情
     */
    AppCourseDetailRespVO getCourseDetailForApp(Long id);

    /**
     * 更新课程上下架状态
     *
     * @param id     课程编号
     * @param status 状态（0=下架，1=上架）
     */
    void updateCourseStatus(Long id, Integer status);

}
