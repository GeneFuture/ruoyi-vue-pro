package cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 佣金账户 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_commission_account")
@KeySequence("maritime_commission_account_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionAccountDO extends BaseDO {

    /**
     * 账户ID
     */
    @TableId
    private Long id;
    /**
     * 推荐人 member_id（唯一）
     */
    private Long memberId;
    /**
     * 累计佣金总额（元）
     */
    private BigDecimal totalAmount;
    /**
     * 已发放金额（元）
     */
    private BigDecimal paidAmount;
    /**
     * 待发放金额（元）
     */
    private BigDecimal pendingAmount;
    /**
     * 冻结中金额（元）
     */
    private BigDecimal frozenAmount;
    /**
     * 被拒绝的佣金总额（元）
     */
    private BigDecimal rejectedAmount;
    /**
     * 累计成功推荐人数
     */
    private Integer totalReferralCount;
    /**
     * 乐观锁版本号
     */
    private Integer version;


}