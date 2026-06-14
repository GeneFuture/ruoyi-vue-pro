package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class ReferralSuccessEvent extends ApplicationEvent {

    /** 推荐人 memberId */
    private final Long referrerMemberId;
    private final Long commissionRecordId;
    private final BigDecimal commissionAmount;

    public ReferralSuccessEvent(Object source, Long referrerMemberId, Long commissionRecordId,
                                BigDecimal commissionAmount) {
        super(source);
        this.referrerMemberId = referrerMemberId;
        this.commissionRecordId = commissionRecordId;
        this.commissionAmount = commissionAmount;
    }

}
