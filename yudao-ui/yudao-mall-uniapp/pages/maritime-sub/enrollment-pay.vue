<!-- 报名支付确认 -->
<template>
  <s-layout title="支付确认" navbar="inner">
    <view class="pay-box ss-flex-col ss-col-center ss-row-center">
      <view class="money-box ss-m-b-20">
        <text class="money-text">{{ state.amount }}</text>
      </view>
      <view class="desc-text">{{ payTypeText }}</view>

      <view v-if="state.error" class="error-box ss-m-t-30">
        <text class="error-text">{{ state.error }}</text>
        <button class="ss-reset-button retry-btn ss-m-t-20" @tap="createPayOrder">重新获取支付单</button>
      </view>

      <view v-if="!state.error" class="wechat-pay-item ss-m-t-60 ss-flex ss-col-center ss-row-center">
        <text class="cicon-wechat-pay pay-icon" />
        <text class="ss-m-l-10">{{ payMethodText }}</text>
      </view>
    </view>

    <su-fixed bottom placeholder>
      <view class="ss-p-x-30 ss-p-b-30">
        <button
          class="ss-reset-button submit-btn"
          :disabled="state.submitting || !state.payOrderId"
          @tap="onPay"
        >
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
    type: 'deposit', // deposit | full | balance
    orderId: '',
    enrollmentId: '',
  });

  const state = reactive({
    amount: '0.00',
    payOrderId: '',
    submitting: false,
    error: '',
  });

  const payTypeText = computed(() => {
    if (params.type === 'balance') return '培训尾款';
    return params.type === 'full' ? '报名全款' : '报名定金';
  });

  /** 开发环境走 mock 渠道，避免依赖微信商户号 */
  const payMethodText = computed(() =>
    process.env.NODE_ENV === 'development' ? '模拟支付（开发环境）' : '微信支付',
  );

  async function createPayOrder() {
    state.error = '';
    state.payOrderId = '';
    try {
      // 定金与全款都走同一笔报名订单的支付；尾款单独接口
      const res =
        params.type === 'balance'
          ? await MaritimeOrderApi.payBalance(params.enrollmentId)
          : await MaritimeOrderApi.payDeposit(params.orderId);
      if (res.code !== 0) {
        // 请求层已提示具体原因，这里保留可重试入口，避免按钮点了没反应
        state.error = res.msg || '创建支付单失败，请重试';
        return;
      }
      state.payOrderId = res.data.payOrderId;
      state.amount = Number(res.data.amount).toFixed(2);
    } catch (e) {
      state.error = '创建支付单失败，请检查网络后重试';
    }
  }

  function onPay() {
    // 防止重复点击导致重复拉起支付
    if (state.submitting || !state.payOrderId) return;
    state.submitting = true;
    // 开发环境没有微信商户号时用 mock 渠道模拟支付；真机微信支付测试时改为 'wechat'
    const payMethod = process.env.NODE_ENV === 'development' ? 'mock' : 'wechat';
    sheep.$platform.pay(payMethod, 'maritime', state.payOrderId);
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

  .error-box {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 40rpx;
  }

  .error-text {
    font-size: 26rpx;
    color: #f83528;
    text-align: center;
  }

  .retry-btn {
    padding: 0 32rpx;
    height: 64rpx;
    line-height: 64rpx;
    border: 1rpx solid var(--ui-BG-Main);
    border-radius: 32rpx;
    font-size: 26rpx;
    color: var(--ui-BG-Main);
  }

  .wechat-pay-item {
    height: 90rpx;
    width: 600rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  .pay-icon {
    font-size: 40rpx;
    color: #09bb07;
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
