import request from '@/sheep/request';

const SessionApi = {
  // 获取班期详情（含费用配置和进行中拼团）
  getSessionDetail: (id) => {
    return request({
      url: '/maritime/session/get',
      method: 'GET',
      params: { id },
    });
  },
};

export default SessionApi;
