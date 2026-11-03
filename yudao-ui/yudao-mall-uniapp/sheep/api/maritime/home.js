import request from '@/sheep/request';

const HomeApi = {
  // 获取首页聚合数据（平台统计 + 近期开班）
  getHome: () => {
    return request({
      url: '/maritime/home/get',
      method: 'GET',
      custom: {
        showLoading: false,
      },
    });
  },
};

export default HomeApi;
