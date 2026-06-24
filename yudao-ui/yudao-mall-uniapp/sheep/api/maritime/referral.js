import request from '@/sheep/request';

const ReferralApi = {
  // 获得我的推荐信息（推荐码、佣金汇总）
  getMyReferralInfo: () => {
    return request({
      url: '/maritime/referral/my-info',
      method: 'GET',
      custom: {
        auth: true,
      },
    });
  },

  // 获得分享参数
  getShareParams: (sessionId) => {
    return request({
      url: '/maritime/referral/share-params',
      method: 'GET',
      params: sessionId ? { sessionId } : {},
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 获得我的推荐记录（分页）
  getMyReferralList: (params) => {
    return request({
      url: '/maritime/referral/my-list',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 获得我的佣金记录（分页）
  getMyCommissionRecords: (params) => {
    return request({
      url: '/maritime/referral/my-commission-records',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 获得我的佣金账户汇总
  getMyCommissionAccount: () => {
    return request({
      url: '/maritime/referral/my-account',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default ReferralApi;
