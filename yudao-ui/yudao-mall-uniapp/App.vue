<script setup>
  import { onLaunch, onShow, onError } from '@dcloudio/uni-app';
  import { ShoproInit } from './sheep';

  onLaunch(() => {
    // 底部导航使用 pages.json 的原生 tabBar（首页/开班/消息/我的）
    // 注意：不要再调用 uni.hideTabBar()，否则会导致小程序没有底部导航
    // （自定义的 s-tabbar 数据源 app.template.basic.tabbar 由后端装修模板下发，当前为空）

    // 加载Shopro底层依赖
    ShoproInit();
  });

  onShow(() => {
    // #ifdef APP-PLUS
    // 获取urlSchemes参数
    const args = plus.runtime.arguments;
    if (args) {
    }

    // 获取剪贴板
    uni.getClipboardData({
      success: (res) => {},
    });
    // #endif
  });
</script>

<style lang="scss">
  @import '@/sheep/scss/index.scss';
</style>
