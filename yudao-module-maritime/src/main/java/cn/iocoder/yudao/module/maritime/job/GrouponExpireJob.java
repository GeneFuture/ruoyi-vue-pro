package cn.iocoder.yudao.module.maritime.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团超时降级定时任务
 *
 * 每5分钟扫描 IN_PROGRESS 且 expire_time < now 的拼团，逐一降级。
 * 在后台「基础设施 → 定时任务」配置 Cron: 0 *\/5 * * * ?
 *
 * @author Gene Ye
 */
@Slf4j
@Component
public class GrouponExpireJob implements JobHandler {

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private GrouponService grouponService;

    @Override
    public String execute(String param) throws Exception {
        List<GrouponRecordDO> expiredRecords = grouponRecordMapper.selectExpiredInProgress(LocalDateTime.now());
        log.info("[GrouponExpireJob] 扫描到超时拼团数: {}", expiredRecords.size());

        int degraded = 0;
        for (GrouponRecordDO record : expiredRecords) {
            try {
                grouponService.degradeGroupon(record.getId());
                degraded++;
            } catch (Exception e) {
                log.error("[GrouponExpireJob] 拼团降级失败, recordId={}", record.getId(), e);
            }
        }

        String result = String.format("共扫描 %d 条，成功降级 %d 条", expiredRecords.size(), degraded);
        log.info("[GrouponExpireJob] 执行完毕: {}", result);
        return result;
    }

}
