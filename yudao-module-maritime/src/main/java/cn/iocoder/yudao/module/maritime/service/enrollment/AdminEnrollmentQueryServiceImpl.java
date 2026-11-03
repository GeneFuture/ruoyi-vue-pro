package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理后台 - 报名记录查询 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminEnrollmentQueryServiceImpl implements AdminEnrollmentQueryService {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Override
    public EnrollmentRespVO getEnrollment(Long id) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            return null;
        }
        return convertList(Collections.singletonList(enrollment)).get(0);
    }

    @Override
    public PageResult<EnrollmentRespVO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO) {
        PageResult<EnrollmentDO> pageResult = enrollmentMapper.selectPage(pageReqVO);
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    /**
     * 批量查询关联数据，避免逐条查询造成 N+1
     */
    private List<EnrollmentRespVO> convertList(List<EnrollmentDO> enrollments) {
        if (CollUtil.isEmpty(enrollments)) {
            return Collections.emptyList();
        }
        Set<Long> sessionIds = enrollments.stream()
                .map(EnrollmentDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseSessionDO> sessionMap = getSessionMap(sessionIds);

        Set<Long> courseIds = sessionMap.values().stream()
                .map(CourseSessionDO::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = getCourseMap(courseIds);

        Set<Long> grouponIds = enrollments.stream()
                .map(EnrollmentDO::getGrouponRecordId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, GrouponRecordDO> grouponMap = getGrouponMap(grouponIds);

        return enrollments.stream()
                .map(enrollment -> convert(enrollment, sessionMap, courseMap, grouponMap))
                .collect(Collectors.toList());
    }

    private EnrollmentRespVO convert(EnrollmentDO enrollment,
                                     Map<Long, CourseSessionDO> sessionMap,
                                     Map<Long, CourseDO> courseMap,
                                     Map<Long, GrouponRecordDO> grouponMap) {
        EnrollmentRespVO vo = BeanUtils.toBean(enrollment, EnrollmentRespVO.class);
        // 身份证脱敏：前 4 后 4，中间固定 10 个 *
        String idCard = enrollment.getIdCard();
        if (StrUtil.isNotBlank(idCard) && idCard.length() >= 8) {
            vo.setIdCardMasked(idCard.substring(0, 4) + "**********"
                    + idCard.substring(idCard.length() - 4));
        }

        CourseSessionDO session = sessionMap.get(enrollment.getSessionId());
        if (session != null) {
            vo.setSessionCode(session.getSessionCode());
            vo.setSessionName(session.getSessionName());
            vo.setSessionStartDate(session.getStartDate());
            CourseDO course = courseMap.get(session.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
            }
        }

        GrouponRecordDO groupon = grouponMap.get(enrollment.getGrouponRecordId());
        if (groupon != null) {
            vo.setGrouponInviteCode(groupon.getInviteCode());
            vo.setGrouponStatus(groupon.getGrouponStatus());
        }
        return vo;
    }

    private Map<Long, CourseSessionDO> getSessionMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return courseSessionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CourseSessionDO::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, CourseDO> getCourseMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CourseDO::getId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, GrouponRecordDO> getGrouponMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return grouponRecordMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(GrouponRecordDO::getId, Function.identity(), (a, b) -> a));
    }

}
