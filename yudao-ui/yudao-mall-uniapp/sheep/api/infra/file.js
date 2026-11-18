import { baseUrl, apiPath, tenantId } from '@/sheep/config';
import request, { getAccessToken } from '@/sheep/request';

// callContainer 私有协议不支持 multipart 上传；本地开发仍可正常上传
const useCallContainer =
  process.env.NODE_ENV !== 'development' &&
  import.meta.env.SHOPRO_USE_CALL_CONTAINER === 'true';

const FileApi = {
  // 上传文件
  uploadFile: (file, directory = '') => {
    // #ifdef MP-WEIXIN
    if (useCallContainer) {
      uni.showToast({ title: '当前版本暂不支持上传文件', icon: 'none' });
      return Promise.resolve(false);
    }
    // #endif
    uni.showLoading({
      title: '上传中',
    });
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: baseUrl + apiPath + '/infra/file/upload',
        filePath: file,
        name: 'file',
        header: {
          Accept: '*/*',
          'tenant-id': tenantId,
          Authorization: 'Bearer ' + getAccessToken(),
        },
        formData: {
          directory,
        },
        success: (uploadFileRes) => {
          let result = JSON.parse(uploadFileRes.data);
          if (result.error === 1) {
            uni.showToast({
              icon: 'none',
              title: result.msg,
            });
          } else {
            return resolve(result);
          }
        },
        fail: (error) => {
          console.log('上传失败：', error);
          return resolve(false);
        },
        complete: () => {
          uni.hideLoading();
        },
      });
    });
  },

  // 获取文件预签名地址
  getFilePresignedUrl: (name, directory) => {
    return request({
      url: '/infra/file/presigned-url',
      method: 'GET',
      params: {
        name,
        directory,
      },
    });
  },

  // 创建文件
  createFile: (data) => {
    return request({
      url: '/infra/file/create', // 请求的 URL
      method: 'POST', // 请求方法
      data: data, // 要发送的数据
    });
  },
};

export default FileApi;
