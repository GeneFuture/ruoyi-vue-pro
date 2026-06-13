package cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 退费申请 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_refund_apply")
@KeySequence("maritime_refund_apply_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundApplyDO extends BaseDO {

    /**
     * 退费申请ID
     */
    @TableId
    private Long id;
    /**
     * 报名ID
     */
    private Long enrollmentId;
    /**
     * 关联报名订单ID（maritime_enrollment_order.id，退哪笔订单）
     */
    private Long orderId;
    /**
     * 申请人
     */
    private Long memberId;
    /**
     * 退费原因
     */
    private String applyReason;
    /**
     * 退费金额（元，审核后填写）
     */
    private BigDecimal refundAmount;
    /**
     * 申请状态（PENDING/APPROVED/REJECTED/REFUNDED）
     */
    private String applyStatus;
    /**
     * 管理员备注
     */
    private String adminRemark;
    /**
     * 审核时间
     */
    private LocalDateTime approveTime;
    /**
     * 审核人（系统用户ID）
     */
    private Long approverId;
    /**
     * ruoyi pay_refund.id（退款发起后填写）
     */
    private Long payRefundId;
    /**
     * 实际退款到账时间
     */
    private LocalDateTime refundTime;


}