package cn.iocoder.yudao.module.maritime.dal.dataobject.withdrawalApply;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 佣金提现申请 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_withdrawal_apply")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalApplyDO extends BaseDO {

    @TableId
    private Long id;
    /** 申请人 */
    private Long memberId;
    /** 申请提现金额（元） */
    private BigDecimal applyAmount;
    /** 代扣税额（元） */
    private BigDecimal taxAmount;
    /** 实际到账金额（元） */
    private BigDecimal netAmount;
    /** 提现渠道（WX_BALANCE/BANK_CARD） */
    private String channel;
    /** 状态（PENDING/PROCESSING/SUCCESS/FAILED/REJECTED） */
    private String applyStatus;
    /** ruoyi pay_transfer.id */
    private Long payTransferId;
    /** 拒绝原因 */
    private String rejectReason;
    /** 失败原因 */
    private String failReason;
    /** 到账时间 */
    private LocalDateTime successTime;

}
