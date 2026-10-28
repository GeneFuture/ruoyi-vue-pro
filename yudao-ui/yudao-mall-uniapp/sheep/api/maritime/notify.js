import request from '@/sheep/request';

const NotifyApi = {
  // 保存微信订阅授权状态（key: templateTitle, value: 'accept'|'reject'）
  saveSubscribeStatus: (data) => {
    return request({
      url: '/maritime/notify/save-subscribe-status',
      method: 'POST',
      data,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 查询站内信分页（复用 ruoyi-notify 原生接口）
  getNotifyMessagePage: (params) => {
    return request({
      url: '/notify/message/my-page',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 获取未读消息数
  getUnreadCount: (params = {}) => {
    return request({
      url: '/notify/message/get-unread-count',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 标记消息已读（ids 为数组）
  // 注意：后端为 @RequestParam("ids") List<Long>，且 luch-request 会把数组序列化成 ids[]=1&ids[]=2，
  // 与 Spring 的参数名对不上，因此这里必须用逗号拼接的字符串走 query 参数。
  updateReadStatus: (ids) => {
    return request({
      url: '/notify/message/update-read',
      method: 'PUT',
      params: { ids: (ids || []).join(',') },
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 一键全部已读
  updateAllReadStatus: () => {
    return request({
      url: '/notify/message/update-all-read',
      method: 'PUT',
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default NotifyApi;
