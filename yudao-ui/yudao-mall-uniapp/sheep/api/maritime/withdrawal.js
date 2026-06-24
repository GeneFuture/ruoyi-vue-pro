import request from '@/sheep/request';

const WithdrawalApi = {
  // 申请提现
  applyWithdrawal: (data) => {
    return request({
      url: '/maritime/withdrawal/apply',
      method: 'POST',
      data,
      custom: {
        auth: true,
        loadingMsg: '提交中',
      },
    });
  },

  // 我的提现记录（分页）
  getMyWithdrawals: (params) => {
    return request({
      url: '/maritime/withdrawal/my-list',
      method: 'GET',
      params,
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 提现税额预览
  previewTax: (amount) => {
    return request({
      url: '/maritime/withdrawal/tax-preview',
      method: 'GET',
      params: { amount },
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default WithdrawalApi;
