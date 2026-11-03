package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 报名取消事件（用户取消、管理员取消、订单超时取消、人工处理取消）
 */
@Getter
public class EnrollmentCancelledEvent extends ApplicationEvent {

    private final Long enrollmentId;
    private final Long memberId;
    private final Long sessionId;
    private final String reason;

    public EnrollmentCancelledEvent(Object source, Long enrollmentId, Long memberId,
                                    Long sessionId, String reason) {
        super(source);
        this.enrollmentId = enrollmentId;
        this.memberId = memberId;
        this.sessionId = sessionId;
        this.reason = reason;
    }

}
