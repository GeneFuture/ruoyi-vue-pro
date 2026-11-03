package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentOverdueEvent;
import cn.iocoder.yudao.module.maritime.service.manualTask.ManualTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 尾款逾期转人工处理任务
 *
 * 每天扫描 DEPOSITED 且尾款截止日期已过（或班期已开班仍未付清）的报名，转为 MANUAL_PROCESSING 并生成人工单。
 * 在后台「基础设施 → 定时任务」配置 Cron: 0 0 9 * * ?
 *
 * @author Gene Ye
 */
@Slf4j
@Component
public class BalanceOverdueJob implements JobHandler {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private ManualTaskService manualTaskService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @TenantJob
    @Override
    public String execute(String param) {
        List<EnrollmentDO> depositedList = enrollmentMapper.selectListByStatus(
                EnrollmentStatusEnum.DEPOSITED.getStatus());
        if (depositedList.isEmpty()) {
            return "共扫描 0 条，转人工 0 条";
        }
        Map<Long, CourseSessionDO> sessionMap = loadSessions(depositedList);

        // 尾款截止日期已过，或班期已开班仍未付清，都转人工处理
        LocalDate today = LocalDate.now();
        List<EnrollmentDO> enrollments = depositedList.stream()
                .filter(enrollment -> hasOverdueBalance(enrollment, sessionMap.get(enrollment.getSessionId()), today))
                .collect(Collectors.toList());
        log.info("[BalanceOverdueJob] 扫描到尾款逾期报名数: {}", enrollments.size());

        int processed = 0;
        for (EnrollmentDO enrollment : enrollments) {
            try {
                BigDecimal balance = enrollment.getBalanceAmount();
                if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                CourseSessionDO session = sessionMap.get(enrollment.getSessionId());
                String reason = buildReason(enrollment, session, today);
                manualTaskService.createTaskIfAbsent(
                        enrollment.getId(),
                        enrollment.getMemberId(),
                        null,
                        "BALANCE",
                        null,
                        ManualTaskTypeEnum.BALANCE_OVERDUE.getType(),
                        reason);
                EnrollmentDO update = new EnrollmentDO();
                update.setId(enrollment.getId());
                update.setEnrollmentStatus(EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus());
                enrollmentMapper.updateById(update);

                eventPublisher.publishEvent(new EnrollmentOverdueEvent(this, enrollment.getId(),
                        enrollment.getMemberId(), enrollment.getSessionId(), balance, reason));
                processed++;
            } catch (Exception e) {
                log.error("[BalanceOverdueJob] 处理失败, enrollmentId={}", enrollment.getId(), e);
            }
        }
        String result = String.format("共扫描 %d 条，转人工 %d 条", enrollments.size(), processed);
        log.info("[BalanceOverdueJob] 执行完毕: {}", result);
        return result;
    }

    private Map<Long, CourseSessionDO> loadSessions(List<EnrollmentDO> enrollments) {
        List<Long> sessionIds = enrollments.stream()
                .map(EnrollmentDO::getSessionId)
                .distinct()
                .collect(Collectors.toList());
        return courseSessionMapper.selectList(CourseSessionDO::getId, sessionIds)
                .stream()
                .collect(Collectors.toMap(CourseSessionDO::getId, session -> session));
    }

    private boolean hasOverdueBalance(EnrollmentDO enrollment, CourseSessionDO session, LocalDate today) {
        // 截止日期早于今天（今天到期仍可正常支付）
        if (enrollment.getBalanceDueDate() != null
                && enrollment.getBalanceDueDate().isBefore(today)) {
            return true;
        }
        // 未配置尾款截止日期时，以开班日期兜底：开班当天仍未付清即转人工
        return session != null && session.getStartDate() != null
                && !session.getStartDate().isAfter(today);
    }

    private String buildReason(EnrollmentDO enrollment, CourseSessionDO session, LocalDate today) {
        if (enrollment.getBalanceDueDate() != null
                && enrollment.getBalanceDueDate().isBefore(today)) {
            return "尾款截止日期已过（" + enrollment.getBalanceDueDate() + "），仍未支付尾款";
        }
        return "班期已开班（" + (session != null ? session.getStartDate() : "") + "），仍未支付尾款";
    }

}
