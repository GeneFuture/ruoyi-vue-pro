/**
 * Shopro-request
 * @description api模块管理，loading配置，请求拦截，错误处理
 */

import Request from 'luch-request';
import { apiPath, baseUrl, tenantId } from '@/sheep/config';
import $store from '@/sheep/store';
import $platform from '@/sheep/platform';
import { showAuthModal } from '@/sheep/hooks/useModal';
import AuthUtil from '@/sheep/api/member/auth';
import { getTerminal } from '@/sheep/helper/const';

const options = {
  // 显示操作成功消息 默认不显示
  showSuccess: false,
  // 成功提醒 默认使用后端返回值
  successMsg: '',
  // 显示失败消息 默认显示
  showError: true,
  // 失败提醒 默认使用后端返回信息
  errorMsg: '',
  // 显示请求时loading模态框 默认显示
  showLoading: true,
  // loading提醒文字
  loadingMsg: '加载中',
  // 需要授权才能请求 默认放开
  auth: false,
  // 是否传递 token
  isToken: true,
};

// Loading全局实例
let LoadingInstance = {
  target: null,
  count: 0,
};

/**
 * 关闭loading
 */
function closeLoading() {
  if (LoadingInstance.count > 0) LoadingInstance.count--;
  if (LoadingInstance.count === 0) uni.hideLoading();
}

/**
 * @description 请求基础配置 可直接使用访问自定义请求
 */
const http = new Request({
  baseURL: baseUrl + apiPath,
  timeout: 8000,
  method: 'GET',
  header: {
    Accept: 'text/json',
    'Content-Type': 'application/json;charset=UTF-8',
    platform: $platform.name,
  },
  // #ifdef APP-PLUS
  sslVerify: false,
  // #endif
  // #ifdef H5
  // 跨域请求时是否携带凭证（cookies）仅H5支持（HBuilderX 2.6.15+）
  withCredentials: false,
  // #endif
  custom: options,
});

// ==================== 微信云托管私有协议（callContainer） ====================
// 小程序通过 wx.cloud.callContainer 走微信专线调用云托管服务，
// 无需在小程序后台配置服务器域名，也无需 ICP 备案。
// 通过 .env 的 SHOPRO_USE_CALL_CONTAINER 开关控制，可随时切回普通 HTTPS 请求。
// #ifdef MP-WEIXIN
const CLOUD_ENV = import.meta.env.SHOPRO_CLOUD_ENV;
const CLOUD_SERVICE = import.meta.env.SHOPRO_CLOUD_SERVICE;
const USE_CALL_CONTAINER =
  // 本地开发仍然直连 SHOPRO_DEV_BASE_URL，方便调试
  process.env.NODE_ENV !== 'development' &&
  import.meta.env.SHOPRO_USE_CALL_CONTAINER === 'true' &&
  !!CLOUD_ENV &&
  !!CLOUD_SERVICE &&
  typeof wx !== 'undefined' &&
  !!wx.cloud &&
  typeof wx.cloud.callContainer === 'function';

let cloudInitialized = false;

// 延迟到第一次请求时初始化，避免模块加载早于小程序 onLaunch
function ensureCloudInit() {
  if (cloudInitialized) {
    return;
  }
  wx.cloud.init({ env: CLOUD_ENV, traceUser: true });
  cloudInitialized = true;
}

// 与 luch-request 的 buildURL 行为保持一致（数组 key[]=x&key[]=y，对象 JSON 化）
function encode(val) {
  return encodeURIComponent(val)
    .replace(/%40/gi, '@')
    .replace(/%3A/gi, ':')
    .replace(/%24/g, '$')
    .replace(/%2C/gi, ',')
    .replace(/%20/g, '+')
    .replace(/%5B/gi, '[')
    .replace(/%5D/gi, ']');
}

function buildURL(url, params, paramsSerializer) {
  if (!params) {
    return url;
  }
  let serializedParams;
  if (paramsSerializer) {
    serializedParams = paramsSerializer(params);
  } else {
    const parts = [];
    Object.keys(params).forEach((key) => {
      let val = params[key];
      if (val === null || typeof val === 'undefined') {
        return;
      }
      if (Array.isArray(val)) {
        key = key + '[]';
      } else {
        val = [val];
      }
      val.forEach((v) => {
        if (v instanceof Date) {
          v = v.toISOString();
        } else if (v !== null && typeof v === 'object') {
          v = JSON.stringify(v);
        }
        parts.push(encode(key) + '=' + encode(v));
      });
    });
    serializedParams = parts.join('&');
  }
  if (serializedParams) {
    const hashmarkIndex = url.indexOf('#');
    if (hashmarkIndex !== -1) {
      url = url.slice(0, hashmarkIndex);
    }
    url += (url.indexOf('?') === -1 ? '?' : '&') + serializedParams;
  }
  return url;
}

/**
 * 通过 wx.cloud.callContainer 发起请求
 *
 * @param config 合并后的请求配置
 * @returns {Promise<Object>} 与 luch-request 拦截器兼容的 response
 */
function cloudDispatch(config, retried = false) {
  return new Promise((resolve, reject) => {
    try {
      ensureCloudInit();
    } catch (e) {
      const message = e && e.message ? e.message : String(e);
      // 云开发初始化存在短暂延迟，遇到 Cloud API isn't enabled 时重试一次
      if (!retried && /Cloud API isn't enabled/i.test(message)) {
        setTimeout(() => cloudDispatch(config, true).then(resolve, reject), 300);
        return;
      }
      reject({
        statusCode: 0,
        errMsg: '云托管初始化失败：' + message,
        config,
      });
      return;
    }

    const method = (config.method || 'GET').toUpperCase();

    // callContainer 不支持 multipart 上传 / 文件下载，回退普通请求
    if (method === 'UPLOAD' || method === 'DOWNLOAD') {
      http.middleware(config).then(resolve, reject);
      return;
    }

    let path = config.url || '';
    // 兼容绝对 URL 和已经带 /app-api 前缀的写法
    if (/^https?:\/\//i.test(path)) {
      path = path.replace(/^https?:\/\/[^/]+/i, '') || '/';
    }
    if (path !== apiPath && !path.startsWith(apiPath + '/')) {
      path = apiPath + (path.startsWith('/') ? path : '/' + path);
    }

    let params = config.params || {};
    let data = config.data;
    // 普通请求 GET 的 data 会被拼到 query，这里保持一致
    if (method === 'GET' && data && typeof data === 'object' && !(data instanceof ArrayBuffer)) {
      params = { ...params, ...data };
      data = undefined;
    }
    path = buildURL(path, params, config.paramsSerializer);

    wx.cloud.callContainer({
      config: { env: CLOUD_ENV },
      path,
      method,
      header: {
        ...config.header,
        'X-WX-SERVICE': CLOUD_SERVICE,
      },
      data,
      timeout: config.timeout,
      success: (res) => {
        const response = {
          data: res.data,
          statusCode: res.statusCode,
          header: res.header,
          cookies: res.cookies,
          errMsg: res.errMsg || '',
          config,
        };
        if (typeof response.data === 'string') {
          try {
            response.data = JSON.parse(response.data);
          } catch (e) {
            // 非 JSON 响应保持原样
          }
        }
        response.rawData = response.data;
        config.fullPath = path;

        const validateStatus =
          config.validateStatus || ((status) => status >= 200 && status < 300);
        if (!response.statusCode || validateStatus(response.statusCode)) {
          resolve(response);
        } else {
          reject(response);
        }
      },
      fail: (error) => {
        const errMsg = (error && error.errMsg) || 'callContainer:fail';
        // 云开发初始化存在短暂延迟，遇到 Cloud API isn't enabled 时重试一次
        if (!retried && /Cloud API isn't enabled/i.test(errMsg)) {
          setTimeout(() => cloudDispatch(config, true).then(resolve, reject), 300);
          return;
        }
        reject({ statusCode: 0, errMsg, config });
      },
    });
  });
}

/**
 * 与 luch-request 的 middleware 保持一致：合并默认配置 + 复用同一套请求/响应拦截器
 */
function cloudMiddleware(config) {
  config = config || {};
  config = {
    ...http.config,
    ...config,
    method: (config.method || http.config.method || 'GET').toUpperCase(),
    params: config.params || {},
    header: { ...http.config.header, ...(config.header || {}) },
    custom: { ...http.config.custom, ...(config.custom || {}) },
  };

  const chain = [cloudDispatch, undefined];
  let promise = Promise.resolve(config);
  http.interceptors.request.forEach((interceptor) => {
    chain.unshift(interceptor.fulfilled, interceptor.rejected);
  });
  http.interceptors.response.forEach((interceptor) => {
    chain.push(interceptor.fulfilled, interceptor.rejected);
  });
  while (chain.length) {
    promise = promise.then(chain.shift(), chain.shift());
  }
  return promise;
}
// #endif

/**
 * @description 请求拦截器
 */
http.interceptors.request.use(
  (config) => {
    // 自定义处理【auth 授权】：必须登录的接口，则跳出 AuthModal 登录弹窗
    if (config.custom.auth && !$store('user').isLogin) {
      showAuthModal();
      return Promise.reject();
    }

    // 自定义处理【loading 加载中】：如果需要显示 loading，则显示 loading
    if (config.custom.showLoading) {
      LoadingInstance.count++;
      LoadingInstance.count === 1 &&
        uni.showLoading({
          title: config.custom.loadingMsg,
          mask: true,
          fail: () => {
            uni.hideLoading();
          },
        });
    }

    // 增加 token 令牌、terminal 终端、tenant 租户的请求头
    const token = config.custom.isToken ? getAccessToken() : undefined;
    if (token) {
      config.header['Authorization'] = token;
    }
    config.header['terminal'] = getTerminal();

    config.header['Accept'] = '*/*';
    config.header['tenant-id'] = getTenantId();
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

/**
 * @description 响应拦截器
 */
http.interceptors.response.use(
  (response) => {
    // 约定：如果是 /auth/ 下的 URL 地址，并且返回了 accessToken 说明是登录相关的接口，则自动设置登陆令牌
    if (response.config.url.indexOf('/member/auth/') >= 0 && response.data?.data?.accessToken) {
      $store('user').setToken(response.data.data.accessToken, response.data.data.refreshToken);
    }

    // 自定处理【loading 加载中】：如果需要显示 loading，则关闭 loading
    response.config.custom.showLoading && closeLoading();

    // 自定义处理【error 错误提示】：如果需要显示错误提示，则显示错误提示
    if (response.data.code !== 0) {
      // 特殊：如果 401 错误码，则跳转到登录页 or 刷新令牌
      if (response.data.code === 401) {
        return refreshToken(response.config);
      }
      if (response.config.custom.showError) {
        // 错误提示
        uni.showToast({
          title: response.data.msg || '服务器开小差啦,请稍后再试~',
          icon: 'none',
          mask: true,
        });
      }
    }

    // 自定义处理【showSuccess 成功提示】：如果需要显示成功提示，则显示成功提示
    if (
      response.config.custom.showSuccess &&
      response.config.custom.successMsg !== '' &&
      response.data.code === 0
    ) {
      uni.showToast({
        title: response.config.custom.successMsg,
        icon: 'none',
      });
    }

    // 返回结果：包括 code + data + msg
    return Promise.resolve(response.data);
  },
  (error) => {
    const userStore = $store('user');
    const isLogin = userStore.isLogin;
    let errorMessage = '网络请求出错';
    if (error !== undefined) {
      switch (error.statusCode) {
        case 400:
          errorMessage = '请求错误';
          break;
        case 401:
          errorMessage = isLogin ? '您的登陆已过期' : '请先登录';
          // 正常情况下，后端不会返回 401 错误，所以这里不处理 handleAuthorized
          break;
        case 403:
          errorMessage = '拒绝访问';
          break;
        case 404:
          errorMessage = '请求出错';
          break;
        case 408:
          errorMessage = '请求超时';
          break;
        case 429:
          errorMessage = '请求频繁, 请稍后再访问';
          break;
        case 500:
          errorMessage = '服务器开小差啦,请稍后再试~';
          break;
        case 501:
          errorMessage = '服务未实现';
          break;
        case 502:
          errorMessage = '网络错误';
          break;
        case 503:
          errorMessage = '服务不可用';
          break;
        case 504:
          errorMessage = '网络超时';
          break;
        case 505:
          errorMessage = 'HTTP 版本不受支持';
          break;
      }
      if (error.errMsg.includes('timeout')) errorMessage = '请求超时';
      // #ifdef H5
      if (error.errMsg.includes('Network'))
        errorMessage = window.navigator.onLine ? '服务器异常' : '请检查您的网络连接';
      // #endif
    }

    if (error && error.config) {
      // 先关闭 loading，再弹错误提示：showToast 与 showLoading 共用原生弹层，
      // 顺序反了会导致 hideLoading 把错误 Toast 一起关掉，用户看不到任何提示
      error.config.custom.showLoading && closeLoading();
      if (error.config.custom.showError) {
        uni.showToast({
          title: error.data?.msg || errorMessage,
          icon: 'none',
          mask: true,
        });
      }
    }

    return false;
  },
);

// Axios 无感知刷新令牌，参考 https://www.dashingdog.cn/article/11 与 https://segmentfault.com/a/1190000020210980 实现
let requestList = []; // 请求队列
let isRefreshToken = false; // 是否正在刷新中
const refreshToken = async (config) => {
  // 如果当前已经是 refresh-token 的 URL 地址，并且还是 401 错误，说明是刷新令牌失败了，直接返回 Promise.reject(error)
  if (config.url.indexOf('/member/auth/refresh-token') >= 0) {
    return Promise.reject('error');
  }

  // 如果未认证，并且未进行刷新令牌，说明可能是访问令牌过期了
  if (!isRefreshToken) {
    // 1. 如果获取不到刷新令牌，则只能执行登出操作
    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      return handleAuthorized();
    }
    // 只有真正发起刷新时才标记刷新中，避免无刷新令牌时状态一直卡住，后续 401 不再弹登录框
    // https://github.com/yudaocode/yudao-mall-uniapp/issues/38
    isRefreshToken = true;
    // 2. 进行刷新访问令牌
    try {
      const refreshTokenResult = await AuthUtil.refreshToken(refreshToken);
      if (refreshTokenResult.code !== 0) {
        // 如果刷新不成功，直接抛出 e 触发 2.2 的逻辑
        // noinspection ExceptionCaughtLocallyJS
        throw new Error('刷新令牌失败');
      }
      // 2.1 刷新成功，则回放队列的请求 + 当前请求
      config.header.Authorization = 'Bearer ' + getAccessToken();
      requestList.forEach((cb) => {
        cb();
      });
      requestList = [];
      return request(config);
    } catch (e) {
      // 为什么需要 catch 异常呢？刷新失败时，请求因为 Promise.reject 触发异常。
      // 2.2 刷新失败，只回放队列的请求
      requestList.forEach((cb) => {
        cb();
      });
      // 提示是否要登出。即不回放当前请求！不然会形成递归
      return handleAuthorized();
    } finally {
      requestList = [];
      isRefreshToken = false;
    }
  } else {
    // 添加到队列，等待刷新获取到新的令牌
    return new Promise((resolve) => {
      requestList.push(() => {
        config.header.Authorization = 'Bearer ' + getAccessToken(); // 让每个请求携带自定义token 请根据实际情况自行修改
        resolve(request(config));
      });
    });
  }
};

/**
 * 处理 401 未登录的错误
 */
const handleAuthorized = () => {
  const userStore = $store('user');
  userStore.logout(true);
  showAuthModal();
  // 登录超时
  return Promise.reject({
    code: 401,
    msg: userStore.isLogin ? '您的登陆已过期' : '请先登录',
  });
};

/** 获得访问令牌 */
export const getAccessToken = () => {
  return uni.getStorageSync('token');
};

/** 获得刷新令牌 */
export const getRefreshToken = () => {
  return uni.getStorageSync('refresh-token');
};

/** 获得租户编号 */
export const getTenantId = () => {
  return uni.getStorageSync('tenant-id') || tenantId;
};

const request = (config) => {
  // #ifdef MP-WEIXIN
  if (USE_CALL_CONTAINER) {
    return cloudMiddleware(config);
  }
  // #endif
  return http.middleware(config);
};

export default request;
