import { defineStore } from 'pinia';

const modal = defineStore('modal', {
  state: () => ({
    auth: '', // 授权弹框 accountLogin|smsLogin|resetPassword|mpAuthorization
    menu: false, // 快捷菜单弹框
    lastTimer: {
      // 短信验证码计时器，为了防止刷新请求做了持久化
      smsLogin: 0,
      resetPassword: 0,
    }
  }),
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'modal-store',
        paths: ['lastTimer'],
      },
    ],
  },
});

export default modal;
