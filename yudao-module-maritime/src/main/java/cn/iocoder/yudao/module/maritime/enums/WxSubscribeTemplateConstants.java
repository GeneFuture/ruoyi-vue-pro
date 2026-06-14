package cn.iocoder.yudao.module.maritime.enums;

/**
 * 微信订阅消息模板标题常量
 * 对应微信公众平台已添加的订阅消息模板标题，通过 SocialClientApi.getWxaSubscribeTemplateList 按 title 匹配
 */
public interface WxSubscribeTemplateConstants {

    String ENROLL_SUCCESS   = "报名成功通知";
    String DEPOSIT_PAID     = "定金支付成功";
    String GROUPON_SUCCESS  = "拼团成功通知";
    String GROUPON_DEGRADED = "拼团降级通知";
    String BALANCE_REMINDER = "尾款催缴提醒";
    String REFERRAL_SUCCESS = "推荐成功通知";
    String COMMISSION_PAID  = "佣金到账通知";

}
