package cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 佣金记录 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_commission_record")
@KeySequence("maritime_commission_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRecordDO extends BaseDO {

    /**
     * 佣金记录ID
     */
    @TableId
    private Long id;
    /**
     * 推荐人
     */
    private Long referrerMemberId;
    /**
     * 被推荐人的报名ID
     */
    private Long referredEnrollmentId;
    /**
     * 班期ID
     */
    private Long sessionId;
    /**
     * 佣金金额（元）
     */
    private BigDecimal commissionAmount;
    /**
     * 状态(WAITING_FOR_CLASS/FROZEN/PENDING_REVIEW/REVIEWING/PENDING_PAYOUT/PAYING/PAID/REJECTED/FAILED/MANUALLY_PAID)
     */
    private String commissionStatus;
    /**
     * 预计结算日期（= 开课日期 + 7天）
     */
    private LocalDate expectedSettleDate;
    /**
     * 是否风控标记
     */
    private Boolean isRiskFlagged;
    /**
     * 风控检查详情（JSON）
     */
    private String riskCheckResult;
    /**
     * 触发时间（全款支付时）
     */
    private LocalDateTime triggerTime;
    /**
     * 开课7天检查执行时间
     */
    private LocalDateTime settleCheckTime;
    /**
     * 审核通过时间
     */
    private LocalDateTime approveTime;
    /**
     * 审核人
     */
    private Long approverId;
    /**
     * 审核备注
     */
    private String approveRemark;
    /**
     * 发放时间
     */
    private LocalDateTime payoutTime;
    /**
     * ruoyi pay_transfer.id（调用 PayTransferApi 后填写）
     */
    private Long payTransferId;
    /**
     * 发放失败原因
     */
    private String failReason;
    /**
     * 自动发放失败后已重试次数
     */
    private Integer retryCount;


}