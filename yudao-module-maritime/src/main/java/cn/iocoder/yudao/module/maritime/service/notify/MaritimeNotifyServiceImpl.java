package cn.iocoder.yudao.module.maritime.service.notify;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.module.maritime.dal.mysql.memberSubscribe.MemberSubscribeMapper;
import cn.iocoder.yudao.module.maritime.enums.NotifyTemplateCodeConstants;
import cn.iocoder.yudao.module.maritime.enums.WxSubscribeTemplateConstants;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.social.SocialClientApi;
import cn.iocoder.yudao.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
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

    @Override
    public void sendEnrollSuccess(Long memberId, String sessionName, String startDate) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.ENROLL_SUCCESS,
                Map.of("sessionName", sessionName, "startDate", startDate),
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
    public void sendGrouponSuccess(Long memberId, String sessionName, int memberCount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.GROUPON_SUCCESS,
                Map.of("sessionName", sessionName, "memberCount", String.valueOf(memberCount)),
                WxSubscribeTemplateConstants.GROUPON_SUCCESS,
                Map.of("thing1", sessionName, "number2", String.valueOf(memberCount)),
                null);
    }

    @Override
    public void sendGrouponDegraded(Long memberId) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.GROUPON_DEGRADED,
                Map.of(),
                WxSubscribeTemplateConstants.GROUPON_DEGRADED,
                Map.of("thing1", "拼团未达成，已自动取消"),
                null);
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
    public void sendReferralSuccess(Long memberId, String commissionAmount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.REFERRAL_SUCCESS,
                Map.of("commissionAmount", commissionAmount),
                WxSubscribeTemplateConstants.REFERRAL_SUCCESS,
                Map.of("amount1", commissionAmount),
                null);
    }

    @Override
    public void sendCommissionPaid(Long memberId, String netAmount) {
        sendNotify(memberId,
                NotifyTemplateCodeConstants.COMMISSION_PAID,
                Map.of("netAmount", netAmount),
                WxSubscribeTemplateConstants.COMMISSION_PAID,
                Map.of("amount1", netAmount),
                null);
    }

    /**
     * 统一发送入口：同时发送站内信和（若已订阅）微信订阅消息
     */
    private void sendNotify(Long memberId,
                            String notifyTemplateCode,
                            Map<String, Object> notifyParams,
                            String wxTemplateTitle,
                            Map<String, String> wxMessages,
                            String wxPage) {
        // 站内信（始终发送）
        try {
            NotifySendSingleToUserReqDTO notifyReq = new NotifySendSingleToUserReqDTO();
            notifyReq.setUserId(memberId);
            notifyReq.setTemplateCode(notifyTemplateCode);
            notifyReq.setTemplateParams(notifyParams);
            notifyMessageSendApi.sendSingleMessageToMember(notifyReq);
        } catch (Exception e) {
            log.warn("[sendNotify] 站内信发送失败, memberId={}, template={}", memberId, notifyTemplateCode, e);
        }

        // 微信订阅消息（仅已授权订阅时发送）
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
