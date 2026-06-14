package cn.iocoder.yudao.module.maritime.service.tax;

import cn.iocoder.yudao.module.maritime.dal.dataobject.taxRecord.TaxRecordDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.taxRecord.TaxRecordMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

/**
 * 税务代扣 Service 实现类
 *
 * 规则：月累计到账佣金 ≤ 800 元免税，超出部分 × 20%（累进计算，避免重复征税）
 *
 * @author Gene Ye
 */
@Slf4j
@Service
public class TaxServiceImpl implements TaxService {

    private static final BigDecimal EXEMPT = new BigDecimal("800");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.20");

    @Resource
    private TaxRecordMapper taxRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal calculateAndRecordTax(Long memberId, BigDecimal payoutAmount) {
        String taxMonth = YearMonth.now().toString();

        TaxRecordDO record = getOrCreate(memberId, taxMonth);
        BigDecimal monthIncomeBefore = record.getMonthIncome();
        BigDecimal monthIncomeAfter = monthIncomeBefore.add(payoutAmount);

        BigDecimal thisTax = computeIncrementalTax(monthIncomeBefore, monthIncomeAfter);

        for (int i = 0; i < 3; i++) {
            int updated = taxRecordMapper.updateIncrease(record.getId(), payoutAmount, thisTax, record.getVersion());
            if (updated > 0) {
                log.info("[calculateAndRecordTax] 代扣税: memberId={}, payout={}, tax={}, month={}",
                        memberId, payoutAmount, thisTax, taxMonth);
                return thisTax;
            }
            // 乐观锁冲突，重新读取
            record = taxRecordMapper.selectByMemberAndMonth(memberId, taxMonth);
            if (record == null) {
                record = getOrCreate(memberId, taxMonth);
            }
            monthIncomeBefore = record.getMonthIncome();
            monthIncomeAfter = monthIncomeBefore.add(payoutAmount);
            thisTax = computeIncrementalTax(monthIncomeBefore, monthIncomeAfter);
        }

        log.warn("[calculateAndRecordTax] 乐观锁重试3次失败，使用0税额: memberId={}", memberId);
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTaxPreview(Long memberId, BigDecimal amount) {
        String taxMonth = YearMonth.now().toString();
        TaxRecordDO record = taxRecordMapper.selectByMemberAndMonth(memberId, taxMonth);
        BigDecimal monthIncomeBefore = record != null ? record.getMonthIncome() : BigDecimal.ZERO;
        return computeIncrementalTax(monthIncomeBefore, monthIncomeBefore.add(amount));
    }

    private TaxRecordDO getOrCreate(Long memberId, String taxMonth) {
        TaxRecordDO record = taxRecordMapper.selectByMemberAndMonth(memberId, taxMonth);
        if (record == null) {
            record = TaxRecordDO.builder()
                    .memberId(memberId)
                    .taxMonth(taxMonth)
                    .monthIncome(BigDecimal.ZERO)
                    .taxPaid(BigDecimal.ZERO)
                    .version(0)
                    .build();
            try {
                taxRecordMapper.insert(record);
            } catch (Exception e) {
                // 并发插入冲突，重新查询
                record = taxRecordMapper.selectByMemberAndMonth(memberId, taxMonth);
            }
        }
        return record;
    }

    private BigDecimal computeIncrementalTax(BigDecimal before, BigDecimal after) {
        BigDecimal taxBefore = before.compareTo(EXEMPT) <= 0
                ? BigDecimal.ZERO
                : before.subtract(EXEMPT).multiply(TAX_RATE);
        BigDecimal taxAfter = after.compareTo(EXEMPT) <= 0
                ? BigDecimal.ZERO
                : after.subtract(EXEMPT).multiply(TAX_RATE);
        return taxAfter.subtract(taxBefore).setScale(2, RoundingMode.HALF_UP);
    }

}
