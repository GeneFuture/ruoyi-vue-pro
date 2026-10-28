import request from '@/sheep/request';

const PayWalletApi = {
  // 获取钱包（当前只使用余额展示，流水/充值能力已随商城页面移除）
  getPayWallet() {
    return request({
      url: '/pay/wallet/get',
      method: 'GET',
      custom: {
        showLoading: false,
        auth: true,
      },
    });
  },
};

export default PayWalletApi;
