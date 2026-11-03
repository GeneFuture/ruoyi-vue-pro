package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 尾款逾期 / 开班未付清，报名转人工处理事件
 */
@Getter
public class EnrollmentOverdueEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;
    private final BigDecimal balanceAmount;
    private final String reason;

    public EnrollmentOverdueEvent(Object source, Long enrollmentId, Long memberId,
                                  Long sessionId, BigDecimal balanceAmount, String reason) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.balanceAmount = balanceAmount;
        this.reason = reason;
    }

}
