package cn.iocoder.yudao.module.maritime.service.notify;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.maritime.dal.mysql.memberSubscribe.MemberSubscribeMapper;
import cn.iocoder.yudao.module.maritime.enums.NotifyTemplateCodeConstants;
import cn.iocoder.yudao.module.maritime.enums.WxSubscribeTemplateConstants;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@Validated
@Slf4j
public class MaritimeNotifyServiceImpl implements MaritimeNotifyService {

    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;

    @Resource
    private SocialClientApi socialClientApi;

    @Resource
    private MemberSubscribeMapper memberSubscribeMapper;

    // ========== 报名 / 支付 ==========

    @Override
    public void sendEnrollSuccess(Long memberId, String sessionName, String startDate, boolean fullPayment) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.ENROLL_SUCCESS,
                Map.of("sessionName", sessionName, "startDate", startDate,
                        "payType", fullPayment ? "全款" : "定金"),
                WxSubscribeTemplateConstants.ENROLL_SUCCESS,
                Map.of("thing1", sessionName, "date2", startDate),
                null);
    }

    @Override
    public void sendDepositPaid(Long memberId, String sessionName, String amount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.DEPOSIT_PAID,
                Map.of("sessionName", sessionName, "amount", amount),
                WxSubscribeTemplateConstants.DEPOSIT_PAID,
                Map.of("thing1", sessionName, "amount2", amount),
                null);
    }

    @Override
    public void sendFullPaid(Long memberId, String sessionName, String amount, String startDate) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.FULL_PAID,
                Map.of("sessionName", sessionName, "amount", amount, "startDate", startDate));
    }

    @Override
    public void sendBalancePaid(Long memberId, String sessionName, String amount, String startDate) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.BALANCE_PAID,
                Map.of("sessionName", sessionName, "amount", amount, "startDate", startDate));
    }

    @Override
    public void sendBalanceReminder(Long memberId, String sessionName, String startDate,
                                    String deadline, String amount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.BALANCE_REMINDER,
                Map.of("sessionName", sessionName, "startDate", startDate,
                        "deadline", deadline, "amount", amount),
                WxSubscribeTemplateConstants.BALANCE_REMINDER,
                Map.of("thing1", sessionName, "date2", startDate,
                        "date3", deadline, "amount4", amount),
                null);
    }

    @Override
    public void sendBalanceOverdue(Long memberId, String sessionName, String amount, String reason) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.BALANCE_OVERDUE,
                Map.of("sessionName", sessionName, "amount", amount, "reason", reason));
    }

    @Override
    public void sendEnrollCancelled(Long memberId, String sessionName, String reason) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.ENROLL_CANCELLED,
                Map.of("sessionName", sessionName, "reason", reason));
    }

    @Override
    public void sendEnrollCompleted(Long memberId, String sessionName) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.ENROLL_COMPLETED,
                Map.of("sessionName", sessionName));
    }

    // ========== 拼团 ==========

    @Override
    public void sendGrouponSuccess(Long memberId, String sessionName, int memberCount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.GROUPON_SUCCESS,
                Map.of("sessionName", sessionName, "memberCount", String.valueOf(memberCount)),
                WxSubscribeTemplateConstants.GROUPON_SUCCESS,
                Map.of("thing1", sessionName, "number2", String.valueOf(memberCount)),
                null);
    }

    @Override
    public void sendGrouponDegraded(Long memberId, String sessionName, String reason) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.GROUPON_DEGRADED,
                Map.of("sessionName", sessionName, "reason", reason),
                WxSubscribeTemplateConstants.GROUPON_DEGRADED,
                Map.of("thing1", reason),
                null);
    }

    // ========== 退费 ==========

    @Override
    public void sendRefundApplied(Long memberId, String sessionName) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.REFUND_APPLIED,
                Map.of("sessionName", sessionName));
    }

    @Override
    public void sendRefundApproved(Long memberId, String sessionName, String amount) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.REFUND_APPROVED,
                Map.of("sessionName", sessionName, "amount", amount));
    }

    @Override
    public void sendRefundRejected(Long memberId, String sessionName, String reason) {
        sendStationNotify(memberId, NotifyTemplateCodeConstants.REFUND_REJECTED,
                Map.of("sessionName", sessionName, "reason", reason));
    }

    // ========== 发送实现 ==========

    /**
     * 只发站内信。用于没有配置微信订阅消息模板的通知类型。
     */
    private void sendStationNotify(Long memberId, String notifyTemplateCode, Map<String, Object> notifyParams) {
        sendNotify(memberId, notifyTemplateCode, notifyParams, null, Map.of(), null);
    }

    /**
     * 统一发送入口：站内信始终尝试发送；wxTemplateTitle 不为空且用户已订阅时，再发微信订阅消息。
     */
    private void sendNotify(Long memberId,
                            String notifyTemplateCode,
                            Map<String, Object> notifyParams,
                            String wxTemplateTitle,
                            Map<String, String> wxMessages,
                            String wxPage) {
        // 站内信
        try {
            NotifySendSingleToUserReqDTO notifyReq = new NotifySendSingleToUserReqDTO();
            notifyReq.setUserId(memberId);
            notifyReq.setTemplateCode(notifyTemplateCode);
            notifyReq.setTemplateParams(notifyParams);
            notifyMessageSendApi.sendSingleMessageToMember(notifyReq);
        } catch (Exception e) {
            log.warn("[sendNotify] 站内信发送失败, memberId={}, template={}", memberId, notifyTemplateCode, e);
        }

        // 微信订阅消息：没有对应模板时跳过
        if (wxTemplateTitle == null) {
            return;
        }
        if (memberSubscribeMapper.isSubscribed(memberId, wxTemplateTitle)) {
            try {
                SocialWxaSubscribeMessageSendReqDTO wxReq = new SocialWxaSubscribeMessageSendReqDTO();
                wxReq.setUserId(memberId);
                wxReq.setUserType(UserTypeEnum.MEMBER.getValue());
                wxReq.setTemplateTitle(wxTemplateTitle);
                wxReq.setPage(wxPage);
                wxMessages.forEach(wxReq::addMessage);
                socialClientApi.sendWxaSubscribeMessage(wxReq);
            } catch (Exception e) {
                log.warn("[sendNotify] 微信订阅消息发送失败, memberId={}, template={}", memberId, wxTemplateTitle, e);
            }
        }
    }

}
