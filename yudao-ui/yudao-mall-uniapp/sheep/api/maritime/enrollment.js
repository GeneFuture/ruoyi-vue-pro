import request from '@/sheep/request';

const EnrollmentApi = {
  // 创建报名记录
  createEnrollment: (data) => {
    return request({
      url: '/maritime/enrollment/create',
      method: 'POST',
      data,
      custom: {
        auth: true,
        loadingMsg: '提交中',
      },
    });
  },

  // 我的报名列表
  getMyEnrollments: () => {
    return request({
      url: '/maritime/enrollment/my-list',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 报名详情
  getEnrollment: (id) => {
    return request({
      url: '/maritime/enrollment/get',
      method: 'GET',
      params: { id },
      custom: {
        auth: true,
      },
    });
  },

  // 取消报名（仅限待支付定金状态）
  cancelEnrollment: (id) => {
    return request({
      url: '/maritime/enrollment/cancel',
      method: 'POST',
      params: { id },
      custom: {
        auth: true,
        showSuccess: true,
        successMsg: '已取消报名',
      },
    });
  },

  // 申请退费
  applyRefund: (data) => {
    return request({
      url: '/maritime/enrollment/refund-apply',
      method: 'POST',
      data,
      custom: {
        auth: true,
        showSuccess: true,
        successMsg: '退费申请已提交',
      },
    });
  },

  // 申请转课
  applyTransfer: (data) => {
    return request({
      url: '/maritime/enrollment/transfer-apply',
      method: 'POST',
      data,
      custom: {
        auth: true,
        showSuccess: true,
        successMsg: '转课申请已提交',
      },
    });
  },

  // 查询退费状态
  getRefundStatus: (enrollmentId) => {
    return request({
      url: '/maritime/enrollment/refund-status',
      method: 'GET',
      params: { enrollmentId },
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },
};

export default EnrollmentApi;
