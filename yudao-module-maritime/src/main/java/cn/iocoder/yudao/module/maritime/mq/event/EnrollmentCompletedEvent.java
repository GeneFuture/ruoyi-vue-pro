package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 培训完成事件
 */
@Getter
public class EnrollmentCompletedEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;

    public EnrollmentCompletedEvent(Object source, Long enrollmentId, Long memberId, Long sessionId) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
    }

}
