package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class GrouponDegradedEvent extends ApplicationEvent {

    private final Long grouponRecordId;
    /** 所有超时拼团成员的 memberId */
    private final List<Long> memberIds;

    public GrouponDegradedEvent(Object source, Long grouponRecordId, List<Long> memberIds) {
        super(source);
        this.grouponRecordId = grouponRecordId;
        this.memberIds = memberIds;
    }

}
