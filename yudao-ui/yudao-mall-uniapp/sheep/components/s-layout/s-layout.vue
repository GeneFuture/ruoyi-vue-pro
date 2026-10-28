<template>
  <view
    class="page-app"
    :class="['theme-' + sys?.mode, 'main-' + sys?.theme, 'font-' + sys?.fontSize]"
  >
    <view class="page-main" :style="[bgMain]">
      <!-- 顶部导航栏-情况1：默认通用顶部导航栏 -->
      <su-navbar
        v-if="navbar === 'normal'"
        :title="title"
        statusBar
        :color="color"
        :tools="tools"
        :opacityBgUi="opacityBgUi"
        :backgroundColor="navbarBackgroundColor"
      />

      <view class="page-body" :style="[bgBody]">
        <!-- 顶部导航栏-情况3：沉浸式头部
             navbarImmersive=true：首屏透明、白字，滚动后淡入白色导航背景
             navbarImmersive=false（默认）：白色导航栏、深色标题，并补一段等高的占位避免遮挡 -->
        <su-inner-navbar
          v-if="navbar === 'inner'"
          :title="title"
          bg="bg-none"
          :solid="!navbarImmersive || navbarSolid"
          :title-visible="navbarTitleVisible"
        />
        <view
          v-if="navbar === 'inner' && !navbarImmersive"
          :style="[{ paddingTop: sheep?.$platform?.navbar + 'px' }]"
        ></view>

        <!-- 页面内容插槽 -->
        <slot />
      </view>
    </view>

    <view class="page-modal">
      <!-- 全局授权弹窗 -->
      <s-auth-modal />
      <!-- 全局快捷入口 -->
      <s-menu-tools />
    </view>
  </view>
</template>

<script setup>
  /**
   * 模板组件 - 提供页面公共组件，属性，方法
   */
  import { computed, onMounted } from 'vue';
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  // #ifdef MP-WEIXIN
  import { onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app';
  // #endif

  const props = defineProps({
    title: {
      type: String,
      default: '',
    },
    navbar: {
      type: String,
      default: 'normal',
    },
    // 沉浸式头图：导航栏透明 + 白色标题，且不生成占位（首屏内容从屏幕顶部开始绘制）
    // 仅 navbar === 'inner' 时生效；适用于首页这类自带深色头图的页面
    navbarImmersive: {
      type: Boolean,
      default: false,
    },
    // 沉浸式导航滚动后是否切换为白色背景
    navbarSolid: {
      type: Boolean,
      default: false,
    },
    // 沉浸式首屏是否展示导航标题（首页可先隐藏，滚动后再出现）
    navbarTitleVisible: {
      type: Boolean,
      default: true,
    },
    opacityBgUi: {
      type: String,
      default: 'bg-white',
    },
    // 顶部导航栏背景颜色（仅在 navbar === 'normal' 时生效）
    navbarBackgroundColor: {
      type: String,
      default: '',
    },
    color: {
      type: String,
      default: '',
    },
    tools: {
      type: String,
      default: 'title',
    },
    bgStyle: {
      type: Object,
      default: () => ({
        src: '',
        color: 'var(--ui-BG-1)',
      }),
    },
    onShareAppMessage: {
      type: [Boolean, Object],
      default: true,
    },
  });

  const sysStore = sheep.$store('sys');
  const sys = computed(() => sysStore);

  // 导航栏模式(因为有自定义导航栏 需要计算)
  const navbarMode = computed(() => {
    return props.navbar === 'inner' ? 'inner' : 'normal';
  });

  // 背景1
  const bgMain = computed(() => {
    if (navbarMode.value === 'inner') {
      return {
        background: `${props.bgStyle.backgroundColor || props.bgStyle.color} url(${sheep.$url.cdn(
          props.bgStyle.backgroundImage,
        )}) no-repeat top center / 100% auto`,
      };
    }
    return {};
  });

  // 背景2
  const bgBody = computed(() => {
    if (navbarMode.value === 'normal') {
      return {
        background: `${props.bgStyle.backgroundColor || props.bgStyle.color} url(${sheep.$url.cdn(
          props.bgStyle.backgroundImage,
        )}) no-repeat top center / 100% auto`,
      };
    }
    return {};
  });

  // 分享信息
  const shareInfo = computed(() => {
    if (props.onShareAppMessage === true) {
      return sheep.$platform.share.getShareInfo();
    }
    if (!isEmpty(props.onShareAppMessage)) {
      return props.onShareAppMessage;
    }
    return {};
  });

  // #ifdef MP-WEIXIN
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
  });
  // 微信小程序分享好友
  onShareAppMessage(() => {
    return {
      title: shareInfo.value.title,
      path: shareInfo.value.forward.path,
      imageUrl: shareInfo.value.image,
    };
  });
  // 微信小程序分享朋友圈
  onShareTimeline(() => {
    // onShareTimeline 的 query 只接受查询字符串，不能传完整页面路径
    const path = shareInfo.value.forward?.path || '';
    const queryIndex = path.indexOf('?');
    return {
      title: shareInfo.value.title,
      query: queryIndex >= 0 ? path.slice(queryIndex + 1) : '',
      imageUrl: shareInfo.value.image,
    };
  });
  // #endif

  // 组件中使用 onMounted 监听页面加载，不是页面组件不使用 onShow
  onMounted(() => {
    // #ifdef MP-ALIPAY
    uni.setNavigationBarTitle({
      title: '',
    });
    // #endif
    if (!isEmpty(shareInfo.value)) {
      sheep.$platform.share.updateShareInfo(shareInfo.value);
    }
  });
</script>

<style lang="scss" scoped>
  .page-app {
    position: relative;
    color: var(--ui-TC);
    background-color: var(--ui-BG-1) !important;
    z-index: 2;
    display: flex;
    width: 100%;
    height: 100vh;

    .page-main {
      position: absolute;
      z-index: 1;
      width: 100%;
      min-height: 100%;
      display: flex;
      flex-direction: column;

      .page-body {
        width: 100%;
        position: relative;
        z-index: 1;
        flex: 1;
      }

      .page-img {
        width: 100vw;
        height: 100vh;
        position: absolute;
        top: 0;
        left: 0;
        z-index: 0;
      }
    }
  }
</style>
