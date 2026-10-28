package cn.iocoder.yudao.module.maritime.service.notify;

/**
 * 海员培训平台消息通知 Service
 * 封装站内信 + 微信订阅消息的统一发送入口
 */
public interface MaritimeNotifyService {

    // ========== 报名 / 支付 ==========

    /** 报名提交成功（支付定金前） */
    void sendEnrollSuccess(Long memberId, String sessionName, String startDate, boolean fullPayment);

    /** 定金支付成功 */
    void sendDepositPaid(Long memberId, String sessionName, String amount);

    /** 全款支付成功 */
    void sendFullPaid(Long memberId, String sessionName, String amount, String startDate);

    /** 尾款支付成功 */
    void sendBalancePaid(Long memberId, String sessionName, String amount, String startDate);

    /** 尾款催缴提醒 */
    void sendBalanceReminder(Long memberId, String sessionName, String startDate,
                             String deadline, String amount);

    /** 尾款逾期转人工 */
    void sendBalanceOverdue(Long memberId, String sessionName, String amount, String reason);

    /** 报名取消 */
    void sendEnrollCancelled(Long memberId, String sessionName, String reason);

    /** 培训完成 */
    void sendEnrollCompleted(Long memberId, String sessionName);

    // ========== 拼团 ==========

    void sendGrouponSuccess(Long memberId, String sessionName, int memberCount);

    void sendGrouponDegraded(Long memberId, String sessionName, String reason);

    // ========== 退费 ==========

    void sendRefundApplied(Long memberId, String sessionName);

    void sendRefundApproved(Long memberId, String sessionName, String amount);

    void sendRefundRejected(Long memberId, String sessionName, String reason);

}
