package cn.iocoder.yudao.module.maritime.service.finance;

import cn.iocoder.yudao.module.maritime.controller.admin.finance.vo.FinanceDailyFlowRespVO;

import java.time.LocalDate;

/**
 * 管理后台 - 资金流水 Service 接口
 *
 * @author Gene Ye
 */
public interface AdminFinanceService {

    /**
     * 获得每日资金流与日期范围汇总
     *
     * @param startDate 开始日期（含），为空默认近 30 天
     * @param endDate   结束日期（含），为空默认今天
     * @return 资金汇总 + 每日明细
     */
    FinanceDailyFlowRespVO getDailyFlow(LocalDate startDate, LocalDate endDate);

}
