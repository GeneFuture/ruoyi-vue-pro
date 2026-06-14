package cn.iocoder.yudao.module.maritime.mq.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class CommissionPaidEvent extends ApplicationEvent {

    /** 收款推荐人 memberId */
    private final Long referrerMemberId;
    private final Long commissionRecordId;
    /** 税后实际到账金额（元） */
    private final BigDecimal netAmount;

    public CommissionPaidEvent(Object source, Long referrerMemberId, Long commissionRecordId,
                               BigDecimal netAmount) {
        super(source);
        this.referrerMemberId = referrerMemberId;
        this.commissionRecordId = commissionRecordId;
        this.netAmount = netAmount;
    }

}
