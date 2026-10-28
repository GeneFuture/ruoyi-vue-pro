package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class DepositPaidEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;
    private final BigDecimal amount;
    /** 是否全款报名（FULL 订单），用于区分“定金支付成功”和“全款支付成功”文案 */
    private final boolean fullPayment;

    public DepositPaidEvent(Object source, Long enrollmentId, Long memberId, Long sessionId,
                            BigDecimal amount, boolean fullPayment) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.amount = amount;
        this.fullPayment = fullPayment;
    }

}
