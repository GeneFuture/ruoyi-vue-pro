package cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 人工处理单 DO
 *
 * 通用人工处理工单：尾款逾期、退费申请、退款金额确认、其他异常等场景共用。
 */
@TableName("maritime_manual_task")
@KeySequence("maritime_manual_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualTaskDO extends BaseDO {

    /**
     * 人工处理单ID
     */
    @TableId
    private Long id;

    /**
     * 人工处理单号
     */
    private String taskNo;

    /**
     * 报名ID
     */
    private Long enrollmentId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 关联支付订单ID
     */
    private Long orderId;

    /**
     * 来源类型（REFUND/BALANCE/GROUPON/OTHER）
     */
    private String sourceType;

    /**
     * 来源记录ID
     */
    private Long sourceId;

    /**
     * 任务类型（BALANCE_OVERDUE/REFUND_APPLY/REFUND_AMOUNT_CONFIRM/OTHER）
     */
    private String taskType;

    /**
     * 状态（PENDING/PROCESSING/DONE/CANCELLED）
     */
    private String status;

    /**
     * 优先级（越大越优先）
     */
    private Integer priority;

    /**
     * 进入人工的原因
     */
    private String reason;

    /**
     * 处理人用户ID
     */
    private Long handlerId;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

}
