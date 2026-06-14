package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class GrouponSuccessEvent extends ApplicationEvent {

    private final Long grouponRecordId;
    private final Long sessionId;
    /** 所有拼团成员的 memberId */
    private final List<Long> memberIds;
    private final int memberCount;

    public GrouponSuccessEvent(Object source, Long grouponRecordId, Long sessionId,
                               List<Long> memberIds) {
        super(source);
        this.grouponRecordId = grouponRecordId;
        this.sessionId = sessionId;
        this.memberIds = memberIds;
        this.memberCount = memberIds.size();
    }

}
