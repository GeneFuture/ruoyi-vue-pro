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

    public DepositPaidEvent(Object source, Long enrollmentId, Long memberId, Long sessionId, BigDecimal amount) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.amount = amount;
    }

}
