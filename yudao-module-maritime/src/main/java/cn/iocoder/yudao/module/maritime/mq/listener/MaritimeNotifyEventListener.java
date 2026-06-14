package cn.iocoder.yudao.module.maritime.mq.listener;

import cn.iocoder.yudao.module.maritime.mq.event.*;
import cn.iocoder.yudao.module.maritime.service.notify.MaritimeNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.format.DateTimeFormatter;

/**
 * 海员培训平台 Spring Event 监听器，异步触发消息通知
 */
@Component
@Slf4j
public class MaritimeNotifyEventListener {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private MaritimeNotifyService maritimeNotifyService;

    @Async
    @EventListener
    public void onEnrollmentCreated(EnrollmentCreatedEvent event) {
        log.info("[onEnrollmentCreated] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendEnrollSuccess(event.getMemberId(), "培训班期", "待确认");
        } catch (Exception e) {
            log.warn("[onEnrollmentCreated] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onDepositPaid(DepositPaidEvent event) {
        log.info("[onDepositPaid] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            String amount = event.getAmount() != null ? event.getAmount().toPlainString() : "0";
            maritimeNotifyService.sendDepositPaid(event.getMemberId(), "培训班期", amount);
        } catch (Exception e) {
            log.warn("[onDepositPaid] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onGrouponSuccess(GrouponSuccessEvent event) {
        log.info("[onGrouponSuccess] grouponRecordId={}, memberCount={}", event.getGrouponRecordId(), event.getMemberCount());
        try {
            for (Long memberId : event.getMemberIds()) {
                try {
                    maritimeNotifyService.sendGrouponSuccess(memberId, "培训班期", event.getMemberCount());
                } catch (Exception e) {
                    log.warn("[onGrouponSuccess] 通知单个会员失败, memberId={}", memberId, e);
                }
            }
        } catch (Exception e) {
            log.warn("[onGrouponSuccess] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onGrouponDegraded(GrouponDegradedEvent event) {
        log.info("[onGrouponDegraded] grouponRecordId={}, memberCount={}", event.getGrouponRecordId(), event.getMemberIds().size());
        try {
            for (Long memberId : event.getMemberIds()) {
                try {
                    maritimeNotifyService.sendGrouponDegraded(memberId);
                } catch (Exception e) {
                    log.warn("[onGrouponDegraded] 通知单个会员失败, memberId={}", memberId, e);
                }
            }
        } catch (Exception e) {
            log.warn("[onGrouponDegraded] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onBalanceReminder(BalanceReminderEvent event) {
        log.info("[onBalanceReminder] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            String startDate = event.getStartDate() != null ? event.getStartDate().format(DATE_FMT) : "";
            String deadline = event.getDeadline() != null ? event.getDeadline().format(DATE_FMT) : "";
            String amount = event.getBalanceAmount() != null ? event.getBalanceAmount().toPlainString() : "0";
            maritimeNotifyService.sendBalanceReminder(event.getMemberId(), "培训班期", startDate, deadline, amount);
        } catch (Exception e) {
            log.warn("[onBalanceReminder] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onReferralSuccess(ReferralSuccessEvent event) {
        log.info("[onReferralSuccess] referrerMemberId={}, commissionRecordId={}", event.getReferrerMemberId(), event.getCommissionRecordId());
        try {
            String amount = event.getCommissionAmount() != null ? event.getCommissionAmount().toPlainString() : "0";
            maritimeNotifyService.sendReferralSuccess(event.getReferrerMemberId(), amount);
        } catch (Exception e) {
            log.warn("[onReferralSuccess] 通知发送失败", e);
        }
    }

    @Async
    @EventListener
    public void onCommissionPaid(CommissionPaidEvent event) {
        log.info("[onCommissionPaid] referrerMemberId={}, commissionRecordId={}", event.getReferrerMemberId(), event.getCommissionRecordId());
        try {
            String amount = event.getNetAmount() != null ? event.getNetAmount().toPlainString() : "0";
            maritimeNotifyService.sendCommissionPaid(event.getReferrerMemberId(), amount);
        } catch (Exception e) {
            log.warn("[onCommissionPaid] 通知发送失败", e);
        }
    }

}
