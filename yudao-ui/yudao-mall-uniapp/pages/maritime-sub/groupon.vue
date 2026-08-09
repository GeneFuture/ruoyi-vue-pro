<!-- 拼团状态 -->
<template>
  <s-layout title="拼团详情" navbar="inner" :onShareAppMessage="shareInfo">
    <view v-if="state.groupon" class="groupon-box ss-p-30">
      <view class="status-banner ss-flex-col ss-col-center ss-row-center">
        <view class="status-icon-text">{{ statusText }}</view>
        <view class="progress-text ss-m-t-16">
          已有 {{ state.groupon.currentCount }}/{{ state.groupon.requiredCount }} 人
        </view>
        <view class="progress-bar ss-m-t-20">
          <view class="progress-inner" :style="{ width: progressPercent + '%' }" />
        </view>
        <view
          v-if="state.groupon.grouponStatus === 'IN_PROGRESS'"
          class="countdown ss-m-t-16"
        >
          还差 {{ state.groupon.remainingCount }} 人，剩余 {{ countdownText }}
        </view>
        <view v-else-if="state.groupon.grouponStatus === 'SUCCESS'" class="countdown ss-m-t-16">
          拼团成功，成团时间 {{ sheep.$helper.timeFormat(state.groupon.successTime, 'yyyy-mm-dd hh:MM') }}
        </view>
        <view v-else class="countdown ss-m-t-16">拼团已降级为单独报名</view>
      </view>

      <view
        class="invite-card ss-m-t-30 ss-flex ss-col-center ss-row-between"
        v-if="state.groupon.grouponStatus === 'IN_PROGRESS'"
      >
        <view>
          <view class="invite-label">邀请码</view>
          <view class="invite-code">{{ state.groupon.inviteCode }}</view>
        </view>
        <button class="ss-reset-button copy-btn" @tap="copyInviteCode">复制</button>
      </view>

      <button
        v-if="state.groupon.grouponStatus === 'IN_PROGRESS'"
        class="ss-reset-button share-btn ss-m-t-30"
        open-type="share"
      >
        邀请好友拼团
      </button>
    </view>

    <s-empty
      v-else-if="!state.loading"
      text="本次报名未参与拼团"
      icon="/static/data-empty.png"
    />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onUnload } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import GrouponApi from '@/sheep/api/maritime/groupon';

  const params = reactive({ enrollmentId: '', sessionId: '' });

  const state = reactive({
    loading: false,
    groupon: null,
    now: Date.now(),
  });

  let timer = null;

  const STATUS_TEXT = {
    IN_PROGRESS: '拼团中',
    SUCCESS: '拼团成功',
    DEGRADED: '已降级',
  };

  const statusText = computed(() => STATUS_TEXT[state.groupon?.grouponStatus] || '');

  const progressPercent = computed(() => {
    if (!state.groupon) return 0;
    return Math.min(100, (state.groupon.currentCount / state.groupon.requiredCount) * 100);
  });

  const countdownText = computed(() => {
    if (!state.groupon?.expireTime) return '';
    const expire = new Date(state.groupon.expireTime.replace(/-/g, '/')).getTime();
    const diff = expire - state.now;
    if (diff <= 0) return '已截止';
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    return `${h}时${m}分${s}秒`;
  });

  async function getStatus() {
    state.loading = true;
    const res = await GrouponApi.getMyGrouponStatus(params.enrollmentId).catch(() => null);
    state.loading = false;
    if (!res || res.code !== 0 || !res.data) return;
    state.groupon = res.data;
  }

  function copyInviteCode() {
    sheep.$helper.copyText(state.groupon.inviteCode);
  }

  const shareInfo = computed(() => {
    return {
      title: '一起拼团报名吧，立省一笔！',
      forward: {
        path: `pages/maritime-sub/enrollment?sessionId=${params.sessionId}&joinGroupon=true&grouponInviteCode=${state.groupon?.inviteCode || ''}`,
      },
    };
  });

  onLoad((options) => {
    params.enrollmentId = options.enrollmentId;
    params.sessionId = options.sessionId || '';
    getStatus();
    timer = setInterval(() => {
      state.now = Date.now();
    }, 1000);
  });

  onUnload(() => {
    if (timer) clearInterval(timer);
  });
</script>

<style lang="scss" scoped>
  .status-banner {
    background: linear-gradient(135deg, #ff9900, #ffb84d);
    border-radius: 16rpx;
    padding: 40rpx 20rpx;
    color: #fff;
  }

  .status-icon-text {
    font-size: 32rpx;
    font-weight: 600;
  }

  .progress-text {
    font-size: 26rpx;
  }

  .progress-bar {
    width: 80%;
    height: 16rpx;
    background: rgba(255, 255, 255, 0.4);
    border-radius: 8rpx;
    overflow: hidden;
  }

  .progress-inner {
    height: 100%;
    background: #fff;
    border-radius: 8rpx;
    transition: width 0.3s;
  }

  .countdown {
    font-size: 24rpx;
  }

  .invite-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 30rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  .invite-label {
    font-size: 22rpx;
    color: #999;
  }

  .invite-code {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    letter-spacing: 2rpx;
  }

  .copy-btn {
    width: 140rpx;
    height: 60rpx;
    line-height: 60rpx;
    font-size: 24rpx;
    color: var(--ui-BG-Main);
    border: 1rpx solid var(--ui-BG-Main);
    border-radius: 30rpx;
  }

  .share-btn {
    width: 100%;
    height: 84rpx;
    line-height: 84rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 42rpx;
    font-size: 28rpx;
  }
</style>
