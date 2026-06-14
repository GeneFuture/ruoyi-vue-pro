package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 尾款催缴提醒任务
 *
 * 每天 10:00 扫描 DEPOSITED 状态且尾款截止日期在 7 天内的报名，发送催缴提醒。
 * 在后台「基础设施 → 定时任务」配置 Cron: 0 0 10 * * ?
 *
 * @author Gene Ye
 */
@Slf4j
@Component
public class BalanceReminderJob implements JobHandler {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Override
    public String execute(String param) throws Exception {
        // 查询尾款截止日期在今天 + 7 天之前（含今天）的 DEPOSITED 报名
        LocalDate reminderBefore = LocalDate.now().plusDays(7);
        List<EnrollmentDO> enrollments = enrollmentMapper.selectDepositedWithBalanceDueBefore(reminderBefore);
        log.info("[BalanceReminderJob] 待发送尾款提醒数: {}", enrollments.size());

        int sent = 0;
        for (EnrollmentDO enrollment : enrollments) {
            try {
                // TODO: 集成消息通知服务（T09）后替换为实际推送
                log.info("[BalanceReminderJob] 发送尾款提醒, enrollmentId={}, memberId={}, balanceDueDate={}",
                        enrollment.getId(), enrollment.getMemberId(), enrollment.getBalanceDueDate());
                sent++;
            } catch (Exception e) {
                log.error("[BalanceReminderJob] 发送提醒失败, enrollmentId={}", enrollment.getId(), e);
            }
        }

        String result = String.format("共扫描 %d 条，成功发送 %d 条", enrollments.size(), sent);
        log.info("[BalanceReminderJob] 执行完毕: {}", result);
        return result;
    }

}
