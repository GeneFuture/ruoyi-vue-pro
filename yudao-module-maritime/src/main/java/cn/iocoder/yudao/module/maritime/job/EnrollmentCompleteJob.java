package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCompletedEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 结班完成任务
 *
 * 每天扫描 PAID_OFF 且班期结束日期早于今天的报名，置为 COMPLETED。
 * 在后台「基础设施 → 定时任务」配置 Cron: 0 30 1 * * ?
 *
 * @author Gene Ye
 */
@Slf4j
@Component
public class EnrollmentCompleteJob implements JobHandler {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @TenantJob
    @Override
    public String execute(String param) {
        List<EnrollmentDO> enrollments = enrollmentMapper.selectListByStatus(
                EnrollmentStatusEnum.PAID_OFF.getStatus());
        if (enrollments.isEmpty()) {
            return "共扫描 0 条，完成 0 条";
        }

        List<Long> sessionIds = enrollments.stream()
                .map(EnrollmentDO::getSessionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, CourseSessionDO> sessionMap = courseSessionMapper
                .selectList(CourseSessionDO::getId, sessionIds)
                .stream()
                .collect(Collectors.toMap(CourseSessionDO::getId, session -> session));

        LocalDate today = LocalDate.now();
        int completed = 0;
        for (EnrollmentDO enrollment : enrollments) {
            try {
                CourseSessionDO session = sessionMap.get(enrollment.getSessionId());
                if (session == null || session.getEndDate() == null
                        || !session.getEndDate().isBefore(today)) {
                    continue;
                }
                EnrollmentDO update = new EnrollmentDO();
                update.setId(enrollment.getId());
                update.setEnrollmentStatus(EnrollmentStatusEnum.COMPLETED.getStatus());
                enrollmentMapper.updateById(update);

                eventPublisher.publishEvent(new EnrollmentCompletedEvent(this, enrollment.getId(),
                        enrollment.getMemberId(), enrollment.getSessionId()));
                completed++;
            } catch (Exception e) {
                log.error("[EnrollmentCompleteJob] 处理失败, enrollmentId={}", enrollment.getId(), e);
            }
        }
        String result = String.format("共扫描 %d 条，完成 %d 条", enrollments.size(), completed);
        log.info("[EnrollmentCompleteJob] 执行完毕: {}", result);
        return result;
    }

}
