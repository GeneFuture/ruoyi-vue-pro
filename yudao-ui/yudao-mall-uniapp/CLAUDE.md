# 小程序端（uni-app）开发规范

> 本文件只在 Claude 处理 `yudao-ui/yudao-mall-uniapp/` 下的文件时加载；通用的后端约定见仓库根 `CLAUDE.md`。

**项目路径**：`yudao-ui/yudao-mall-uniapp/`

### API 调用规范

```js
// sheep/api/maritime/course.js
import request from '@/sheep/request';

const CourseApi = {
  // 获取课程列表（分页）
  getCoursePage: (params) => request({ url: '/app-api/maritime/course/page', method: 'GET', data: params }),

  // 获取课程详情
  getCourse: (id) => request({ url: '/app-api/maritime/course/get', method: 'GET', data: { id } }),

  // 报名登记
  createEnrollment: (data) => request({
    url: '/app-api/maritime/enrollment/create',
    method: 'POST',
    data,
    custom: { showSuccess: true, successMsg: '报名成功', loadingMsg: '提交中' }
  }),
};
export default CourseApi;
```

- 文件名：`camelCase.js`，导出对象名：`XxxApi`
- URL 前缀：App 端接口用 `/app-api/`（框架自动带 `baseUrl`，无需写完整域名）
- `custom` 字段控制 loading/成功提示/错误提示

### Pinia Store 规范

```js
// sheep/store/maritime.js
import { defineStore } from 'pinia';
import CourseApi from '@/sheep/api/maritime/course';

const maritime = defineStore('maritime', {
  state: () => ({
    courseList: [],
    currentCourse: null,
  }),
  actions: {
    async getCourseList(params) {
      const { code, data } = await CourseApi.getCoursePage(params);
      if (code !== 0) return;
      this.courseList = data.list;
    },
  },
});
export default maritime;
```

- Store 文件放 `sheep/store/`，用 `defineStore` Options API 风格
- 页面通过 `sheep.$store('maritime')` 访问
- 响应码 `0` 为成功（框架约定）

### 页面组件规范

```vue
<template>
  <s-layout title="课程列表">
    <view class="ss-p-20">
      <view
        v-for="course in state.courses"
        :key="course.id"
        class="ss-m-b-20"
        @tap="onCourseDetail(course.id)"
      >
        <s-goods-item :title="course.name" :img="course.picUrl" :price="course.price" />
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';

  const state = reactive({ courses: [] });

  onLoad(async () => {
    const { code, data } = await CourseApi.getCoursePage({});
    if (code === 0) state.courses = data.list;
  });

  const onCourseDetail = (id) => {
    sheep.$router.go('/pages/maritime/course-detail', { id });
  };
</script>
```

**关键约定：**
- 使用 `<script setup>` + Composition API
- 生命周期钩子从 `@dcloudio/uni-app` 导入（`onLoad`、`onShow`、`onPullDownRefresh`）
- 布局用 `<s-layout>`，内置导航栏和 tabbar 支持
- 框架内置组件前缀 `s-`（如 `s-goods-item`、`s-empty`、`s-layout`）
- UI 基础组件前缀 `su-`（如 `su-tabs`、`su-sticky`）
- 路由跳转用 `sheep.$router.go(path, params)`
- 全局状态用 `sheep.$store('storeName')`
- CSS 类用框架内置工具类（`ss-p-20`=padding 20rpx，`ss-m-b-20`=margin-bottom 20rpx，`ss-flex`=display flex 等）

### 微信登录流程

```js
// 平台适配层自动处理，页面层调用：
import AuthUtil from '@/sheep/api/member/auth';

// 微信小程序登录
const res = await AuthUtil.weixinMiniAppLogin(code, encryptedData, iv);
// 登录成功后 token 自动存 uni.setStorageSync('token', ...)
// 用户信息存 sheep.$store('user').userInfo
```

- 微信登录由 `sheep/platform/provider/wechat/` 处理平台差异（`#ifdef MP-WEIXIN`）
- 需要登录的接口在 `custom.auth = true` 时自动弹出授权弹窗

### 支付流程（小程序端）

```js
import PayApi from '@/sheep/api/pay/order';

// 1. 调后端创建支付单
const { code, data } = await PayApi.createPayOrder({ bizOrderId: orderId, channelCode: 'wx_lite' });

// 2. 发起微信支付
uni.requestPayment({
  provider: 'wxpay',
  ...data.channelExtras,  // timeStamp, nonceStr, package, signType, paySign
  success: () => { /* 支付成功跳转 */ },
  fail: () => { /* 处理失败 */ },
});
```
