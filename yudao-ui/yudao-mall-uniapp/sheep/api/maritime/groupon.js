import request from '@/sheep/request';

const GrouponApi = {
  // 获取班期进行中的拼团列表
  getActiveGroupons: (sessionId) => {
    return request({
      url: `/maritime/groupon/active/${sessionId}`,
      method: 'GET',
      custom: {
        showLoading: false,
      },
    });
  },

  // 查询我的拼团状态
  getMyGrouponStatus: (enrollmentId) => {
    return request({
      url: `/maritime/groupon/my-status/${enrollmentId}`,
      method: 'GET',
      custom: {
        auth: true,
      },
    });
  },
};

export default GrouponApi;
