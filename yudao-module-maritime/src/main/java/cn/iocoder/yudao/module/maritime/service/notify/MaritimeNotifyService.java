package cn.iocoder.yudao.module.maritime.service.notify;

/**
 * 海员培训平台消息通知 Service
 * 封装站内信 + 微信订阅消息的统一发送入口
 */
public interface MaritimeNotifyService {

    void sendEnrollSuccess(Long memberId, String sessionName, String startDate);

    void sendDepositPaid(Long memberId, String sessionName, String amount);

    void sendGrouponSuccess(Long memberId, String sessionName, int memberCount);

    void sendGrouponDegraded(Long memberId);

    void sendBalanceReminder(Long memberId, String sessionName, String startDate,
                             String deadline, String amount);

    void sendReferralSuccess(Long memberId, String commissionAmount);

    void sendCommissionPaid(Long memberId, String netAmount);

}
