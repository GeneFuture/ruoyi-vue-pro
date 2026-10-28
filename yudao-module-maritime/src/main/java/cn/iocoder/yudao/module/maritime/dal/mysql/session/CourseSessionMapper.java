package cn.iocoder.yudao.module.maritime.dal.mysql.session;

import java.time.LocalDate;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    /**
     * 小程序课程列表过滤：查询满足班期/月份条件的 OPEN 班期，用于提取 courseId 范围。
     * 城市不在班期表上（场地表才有），调用方需先按城市查出 sessionIds 再传进来；
     * 注意传空集合表示「按城市筛完一条都没有」，此时调用方应直接返回空页，不要调本方法。
     */
    default List<CourseSessionDO> selectOpenWithFilters(Collection<Long> sessionIds, LocalDate startFrom, LocalDate startTo) {
        LambdaQueryWrapperX<CourseSessionDO> wrapper = new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .inIfPresent(CourseSessionDO::getId, sessionIds);
        if (startFrom != null && startTo != null) {
            wrapper.between(CourseSessionDO::getStartDate, startFrom, startTo);
        }
        return selectList(wrapper);
    }

    /** 查询所有招生中（OPEN）的班期，按开班日期升序（用于首页统计余位/近期开班） */
    default List<CourseSessionDO> selectOpenList() {
        return selectList(new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .orderByAsc(CourseSessionDO::getStartDate));
    }

    /** 查询开班日期在指定区间内的招生中（OPEN）班期，按开班日期升序（首页近期开班楼层） */
    default List<CourseSessionDO> selectOpenByStartDateBetween(LocalDate start, LocalDate end) {
        return selectList(new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getSessionStatus, "OPEN")
                .between(CourseSessionDO::getStartDate, start, end)
                .orderByAsc(CourseSessionDO::getStartDate));
    }

    /** 统计招生中（OPEN）的班期数量 */
    default Long selectOpenCount() {
        return selectCount(new LambdaQueryWrapperX<CourseSessionDO>()
                .eq(CourseSessionDO::getSessionStatus, "OPEN"));
    }

    /** CAS 原子加一：enrolled_count < max_students 时才更新，防并发超卖；返回影响行数 */
    @Update("UPDATE maritime_course_session SET enrolled_count = enrolled_count + 1 " +
            "WHERE id = #{sessionId} AND enrolled_count < #{maxStudents} AND deleted = 0")
    int incrementEnrolledCount(Long sessionId, Integer maxStudents);

    /** 归还名额：enrolled_count > 0 时减一（取消报名时调用） */
    @Update("UPDATE maritime_course_session SET enrolled_count = enrolled_count - 1 " +
            "WHERE id = #{sessionId} AND enrolled_count > 0 AND deleted = 0")
    int decrementEnrolledCount(Long sessionId);

    /**
     * 锁定课程行，用于班期编号生成时串行化同一课程的并发新增。
     *
     * @param courseId 课程ID
     * @return 课程ID；课程不存在时返回 null
     */
    @Select("SELECT id FROM maritime_course WHERE id = #{courseId} AND deleted = 0 FOR UPDATE")
    Long lockCourseForUpdate(@Param("courseId") Long courseId);

    /**
     * 查询指定前缀的班期编号，包含已软删除的记录。
     * 编号一旦使用就不再复用，避免 (session_code, deleted) 唯一键在重复删除时冲突。
     *
     * @param pattern LIKE 匹配串，如 C1-202610-%
     * @return 班期编号列表
     */
    @Select("SELECT session_code FROM maritime_course_session WHERE session_code LIKE #{pattern}")
    List<String> selectCodesByPattern(@Param("pattern") String pattern);

}