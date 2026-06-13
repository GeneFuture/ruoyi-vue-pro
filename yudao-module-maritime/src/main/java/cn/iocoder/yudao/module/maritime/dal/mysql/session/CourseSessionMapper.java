package cn.iocoder.yudao.module.maritime.dal.mysql.session;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.*;

/**
 * 班期管理 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface CourseSessionMapper extends BaseMapperX<CourseSessionDO> {

    default PageResult<CourseSessionDO> selectPage(CourseSessionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourseSessionDO>()
                .eqIfPresent(CourseSessionDO::getCourseId, reqVO.getCourseId())
                .eqIfPresent(CourseSessionDO::getSessionCode, reqVO.getSessionCode())
                .likeIfPresent(CourseSessionDO::getSessionName, reqVO.getSessionName())
                .eqIfPresent(CourseSessionDO::getLocation, reqVO.getLocation())
                .eqIfPresent(CourseSessionDO::getDurationDays, reqVO.getDurationDays())
                .eqIfPresent(CourseSessionDO::getInstructorInfo, reqVO.getInstructorInfo())
                .betweenIfPresent(CourseSessionDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(CourseSessionDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(CourseSessionDO::getEnrollmentDeadline, reqVO.getEnrollmentDeadline())
                .eqIfPresent(CourseSessionDO::getMaxStudents, reqVO.getMaxStudents())
                .eqIfPresent(CourseSessionDO::getEnrolledCount, reqVO.getEnrolledCount())
                .eqIfPresent(CourseSessionDO::getSessionStatus, reqVO.getSessionStatus())
                .eqIfPresent(CourseSessionDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CourseSessionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CourseSessionDO::getId));
    }

    /** 查询某课程下所有 OPEN 状态的班期，按开班日期升序 */
    default List<CourseSessionDO> selectOpenByCourseId(Long courseId) {
        return selectList(new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getCourseId, courseId)
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .orderByAsc(CourseSessionDO::getStartDate));
    }

    /** 批量查询多个课程下所有 OPEN 状态的班期，按开班日期升序 */
    default List<CourseSessionDO> selectOpenByCourseIds(Collection<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CourseSessionDO>()
                .in(CourseSessionDO::getCourseId, courseIds)
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .orderByAsc(CourseSessionDO::getStartDate));
    }

    /** 小程序课程列表过滤：查询满足地点/月份条件的 OPEN 班期，用于提取 courseId 范围 */
    default List<CourseSessionDO> selectOpenWithFilters(String location, LocalDate startFrom, LocalDate startTo) {
        LambdaQueryWrapperX<CourseSessionDO> wrapper = new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .likeIfPresent(CourseSessionDO::getLocation, location);
        if (startFrom != null && startTo != null) {
            wrapper.between(CourseSessionDO::getStartDate, startFrom, startTo);
        }
        return selectList(wrapper);
    }

    /** CAS 原子加一：enrolled_count < max_students 时才更新，防并发超卖；返回影响行数 */
    @Update("UPDATE maritime_course_session SET enrolled_count = enrolled_count + 1 " +
            "WHERE id = #{sessionId} AND enrolled_count < #{maxStudents} AND deleted = 0")
    int incrementEnrolledCount(Long sessionId, Integer maxStudents);

    /** 归还名额：enrolled_count > 0 时减一（取消报名时调用） */
    @Update("UPDATE maritime_course_session SET enrolled_count = enrolled_count - 1 " +
            "WHERE id = #{sessionId} AND enrolled_count > 0 AND deleted = 0")
    int decrementEnrolledCount(Long sessionId);

}