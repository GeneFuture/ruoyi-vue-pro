import request from '@/sheep/request';

const StudentInfoApi = {
  // 获得我的学员信息（姓名 / 身份证 / 手机号）
  getStudentInfo: () => {
    return request({
      url: '/maritime/student-info/get',
      method: 'GET',
      custom: {
        auth: true,
        showLoading: false,
      },
    });
  },

  // 保存我的学员信息
  saveStudentInfo: (data) => {
    return request({
      url: '/maritime/student-info/save',
      method: 'PUT',
      data,
      custom: {
        auth: true,
        showLoading: true,
        loadingMsg: '保存中',
        showSuccess: true,
        successMsg: '保存成功',
      },
    });
  },
};

export default StudentInfoApi;
