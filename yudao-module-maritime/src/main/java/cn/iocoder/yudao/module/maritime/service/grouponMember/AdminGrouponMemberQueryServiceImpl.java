package cn.iocoder.yudao.module.maritime.service.grouponMember;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember.GrouponMemberMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.service.common.AdminMaritimeQuerySupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理后台 - 拼团成员查询 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminGrouponMemberQueryServiceImpl implements AdminGrouponMemberQueryService {

    @Resource
    private GrouponMemberMapper grouponMemberMapper;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private AdminMaritimeQuerySupport querySupport;

    @Override
    public GrouponMemberRespVO getGrouponMember(Long id) {
        GrouponMemberDO member = grouponMemberMapper.selectById(id);
        if (member == null) {
            return null;
        }
        return convertList(Collections.singletonList(member)).get(0);
    }

    @Override
    public PageResult<GrouponMemberRespVO> getGrouponMemberPage(GrouponMemberPageReqVO pageReqVO) {
        if (StrUtil.isNotBlank(pageReqVO.getInviteCode())) {
            GrouponRecordDO groupon = grouponRecordMapper.selectByInviteCode(pageReqVO.getInviteCode());
            if (groupon == null) {
                return PageResult.empty();
            }
            pageReqVO.setGrouponRecordId(groupon.getId());
        }
        if (StrUtil.isNotBlank(pageReqVO.getRealName()) || StrUtil.isNotBlank(pageReqVO.getPhone())) {
            List<Long> enrollmentIds = querySupport.findEnrollmentIds(
                    pageReqVO.getRealName(), pageReqVO.getPhone());
            if (CollUtil.isEmpty(enrollmentIds)) {
                return PageResult.empty();
            }
            pageReqVO.setEnrollmentIds(enrollmentIds);
        }
        PageResult<GrouponMemberDO> pageResult = grouponMemberMapper.selectPage(pageReqVO);
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    private List<GrouponMemberRespVO> convertList(List<GrouponMemberDO> members) {
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        Set<Long> grouponIds = members.stream()
                .map(GrouponMemberDO::getGrouponRecordId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, GrouponRecordDO> grouponMap = querySupport.getGrouponMap(grouponIds);

        Set<Long> enrollmentIds = members.stream()
                .map(GrouponMemberDO::getEnrollmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, EnrollmentDO> enrollmentMap = querySupport.getEnrollmentMap(enrollmentIds);

        Set<Long> sessionIds = grouponMap.values().stream()
                .map(GrouponRecordDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet());
        sessionIds.addAll(enrollmentMap.values().stream()
                .map(EnrollmentDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, CourseSessionDO> sessionMap = querySupport.getSessionMap(sessionIds);

        Set<Long> courseIds = sessionMap.values().stream()
                .map(CourseSessionDO::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = querySupport.getCourseMap(courseIds);

        return members.stream().map(member -> {
            GrouponMemberRespVO vo = BeanUtils.toBean(member, GrouponMemberRespVO.class);
            GrouponRecordDO groupon = grouponMap.get(member.getGrouponRecordId());
            if (groupon != null) {
                vo.setInviteCode(groupon.getInviteCode());
                vo.setGrouponStatus(groupon.getGrouponStatus());
            }
            EnrollmentDO enrollment = enrollmentMap.get(member.getEnrollmentId());
            if (enrollment != null) {
                vo.setEnrollmentNo(enrollment.getEnrollmentNo());
                vo.setRealName(enrollment.getRealName());
                vo.setPhone(enrollment.getPhone());
            }
            Long sessionId = groupon != null ? groupon.getSessionId()
                    : (enrollment != null ? enrollment.getSessionId() : null);
            CourseSessionDO session = sessionMap.get(sessionId);
            if (session != null) {
                vo.setSessionCode(session.getSessionCode());
                vo.setSessionName(session.getSessionName());
                vo.setSessionStartDate(session.getStartDate());
                CourseDO course = courseMap.get(session.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

}
