import request from '@/sheep/request';

const MaritimeOrderApi = {
  // 发起定金支付（创建 pay 模块订单，返回 payOrderId 供前端调用微信支付）
  payDeposit: (orderId) => {
    return request({
      url: '/maritime/order/pay-deposit',
      method: 'POST',
      data: { orderId },
      custom: {
        auth: true,
        loadingMsg: '创建支付单中',
      },
    });
  },

  // 发起尾款支付（创建 pay 模块订单，返回 payOrderId 供前端调用微信支付）
  payBalance: (enrollmentId) => {
    return request({
      url: '/maritime/order/pay-balance',
      method: 'POST',
      data: { enrollmentId },
      custom: {
        auth: true,
        loadingMsg: '创建支付单中',
      },
    });
  },

  // 查询支付结果（前端轮询，处理微信回调延迟）
  getPayOrderStatus: (payOrderId) => {
    return request({
      url: '/maritime/order/status',
      method: 'GET',
      params: { payOrderId },
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default MaritimeOrderApi;
