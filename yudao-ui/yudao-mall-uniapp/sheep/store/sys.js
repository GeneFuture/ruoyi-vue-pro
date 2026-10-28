import { defineStore } from 'pinia';
import app from './app';

const sys = defineStore('sys', {
  state: () => ({
    theme: '', // 主题,
    mode: 'light', // 明亮模式、暗黑模式（暂未支持）
    modeAuto: false, // 跟随系统
    fontSize: 1, // 设置默认字号等级(0-4)
  }),
  getters: {},
  actions: {
    setTheme(theme = '') {
      if (theme === '') {
        // 兜底品牌色：后端装修模板（app.template.basic）当前恒为空，因此这里就是全站主题色的事实来源。
        // 'brand' 由 sheep/scss/_var.scss 的 $colors 提供，对应 --ui-BG-Main = #1e5eff
        this.theme = app().template?.basic.theme || 'brand';
      } else {
        this.theme = theme;
      }
    },
  },
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'sys-store',
      },
    ],
  },
});

export default sys;
