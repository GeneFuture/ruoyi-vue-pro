<!-- 个人中心 -->
<template>
  <s-layout title="个人中心" navbar="inner" onShareAppMessage>
    <!-- 用户信息卡片 -->
    <view class="user-card ss-p-30 ss-flex ss-col-center">
      <image class="avatar-img ss-m-r-20" :src="avatarUrl" mode="aspectFill" />
      <view v-if="isLogin" class="ss-flex-1">
        <view class="nickname">{{ userInfo.nickname || '未设置昵称' }}</view>
      </view>
      <view v-else class="ss-flex-1" @tap="onLogin">
        <view class="nickname">点击登录</view>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="menu-card ss-m-t-20">
      <view class="menu-item ss-flex ss-row-between ss-col-center" @tap="goEnrollments">
        <view class="menu-title">我的报名</view>
        <text class="cicon-forward" />
      </view>
      <view
        class="menu-item ss-flex ss-row-between ss-col-center"
        :class="{ 'menu-item-disabled': !state.hasReferralRight }"
        @tap="goReferral"
      >
        <view class="menu-title">我的推荐</view>
        <view class="ss-flex ss-col-center">
          <text v-if="!state.hasReferralRight" class="menu-tip ss-m-r-10">支付定金后开放</text>
          <text class="cicon-forward" />
        </view>
      </view>
      <view class="menu-item ss-flex ss-row-between ss-col-center" @tap="goNotify">
        <view class="menu-title">消息中心</view>
        <view class="ss-flex ss-col-center">
          <text v-if="state.unreadCount > 0" class="menu-badge ss-m-r-10">{{ state.unreadCount }}</text>
          <text class="cicon-forward" />
        </view>
      </view>
      <view class="menu-item ss-flex ss-row-between ss-col-center" @tap="goSetting">
        <view class="menu-title">系统设置</view>
        <text class="cicon-forward" />
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import ReferralApi from '@/sheep/api/maritime/referral';
  import NotifyApi from '@/sheep/api/maritime/notify';

  const state = reactive({
    hasReferralRight: false,
    unreadCount: 0,
  });

  const isLogin = computed(() => sheep.$store('user').isLogin);
  const userInfo = computed(() => sheep.$store('user').userInfo);

  // 微信小程序只允许加载 HTTPS 图片，头像异常（空值、非法协议）时回退到本地占位图
  const avatarUrl = computed(() => {
    const url = sheep.$url.cdn(userInfo.value.avatar);
    return url && url.indexOf('https://') === 0 ? url : '/static/data-empty.png';
  });

  function onLogin() {
    showAuthModal();
  }

  async function loadUserAssets() {
    if (!isLogin.value) {
      state.hasReferralRight = false;
      state.unreadCount = 0;
      return;
    }
    const [referralRes, unreadRes] = await Promise.all([
      ReferralApi.getMyReferralInfo(),
      NotifyApi.getUnreadCount(),
    ]);
    if (referralRes.code === 0) {
      state.hasReferralRight = !!referralRes.data.hasReferralRight;
    }
    if (unreadRes.code === 0) {
      state.unreadCount = unreadRes.data || 0;
    }
  }

  function goEnrollments() {
    sheep.$router.go('/pages/maritime/my-enrollments');
  }

  function goReferral() {
    sheep.$router.go('/pages/maritime/referral');
  }

  function goNotify() {
    sheep.$router.go('/pages/maritime-sub/notify/index');
  }

  function goSetting() {
    sheep.$router.go('/pages/public/setting');
  }

  onLoad(() => {
    loadUserAssets();
  });

  onShow(() => {
    loadUserAssets();
  });
</script>

<style lang="scss" scoped>
  .user-card {
    background: linear-gradient(135deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
  }

  .avatar-img {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    background: #fff;
  }

  .nickname {
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;
  }

  .menu-card {
    background: #fff;
  }

  .menu-item {
    height: 100rpx;
    padding: 0 30rpx;
    border-bottom: 2rpx solid #f5f5f5;
  }

  .menu-item-disabled {
    opacity: 0.6;
  }

  .menu-title {
    font-size: 28rpx;
    color: #333;
  }

  .menu-tip {
    font-size: 22rpx;
    color: #999;
  }

  .menu-badge {
    min-width: 32rpx;
    height: 32rpx;
    line-height: 32rpx;
    padding: 0 8rpx;
    border-radius: 16rpx;
    background: #f83528;
    color: #fff;
    font-size: 20rpx;
    text-align: center;
  }

  .cicon-forward {
    font-size: 26rpx;
    color: #c4c4c4;
  }
</style>
