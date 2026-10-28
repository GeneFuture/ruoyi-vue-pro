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
  // silent=true 时接口报错不弹 toast，用于详情页静默判断是否参与拼团
  getMyGrouponStatus: (enrollmentId, silent = false) => {
    return request({
      url: `/maritime/groupon/my-status/${enrollmentId}`,
      method: 'GET',
      custom: {
        auth: true,
        showError: !silent,
      },
    });
  },
};

export default GrouponApi;
