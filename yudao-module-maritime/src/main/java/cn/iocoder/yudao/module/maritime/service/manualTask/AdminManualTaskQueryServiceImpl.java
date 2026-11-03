package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.manualTask.ManualTaskMapper;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
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
 * 管理后台 - 人工处理单查询 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminManualTaskQueryServiceImpl implements AdminManualTaskQueryService {

    @Resource
    private ManualTaskMapper manualTaskMapper;

    @Resource
    private AdminMaritimeQuerySupport querySupport;

    @Override
    public ManualTaskRespVO getManualTask(Long id) {
        ManualTaskDO task = manualTaskMapper.selectById(id);
        if (task == null) {
            return null;
        }
        return convertList(Collections.singletonList(task)).get(0);
    }

    @Override
    public PageResult<ManualTaskRespVO> getManualTaskPage(ManualTaskPageReqVO pageReqVO) {
        // 页面没有学员ID输入框，用姓名/手机号先换算成报名ID
        if (StrUtil.isNotBlank(pageReqVO.getRealName()) || StrUtil.isNotBlank(pageReqVO.getPhone())) {
            List<Long> enrollmentIds = querySupport.findEnrollmentIds(
                    pageReqVO.getRealName(), pageReqVO.getPhone());
            if (CollUtil.isEmpty(enrollmentIds)) {
                return PageResult.empty();
            }
            pageReqVO.setEnrollmentIds(enrollmentIds);
        }
        PageResult<ManualTaskDO> pageResult = manualTaskMapper.selectPage(pageReqVO);
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    private List<ManualTaskRespVO> convertList(List<ManualTaskDO> tasks) {
        if (CollUtil.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        Set<Long> enrollmentIds = tasks.stream()
                .map(ManualTaskDO::getEnrollmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, EnrollmentDO> enrollmentMap = querySupport.getEnrollmentMap(enrollmentIds);

        Set<Long> sessionIds = enrollmentMap.values().stream()
                .map(EnrollmentDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseSessionDO> sessionMap = querySupport.getSessionMap(sessionIds);

        Set<Long> courseIds = sessionMap.values().stream()
                .map(CourseSessionDO::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = querySupport.getCourseMap(courseIds);

        Set<Long> refundApplyIds = tasks.stream()
                .filter(AdminManualTaskQueryServiceImpl::isRefundTask)
                .map(ManualTaskDO::getSourceId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RefundApplyDO> refundApplyMap = querySupport.getRefundApplyMap(refundApplyIds);

        return tasks.stream().map(task -> {
            ManualTaskRespVO vo = BeanUtils.toBean(task, ManualTaskRespVO.class);
            EnrollmentDO enrollment = enrollmentMap.get(task.getEnrollmentId());
            if (enrollment != null) {
                vo.setEnrollmentNo(enrollment.getEnrollmentNo());
                vo.setRealName(enrollment.getRealName());
                vo.setPhone(enrollment.getPhone());
                CourseSessionDO session = sessionMap.get(enrollment.getSessionId());
                if (session != null) {
                    vo.setSessionCode(session.getSessionCode());
                    vo.setSessionName(session.getSessionName());
                    CourseDO course = courseMap.get(session.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getName());
                    }
                }
            }
            // sourceId 只有退费类任务才指向退费申请，避免其它类型人工单误命中同 ID 的退费记录
            if (isRefundTask(task)) {
                RefundApplyDO refundApply = refundApplyMap.get(task.getSourceId());
                if (refundApply != null) {
                    vo.setRefundAmount(refundApply.getRefundAmount());
                    vo.setApplyStatus(refundApply.getApplyStatus());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private static boolean isRefundTask(ManualTaskDO task) {
        return ManualTaskTypeEnum.REFUND_APPLY.getType().equals(task.getTaskType())
                || ManualTaskTypeEnum.REFUND_AMOUNT_CONFIRM.getType().equals(task.getTaskType());
    }

}
