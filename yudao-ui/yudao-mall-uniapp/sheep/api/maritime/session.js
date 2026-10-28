import request from '@/sheep/request';

const SessionApi = {
  // 获取班期详情（含费用配置和进行中拼团）
  // custom 可选：页面只需要静默读取费用字段时传 { showLoading: false, showError: false }
  getSessionDetail: (id, custom = {}) => {
    return request({
      url: '/maritime/session/get',
      method: 'GET',
      params: { id },
      custom,
    });
  },

  // 获取招生中班期分页（开班页：按月份/地点/证书类型筛选）
  getSessionPage: (params) => {
    return request({
      url: '/maritime/session/page',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
      },
    });
  },
};

export default SessionApi;
