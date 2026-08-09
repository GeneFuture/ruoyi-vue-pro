<!-- 报名支付确认 -->
<template>
  <s-layout title="支付确认" navbar="inner">
    <view class="pay-box ss-flex-col ss-col-center ss-row-center">
      <view class="money-box ss-m-b-20">
        <text class="money-text">{{ state.amount }}</text>
      </view>
      <view class="desc-text">{{ payTypeText }}</view>

      <view class="wechat-pay-item ss-m-t-60 ss-flex ss-col-center ss-row-center">
        <image class="pay-icon" :src="sheep.$url.static('/static/img/shop/pay/wechat.png')" mode="aspectFit" />
        <text class="ss-m-l-10">微信支付</text>
      </view>
    </view>

    <su-fixed bottom placeholder>
      <view class="ss-p-x-30 ss-p-b-30">
        <button class="ss-reset-button submit-btn" :disabled="state.submitting" @tap="onPay">
          {{ state.submitting ? '处理中...' : '确认支付' }}
        </button>
      </view>
    </su-fixed>
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import MaritimeOrderApi from '@/sheep/api/maritime/order';

  const params = reactive({
    type: 'deposit', // deposit | balance
    orderId: '',
    enrollmentId: '',
  });

  const state = reactive({
    amount: '0.00',
    payOrderId: '',
    submitting: false,
  });

  const payTypeText = computed(() => (params.type === 'deposit' ? '报名定金' : '培训尾款'));

  async function createPayOrder() {
    const res =
      params.type === 'deposit'
        ? await MaritimeOrderApi.payDeposit(params.orderId)
        : await MaritimeOrderApi.payBalance(params.enrollmentId);
    if (res.code !== 0) return;
    state.payOrderId = res.data.payOrderId;
    state.amount = Number(res.data.amount).toFixed(2);
  }

  function onPay() {
    if (!state.payOrderId) return;
    state.submitting = true;
    sheep.$platform.pay('wechat', 'maritime', state.payOrderId);
    setTimeout(() => {
      state.submitting = false;
    }, 1500);
  }

  onLoad((options) => {
    params.type = options.type || 'deposit';
    params.orderId = options.orderId;
    params.enrollmentId = options.enrollmentId;
    createPayOrder();
  });
</script>

<style lang="scss" scoped>
  .pay-box {
    padding: 100rpx 0 40rpx;
  }

  .money-text {
    font-size: 60rpx;
    font-weight: 600;
    color: #f83528;

    &::before {
      content: '￥';
      font-size: 36rpx;
    }
  }

  .desc-text {
    font-size: 26rpx;
    color: #999;
  }

  .wechat-pay-item {
    height: 90rpx;
    width: 600rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  .pay-icon {
    width: 40rpx;
    height: 40rpx;
  }

  .submit-btn {
    width: 100%;
    height: 84rpx;
    line-height: 84rpx;
    border-radius: 42rpx;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #fff;
    font-size: 30rpx;
  }
</style>
