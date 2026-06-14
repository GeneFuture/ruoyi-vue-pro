package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.service.enrollment.EnrollmentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报名定金订单过期关闭任务
 *
 * 每5分钟扫描 PENDING 且 expire_time < now 的定金订单，逐一关闭。
 * 在后台「基础设施 → 定时任务」配置 Cron: 0 *\/5 * * * ?
 *
 * @author Gene Ye
 */
@Slf4j
@Component
public class OrderExpireJob implements JobHandler {

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private EnrollmentService enrollmentService;

    @Override
    public String execute(String param) throws Exception {
        List<EnrollmentOrderDO> expiredOrders = enrollmentOrderMapper.selectExpiredPendingOrders(LocalDateTime.now());
        log.info("[OrderExpireJob] 扫描到过期订单数: {}", expiredOrders.size());

        int closed = 0;
        for (EnrollmentOrderDO order : expiredOrders) {
            try {
                enrollmentService.closeExpiredOrder(order.getId());
                closed++;
            } catch (Exception e) {
                log.error("[OrderExpireJob] 关闭订单失败, orderId={}", order.getId(), e);
            }
        }

        String result = String.format("共扫描 %d 条，成功关闭 %d 条", expiredOrders.size(), closed);
        log.info("[OrderExpireJob] 执行完毕: {}", result);
        return result;
    }

}
