package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 退费审核结果事件
 */
@Getter
public class RefundResultEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;
    private final BigDecimal refundAmount;
    private final boolean approved;
    /** 拒绝原因；通过时可为空 */
    private final String reason;

    public RefundResultEvent(Object source, Long enrollmentId, Long memberId, Long sessionId,
                             BigDecimal refundAmount, boolean approved, String reason) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.refundAmount = refundAmount;
        this.approved = approved;
        this.reason = reason;
    }

}
