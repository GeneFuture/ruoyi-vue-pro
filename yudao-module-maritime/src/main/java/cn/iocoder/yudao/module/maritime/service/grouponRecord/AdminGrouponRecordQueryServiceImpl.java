package cn.iocoder.yudao.module.maritime.service.grouponRecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
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
 * 管理后台 - 拼团记录查询 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminGrouponRecordQueryServiceImpl implements AdminGrouponRecordQueryService {

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private AdminMaritimeQuerySupport querySupport;

    @Override
    public GrouponRecordRespVO getGrouponRecord(Long id) {
        GrouponRecordDO record = grouponRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        return convertList(Collections.singletonList(record)).get(0);
    }

    @Override
    public PageResult<GrouponRecordRespVO> getGrouponRecordPage(GrouponRecordPageReqVO pageReqVO) {
        PageResult<GrouponRecordDO> pageResult = grouponRecordMapper.selectPage(pageReqVO);
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    private List<GrouponRecordRespVO> convertList(List<GrouponRecordDO> records) {
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> sessionIds = records.stream()
                .map(GrouponRecordDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseSessionDO> sessionMap = querySupport.getSessionMap(sessionIds);

        Set<Long> courseIds = sessionMap.values().stream()
                .map(CourseSessionDO::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = querySupport.getCourseMap(courseIds);

        Set<Long> initiatorIds = records.stream()
                .map(GrouponRecordDO::getInitiatorEnrollmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, EnrollmentDO> initiatorMap = querySupport.getEnrollmentMap(initiatorIds);

        return records.stream().map(record -> {
            GrouponRecordRespVO vo = BeanUtils.toBean(record, GrouponRecordRespVO.class);
            CourseSessionDO session = sessionMap.get(record.getSessionId());
            if (session != null) {
                vo.setSessionCode(session.getSessionCode());
                vo.setSessionName(session.getSessionName());
                vo.setSessionStartDate(session.getStartDate());
                CourseDO course = courseMap.get(session.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getName());
                }
            }
            EnrollmentDO initiator = initiatorMap.get(record.getInitiatorEnrollmentId());
            if (initiator != null) {
                vo.setInitiatorEnrollmentNo(initiator.getEnrollmentNo());
                vo.setInitiatorRealName(initiator.getRealName());
                vo.setInitiatorPhone(initiator.getPhone());
            }
            return vo;
        }).collect(Collectors.toList());
    }

}
