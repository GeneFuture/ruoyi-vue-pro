package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 佣金 T+1 自动发放 Job — 每日 09:00 执行
 * 将审批后等待超 24h 的 PENDING_PAYOUT 佣金通过企业转账发放给推荐人
 */
@Slf4j
@Component
public class CommissionPayoutJob implements JobHandler {

    private static final int PAGE_SIZE = 50;

    @Resource
    private CommissionRecordMapper commissionRecordMapper;
    @Resource
    private CommissionRecordService commissionRecordService;

    @Override
    public String execute(String param) throws Exception {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info("[CommissionPayoutJob] 开始执行，threshold={}", threshold);

        int page = 0;
        int totalProcessed = 0;
        int totalFailed = 0;

        while (true) {
            List<CommissionRecordDO> records = commissionRecordMapper
                    .selectPendingPayoutBefore(threshold, page * PAGE_SIZE, PAGE_SIZE);
            if (records.isEmpty()) {
                break;
            }

            for (CommissionRecordDO record : records) {
                try {
                    commissionRecordService.payoutCommission(record.getId());
                    totalProcessed++;
                } catch (Exception e) {
                    totalFailed++;
                    log.error("[CommissionPayoutJob] 打款失败 recordId={}", record.getId(), e);
                }
            }

            if (records.size() < PAGE_SIZE) {
                break;
            }
            page++;
        }

        String result = String.format("处理完成：成功=%d，失败=%d", totalProcessed, totalFailed);
        log.info("[CommissionPayoutJob] {}", result);
        return result;
    }

}
