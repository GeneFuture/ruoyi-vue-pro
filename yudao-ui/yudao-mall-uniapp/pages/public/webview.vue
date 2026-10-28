<!-- 网页加载 -->
<template>
  <view>
    <web-view :src="url" />
  </view>
</template>

<script setup>
  import { onLoad } from '@dcloudio/uni-app';
  import { ref } from 'vue';

  const url = ref('');
  onLoad((options) => {
    const target = decodeURIComponent(options.url || '');
    // 正式环境只允许 HTTPS；本地开发允许 HTTP，方便联调
    const isHttps = /^https:\/\//i.test(target);
    const isDevHttp = process.env.NODE_ENV === 'development' && /^http:\/\//i.test(target);
    if (!isHttps && !isDevHttp) {
      uni.showToast({ title: '链接格式不正确', icon: 'none' });
      return;
    }
    url.value = target;
  });
</script>

<style lang="scss" scoped></style>
