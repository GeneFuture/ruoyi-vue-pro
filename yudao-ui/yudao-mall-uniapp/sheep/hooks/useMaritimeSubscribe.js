import NotifyApi from '@/sheep/api/maritime/notify';

/**
 * 微信订阅消息请求工具
 *
 * 在报名确认、支付确认等关键页面调用，弹出订阅授权弹窗并将结果上报后端。
 *
 * 每次最多申请 3 个模板（微信限制）。
 * 需提前在微信公众平台配置好订阅消息模板，并将 templateId 填入 .env。
 *
 * 使用方式：
 *   import { requestMaritimeSubscribe } from '@/sheep/hooks/useMaritimeSubscribe';
 *   await requestMaritimeSubscribe(['ENROLL', 'PAY']);
 */

// 模板标题 → 微信订阅消息 templateId 映射（后端以 templateTitle 为 key 存储订阅状态）
// templateId 需在微信公众平台申请后替换
const TEMPLATE_MAP = {
  ENROLL_SUCCESS: import.meta.env.SHOPRO_WX_TPL_ENROLL_SUCCESS || '',
  DEPOSIT_PAID: import.meta.env.SHOPRO_WX_TPL_DEPOSIT_PAID || '',
  GROUPON_SUCCESS: import.meta.env.SHOPRO_WX_TPL_GROUPON_SUCCESS || '',
  GROUPON_DEGRADED: import.meta.env.SHOPRO_WX_TPL_GROUPON_DEGRADED || '',
  BALANCE_REMINDER: import.meta.env.SHOPRO_WX_TPL_BALANCE_REMINDER || '',
  REFERRAL_SUCCESS: import.meta.env.SHOPRO_WX_TPL_REFERRAL_SUCCESS || '',
  COMMISSION_PAID: import.meta.env.SHOPRO_WX_TPL_COMMISSION_PAID || '',
};

/**
 * 请求订阅并上报授权结果
 * @param {string[]} keys - TEMPLATE_MAP 中的 key 列表（一次最多 3 个）
 */
export async function requestMaritimeSubscribe(keys) {
  // #ifdef MP-WEIXIN
  const tmplIds = keys
    .slice(0, 3)
    .map((k) => TEMPLATE_MAP[k])
    .filter(Boolean);

  if (tmplIds.length === 0) return;

  return new Promise((resolve) => {
    wx.requestSubscribeMessage({
      tmplIds,
      success(res) {
        // res: { templateId1: 'accept'|'reject', ... }
        const payload = {};
        tmplIds.forEach((id, idx) => {
          if (res[id]) {
            payload[keys[idx]] = res[id]; // 以 templateTitle（key）为索引上报
          }
        });
        NotifyApi.saveSubscribeStatus(payload).catch(() => {});
        resolve(res);
      },
      fail(err) {
        // 用户拒绝订阅弹窗或其他错误，不影响主流程
        resolve(null);
      },
    });
  });
  // #endif
}
