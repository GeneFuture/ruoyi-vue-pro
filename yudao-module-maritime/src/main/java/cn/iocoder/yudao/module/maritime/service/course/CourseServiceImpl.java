package cn.iocoder.yudao.module.maritime.service.course;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseRespVO;
import cn.iocoder.yudao.module.maritime.convert.course.CourseConvert;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(Long id, Integer status) {
        getCourse(id);
        CourseDO updateObj = new CourseDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
        courseMapper.updateById(updateObj);
    }

    @Override
    public PageResult<AppCourseRespVO> getCoursePageForApp(AppCoursePageReqVO pageReqVO) {
        // 1. 根据班期级过滤条件（地点/月份）收窄课程范围
        Collection<Long> filterCourseIds = null;
        if (StrUtil.isNotBlank(pageReqVO.getLocation()) || StrUtil.isNotBlank(pageReqVO.getSessionMonth())) {
            LocalDate startFrom = null;
            LocalDate startTo = null;
            if (StrUtil.isNotBlank(pageReqVO.getSessionMonth())) {
                LocalDate monthStart = LocalDate.parse(pageReqVO.getSessionMonth() + "-01");
                startFrom = monthStart;
                startTo = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            }
            List<CourseSessionDO> filteredSessions = courseSessionMapper.selectOpenWithFilters(
                    pageReqVO.getLocation(), startFrom, startTo);
            filterCourseIds = filteredSessions.stream()
                    .map(CourseSessionDO::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());
            if (filterCourseIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }

        // 2. 分页查询上架课程
        PageResult<CourseDO> coursePage = courseMapper.selectPageForApp(pageReqVO, filterCourseIds);
        if (coursePage.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 3. 查询这批课程的所有 OPEN 班期
        List<Long> courseIds = coursePage.getList().stream()
                .map(CourseDO::getId).collect(Collectors.toList());
        List<CourseSessionDO> openSessions = courseSessionMapper.selectOpenByCourseIds(courseIds);

        // 4. 查询这批班期的费用配置
        List<Long> sessionIds = openSessions.stream()
                .map(CourseSessionDO::getId).collect(Collectors.toList());
        Map<Long, SessionFeeConfigDO> feeConfigBySessionId = sessionFeeConfigMapper.selectBySessionIds(sessionIds)
                .stream().collect(Collectors.toMap(SessionFeeConfigDO::getSessionId, f -> f, (a, b) -> a));

        // 5. 构建辅助 Map：courseId -> 最近班期（已按 startDate asc 排序，取第一条）
        Map<Long, CourseSessionDO> nearestSessionByCourseId = new LinkedHashMap<>();
        Map<Long, List<CourseSessionDO>> sessionsByCourseId = openSessions.stream()
                .collect(Collectors.groupingBy(CourseSessionDO::getCourseId));
        for (CourseSessionDO session : openSessions) {
            nearestSessionByCourseId.putIfAbsent(session.getCourseId(), session);
        }

        // 6. 组装 VO
        List<AppCourseRespVO> voList = coursePage.getList().stream().map(course -> {
            AppCourseRespVO vo = new AppCourseRespVO();
            vo.setId(course.getId());
            vo.setName(course.getName());
            vo.setCertificateType(course.getCertificateType());
            vo.setCoverImage(course.getCoverImage());

            CourseSessionDO nearest = nearestSessionByCourseId.get(course.getId());
            if (nearest != null) {
                vo.setNearestSessionId(nearest.getId());
                vo.setNearestStartDate(nearest.getStartDate());
                vo.setLocation(nearest.getLocation());
                vo.setDurationDays(nearest.getDurationDays());
                vo.setSessionStatus(nearest.getSessionStatus());
            }

            List<BigDecimal> tuitions = sessionsByCourseId
                    .getOrDefault(course.getId(), Collections.emptyList()).stream()
                    .map(s -> feeConfigBySessionId.get(s.getId()))
                    .filter(Objects::nonNull)
                    .map(SessionFeeConfigDO::getTuitionAmount)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!tuitions.isEmpty()) {
                vo.setMinTuition(tuitions.stream().min(BigDecimal::compareTo).orElse(null));
                vo.setMaxTuition(tuitions.stream().max(BigDecimal::compareTo).orElse(null));
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, coursePage.getTotal());
    }

    @Override
    public AppCourseDetailRespVO getCourseDetailForApp(Long id) {
        CourseDO course = getCourse(id);

        List<CourseSessionDO> openSessions = courseSessionMapper.selectOpenByCourseId(id);
        List<Long> sessionIds = openSessions.stream()
                .map(CourseSessionDO::getId).collect(Collectors.toList());
        Map<Long, SessionFeeConfigDO> feeConfigBySessionId = sessionFeeConfigMapper.selectBySessionIds(sessionIds)
                .stream().collect(Collectors.toMap(SessionFeeConfigDO::getSessionId, f -> f, (a, b) -> a));

        AppCourseDetailRespVO vo = new AppCourseDetailRespVO();
        vo.setId(course.getId());
        vo.setName(course.getName());
        vo.setCertificateType(course.getCertificateType());
        vo.setCoverImage(course.getCoverImage());
        vo.setDescription(course.getDescription());
        vo.setEnrollmentConditionText(course.getEnrollmentConditionText());

        List<AppCourseDetailRespVO.AppSessionSummaryVO> sessionVOs = openSessions.stream().map(session -> {
            AppCourseDetailRespVO.AppSessionSummaryVO svo = new AppCourseDetailRespVO.AppSessionSummaryVO();
            svo.setId(session.getId());
            svo.setSessionCode(session.getSessionCode());
            svo.setSessionName(session.getSessionName());
            svo.setLocation(session.getLocation());
            svo.setDurationDays(session.getDurationDays());
            svo.setInstructorInfo(session.getInstructorInfo());
            svo.setStartDate(session.getStartDate());
            svo.setEndDate(session.getEndDate());
            svo.setEnrollmentDeadline(session.getEnrollmentDeadline());
            svo.setMaxStudents(session.getMaxStudents());
            int enrolled = session.getEnrolledCount() != null ? session.getEnrolledCount() : 0;
            svo.setEnrolledCount(enrolled);
            svo.setRemainingCount(session.getMaxStudents() - enrolled);

            SessionFeeConfigDO feeConfig = feeConfigBySessionId.get(session.getId());
            if (feeConfig != null) {
                svo.setTuitionAmount(feeConfig.getTuitionAmount());
                svo.setDepositAmount(feeConfig.getDepositAmount());
                svo.setIsGrouponEnabled(feeConfig.getIsGrouponEnabled());
                if (Boolean.TRUE.equals(feeConfig.getIsGrouponEnabled())
                        && feeConfig.getDepositAmount() != null
                        && feeConfig.getGrouponDiscountAmount() != null) {
                    svo.setGrouponDepositAmount(
                            feeConfig.getDepositAmount().subtract(feeConfig.getGrouponDiscountAmount()));
                }
            }
            return svo;
        }).collect(Collectors.toList());

        vo.setOpenSessions(sessionVOs);
        return vo;
    }

}
