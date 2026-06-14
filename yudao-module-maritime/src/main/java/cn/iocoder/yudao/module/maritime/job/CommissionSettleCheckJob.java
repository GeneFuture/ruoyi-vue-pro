package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 佣金结算检查 Job — 每日 08:00 执行
 * 将已过结算日的 WAITING_FOR_CLASS 佣金推进到 PENDING_REVIEW（无退款）或 FROZEN（有退款申请）
 */
@Slf4j
@Component
public class CommissionSettleCheckJob implements JobHandler {

    private static final int PAGE_SIZE = 100;

    @Resource
    private CommissionRecordMapper commissionRecordMapper;
    @Resource
    private CommissionRecordService commissionRecordService;

    @Override
    public String execute(String param) throws Exception {
        LocalDate today = LocalDate.now();
        log.info("[CommissionSettleCheckJob] 开始执行，today={}", today);

        int page = 0;
        int totalProcessed = 0;
        int totalFailed = 0;

        while (true) {
            List<CommissionRecordDO> records = commissionRecordMapper
                    .selectWaitingForClassAndDue(today, page * PAGE_SIZE, PAGE_SIZE);
            if (records.isEmpty()) {
                break;
            }

            for (CommissionRecordDO record : records) {
                try {
                    commissionRecordService.processSettleCheck(record.getId());
                    totalProcessed++;
                } catch (Exception e) {
                    totalFailed++;
                    log.error("[CommissionSettleCheckJob] 处理失败 recordId={}", record.getId(), e);
                }
            }

            if (records.size() < PAGE_SIZE) {
                break;
            }
            page++;
        }

        String result = String.format("处理完成：成功=%d，失败=%d", totalProcessed, totalFailed);
        log.info("[CommissionSettleCheckJob] {}", result);
        return result;
    }

}
