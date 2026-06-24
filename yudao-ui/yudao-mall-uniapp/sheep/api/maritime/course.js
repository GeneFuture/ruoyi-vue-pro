import request from '@/sheep/request';

const CourseApi = {
  // 获取课程列表（分页）
  getCourseList: (params) => {
    return request({
      url: '/maritime/course/list',
      method: 'GET',
      params,
      custom: {
        showLoading: false,
      },
    });
  },

  // 获取课程详情（含可报名班期列表）
  getCourseDetail: (id) => {
    return request({
      url: '/maritime/course/get',
      method: 'GET',
      params: { id },
    });
  },
};

export default CourseApi;
