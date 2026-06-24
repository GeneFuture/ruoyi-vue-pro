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
  getUnreadCount: () => {
    return request({
      url: '/notify/message/get-unread-count',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 标记消息已读（ids 为数组）
  updateReadStatus: (ids) => {
    return request({
      url: '/notify/message/update-read',
      method: 'PUT',
      data: { ids },
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
