package cn.iocoder.yudao.module.maritime.service.tax;

import java.math.BigDecimal;

/**
 * 税务代扣 Service 接口
 *
 * @author Gene Ye
 */
public interface TaxService {

    /**
     * 计算并记录本次应扣税额（累进计算，更新月度税务记录）
     *
     * 规则：月累计 ≤ 800 元免税，超出部分 × 20%
     *
     * @param memberId     推荐人ID
     * @param payoutAmount 本次发放金额（元）
     * @return 本次应扣税额（元）
     */
    BigDecimal calculateAndRecordTax(Long memberId, BigDecimal payoutAmount);

    /**
     * 计算税额预览（不更新数据库，用于提现前展示）
     *
     * @param memberId 推荐人ID
     * @param amount   提现金额（元）
     * @return 预计税额（元）
     */
    BigDecimal calculateTaxPreview(Long memberId, BigDecimal amount);

}
