package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class BalanceReminderEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;
    private final BigDecimal balanceAmount;
    private final LocalDate startDate;
    private final LocalDate deadline;

    public BalanceReminderEvent(Object source, Long enrollmentId, Long memberId, Long sessionId,
                                BigDecimal balanceAmount, LocalDate startDate, LocalDate deadline) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.balanceAmount = balanceAmount;
        this.startDate = startDate;
        this.deadline = deadline;
    }

}
