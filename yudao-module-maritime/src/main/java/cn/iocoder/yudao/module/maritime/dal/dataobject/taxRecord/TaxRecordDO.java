package cn.iocoder.yudao.module.maritime.dal.dataobject.taxRecord;

import lombok.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 税务代扣月度记录 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_tax_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRecordDO extends BaseDO {

    @TableId
    private Long id;
    /** 推荐人 member_id */
    private Long memberId;
    /** 税务月份（格式：2026-06） */
    private String taxMonth;
    /** 当月累计到账佣金（元） */
    private BigDecimal monthIncome;
    /** 当月已代扣税额（元） */
    private BigDecimal taxPaid;
    /** 乐观锁版本号 */
    private Integer version;

}
