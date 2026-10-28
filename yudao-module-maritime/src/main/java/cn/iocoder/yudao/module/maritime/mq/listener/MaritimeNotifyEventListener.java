package cn.iocoder.yudao.module.maritime.mq.listener;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.mq.event.*;
import cn.iocoder.yudao.module.maritime.service.notify.MaritimeNotifyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 海员培训平台 Spring Event 监听器，异步触发消息通知
 *
 * 使用 {@link TransactionalEventListener}（AFTER_COMMIT + fallbackExecution）而非普通 {@code @EventListener}：
 * 业务方法在同一事务内发布事件后，若事务后续步骤抛异常回滚，普通监听器仍会在事务提交前同步触发，
 * 导致用户收到"已回滚"操作的通知（如报名因超卖回滚但仍收到报名成功推送）。
 * fallbackExecution=true 兼容 BalanceReminderJob 等无事务上下文场景下的发布。
 */
@Component
@Slf4j
public class MaritimeNotifyEventListener {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private MaritimeNotifyService maritimeNotifyService;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEnrollmentCreated(EnrollmentCreatedEvent event) {
        log.info("[onEnrollmentCreated] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendEnrollSuccess(event.getMemberId(),
                    sessionName(event.getSessionId()), startDate(event.getSessionId()), event.isFullPayment());
        } catch (Exception e) {
            log.warn("[onEnrollmentCreated] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDepositPaid(DepositPaidEvent event) {
        log.info("[onDepositPaid] enrollmentId={}, memberId={}, fullPayment={}",
                event.getEnrollmentId(), event.getMemberId(), event.isFullPayment());
        try {
            String amount = amount(event.getAmount());
            if (event.isFullPayment()) {
                maritimeNotifyService.sendFullPaid(event.getMemberId(),
                        sessionName(event.getSessionId()), amount, startDate(event.getSessionId()));
            } else {
                maritimeNotifyService.sendDepositPaid(event.getMemberId(),
                        sessionName(event.getSessionId()), amount);
            }
        } catch (Exception e) {
            log.warn("[onDepositPaid] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBalancePaid(BalancePaidEvent event) {
        log.info("[onBalancePaid] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendBalancePaid(event.getMemberId(),
                    sessionName(event.getSessionId()), amount(event.getAmount()),
                    startDate(event.getSessionId()));
        } catch (Exception e) {
            log.warn("[onBalancePaid] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onGrouponSuccess(GrouponSuccessEvent event) {
        log.info("[onGrouponSuccess] grouponRecordId={}, memberCount={}", event.getGrouponRecordId(), event.getMemberCount());
        try {
            String sessionName = sessionName(event.getSessionId());
            for (Long memberId : event.getMemberIds()) {
                try {
                    maritimeNotifyService.sendGrouponSuccess(memberId, sessionName, event.getMemberCount());
                } catch (Exception e) {
                    log.warn("[onGrouponSuccess] 通知单个会员失败, memberId={}", memberId, e);
                }
            }
        } catch (Exception e) {
            log.warn("[onGrouponSuccess] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onGrouponDegraded(GrouponDegradedEvent event) {
        log.info("[onGrouponDegraded] grouponRecordId={}, memberCount={}, reason={}",
                event.getGrouponRecordId(), event.getMemberIds().size(), event.getReason());
        try {
            String sessionName = sessionName(event.getSessionId());
            String reason = StrUtil.blankToDefault(event.getReason(), "拼团未达成");
            for (Long memberId : event.getMemberIds()) {
                try {
                    maritimeNotifyService.sendGrouponDegraded(memberId, sessionName, reason);
                } catch (Exception e) {
                    log.warn("[onGrouponDegraded] 通知单个会员失败, memberId={}", memberId, e);
                }
            }
        } catch (Exception e) {
            log.warn("[onGrouponDegraded] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBalanceReminder(BalanceReminderEvent event) {
        log.info("[onBalanceReminder] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendBalanceReminder(event.getMemberId(),
                    sessionName(event.getSessionId()), dateText(event.getStartDate()),
                    dateText(event.getDeadline()), amount(event.getBalanceAmount()));
        } catch (Exception e) {
            log.warn("[onBalanceReminder] 通知发送失败", e);
        }
    }

    // ========== P1 新增：退费 / 取消 / 逾期 / 结班 ==========

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRefundApplied(RefundAppliedEvent event) {
        log.info("[onRefundApplied] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendRefundApplied(event.getMemberId(), sessionName(event.getSessionId()));
        } catch (Exception e) {
            log.warn("[onRefundApplied] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRefundResult(RefundResultEvent event) {
        log.info("[onRefundResult] enrollmentId={}, approved={}", event.getEnrollmentId(), event.isApproved());
        try {
            String sessionName = sessionName(event.getSessionId());
            if (event.isApproved()) {
                maritimeNotifyService.sendRefundApproved(event.getMemberId(), sessionName,
                        amount(event.getRefundAmount()));
            } else {
                maritimeNotifyService.sendRefundRejected(event.getMemberId(), sessionName,
                        StrUtil.blankToDefault(event.getReason(), "未说明原因"));
            }
        } catch (Exception e) {
            log.warn("[onRefundResult] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEnrollmentCancelled(EnrollmentCancelledEvent event) {
        log.info("[onEnrollmentCancelled] enrollmentId={}, reason={}", event.getEnrollmentId(), event.getReason());
        try {
            maritimeNotifyService.sendEnrollCancelled(event.getMemberId(),
                    sessionName(event.getSessionId()),
                    StrUtil.blankToDefault(event.getReason(), "报名已取消"));
        } catch (Exception e) {
            log.warn("[onEnrollmentCancelled] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEnrollmentOverdue(EnrollmentOverdueEvent event) {
        log.info("[onEnrollmentOverdue] enrollmentId={}, reason={}", event.getEnrollmentId(), event.getReason());
        try {
            maritimeNotifyService.sendBalanceOverdue(event.getMemberId(),
                    sessionName(event.getSessionId()), amount(event.getBalanceAmount()),
                    StrUtil.blankToDefault(event.getReason(), "尾款逾期未付"));
        } catch (Exception e) {
            log.warn("[onEnrollmentOverdue] 通知发送失败", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEnrollmentCompleted(EnrollmentCompletedEvent event) {
        log.info("[onEnrollmentCompleted] enrollmentId={}, memberId={}", event.getEnrollmentId(), event.getMemberId());
        try {
            maritimeNotifyService.sendEnrollCompleted(event.getMemberId(), sessionName(event.getSessionId()));
        } catch (Exception e) {
            log.warn("[onEnrollmentCompleted] 通知发送失败", e);
        }
    }

    // ========== 工具方法 ==========

    private CourseSessionDO getSession(Long sessionId) {
        return sessionId != null ? courseSessionMapper.selectById(sessionId) : null;
    }

    private String sessionName(Long sessionId) {
        CourseSessionDO session = getSession(sessionId);
        if (session == null) {
            return "培训班期";
        }
        if (StrUtil.isNotBlank(session.getSessionName())) {
            return session.getSessionName();
        }
        return StrUtil.blankToDefault(session.getSessionCode(), "培训班期");
    }

    private String startDate(Long sessionId) {
        CourseSessionDO session = getSession(sessionId);
        return session != null ? dateText(session.getStartDate()) : "待定";
    }

    private String dateText(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "待定";
    }

    private String amount(BigDecimal value) {
        return value != null ? value.toPlainString() : "0";
    }

}
