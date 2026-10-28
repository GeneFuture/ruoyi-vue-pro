package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class GrouponDegradedEvent extends ApplicationEvent {

    private final Long grouponRecordId;
    private final Long sessionId;
    /** 降级 / 结束原因，如“拼团超时未成团”“成员提前付清尾款，拼团结束” */
    private final String reason;
    /** 所有拼团成员的 memberId */
    private final List<Long> memberIds;

    public GrouponDegradedEvent(Object source, Long grouponRecordId, Long sessionId,
                                String reason, List<Long> memberIds) {
        super(source);
        this.grouponRecordId = grouponRecordId;
        this.sessionId = sessionId;
        this.reason = reason;
        this.memberIds = memberIds;
    }

}
