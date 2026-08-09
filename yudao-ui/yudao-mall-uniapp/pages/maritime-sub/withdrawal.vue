<!-- 佣金提现 -->
<template>
  <s-layout title="佣金提现" navbar="inner">
    <!-- 申请表单 -->
    <view class="form-card ss-m-20 ss-p-30">
      <view class="form-label">提现金额（元）</view>
      <input
        v-model="state.amount"
        class="amount-input ss-m-t-16"
        type="digit"
        placeholder="请输入提现金额"
        @blur="onAmountBlur"
      />

      <view class="tax-preview ss-m-t-20" v-if="state.preview.taxAmount !== undefined">
        <view class="ss-flex ss-row-between ss-m-t-10">
          <text>预计代扣税额</text>
          <text>¥{{ toYuan(state.preview.taxAmount) }}</text>
        </view>
        <view class="ss-flex ss-row-between ss-m-t-10">
          <text>预计实际到账</text>
          <text class="net-amount">¥{{ toYuan(state.preview.netAmount) }}</text>
        </view>
        <view class="tax-desc ss-m-t-10" v-if="state.preview.taxDesc">{{ state.preview.taxDesc }}</view>
      </view>

      <button class="ss-reset-button submit-btn ss-m-t-30" @tap="submit">申请提现（微信零钱）</button>
    </view>

    <!-- 提现记录 -->
    <view class="ss-p-x-20">
      <view class="list-title ss-m-b-16">提现记录</view>
      <view v-for="item in state.list" :key="item.id" class="record-card ss-m-b-16 ss-p-24">
        <view class="ss-flex ss-row-between ss-col-center">
          <view class="record-amount">¥{{ toYuan(item.applyAmount) }}</view>
          <view class="record-status" :class="statusClass(item.applyStatus)">
            {{ statusText(item.applyStatus) }}
          </view>
        </view>
        <view class="record-meta ss-m-t-10">
          实际到账 ¥{{ toYuan(item.netAmount) }}（代扣税 ¥{{ toYuan(item.taxAmount) }}）
        </view>
        <view class="record-meta ss-m-t-6">
          {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM') }}
        </view>
        <view class="record-meta ss-m-t-6 fail-reason" v-if="item.failReason">
          {{ item.failReason }}
        </view>
      </view>
      <s-empty v-if="!state.loading && state.list.length === 0" text="暂无提现记录" icon="/static/data-empty.png" />
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import WithdrawalApi from '@/sheep/api/maritime/withdrawal';

  const STATUS_TEXT = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    SUCCESS: '已到账',
    FAILED: '失败',
    REJECTED: '已拒绝',
  };

  function statusText(status) {
    return STATUS_TEXT[status] || status;
  }

  function statusClass(status) {
    if (status === 'SUCCESS') return 'text-success';
    if (['FAILED', 'REJECTED'].includes(status)) return 'text-danger';
    return 'text-default';
  }

  function toYuan(amount) {
    return amount ? Number(amount).toFixed(2) : '0.00';
  }

  const state = reactive({
    loading: false,
    amount: '',
    preview: {},
    list: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
  });

  async function onAmountBlur() {
    const amount = Number(state.amount);
    if (!amount || amount <= 0) {
      state.preview = {};
      return;
    }
    const { code, data } = await WithdrawalApi.previewTax(amount);
    if (code === 0) state.preview = data;
  }

  async function submit() {
    const amount = Number(state.amount);
    if (!amount || amount <= 0) {
      sheep.$helper.toast('请输入正确的提现金额');
      return;
    }
    const { code, data } = await WithdrawalApi.applyWithdrawal({
      amount,
      channel: 'WX_BALANCE',
    });
    if (code !== 0) return;
    uni.showModal({
      title: '提现申请已提交',
      content: `${data.estimatedArrivalDesc || '预计1-3个工作日到账'}，实际到账约 ¥${toYuan(data.netAmount)}`,
      showCancel: false,
    });
    state.amount = '';
    state.preview = {};
    resetAndLoad();
  }

  async function getList() {
    state.loading = true;
    const { code, data } = await WithdrawalApi.getMyWithdrawals({
      pageNo: state.pageNo,
      pageSize: state.pageSize,
    });
    state.loading = false;
    if (code !== 0) return;
    state.list = concat(state.list, data.list);
    state.total = data.total;
  }

  function resetAndLoad() {
    state.list = [];
    state.pageNo = 1;
    state.total = 0;
    getList();
  }

  onLoad(() => {
    resetAndLoad();
  });

  onReachBottom(() => {
    if (state.list.length >= state.total) return;
    state.pageNo += 1;
    getList();
  });
</script>

<style lang="scss" scoped>
  .form-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .form-label {
    font-size: 26rpx;
    color: #666;
  }

  .amount-input {
    height: 80rpx;
    border-bottom: 1rpx solid #f0f0f0;
    font-size: 40rpx;
    font-weight: 600;
    color: #333;
  }

  .tax-preview {
    font-size: 24rpx;
    color: #666;
    background: #f8f8f8;
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
  }

  .net-amount {
    color: #f83528;
    font-weight: 600;
  }

  .tax-desc {
    font-size: 20rpx;
    color: #999;
  }

  .submit-btn {
    width: 100%;
    height: 84rpx;
    line-height: 84rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 42rpx;
    font-size: 28rpx;
  }

  .list-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #222;
  }

  .record-card {
    background: #fff;
    border-radius: 12rpx;
  }

  .record-amount {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .record-meta {
    font-size: 22rpx;
    color: #999;
  }

  .fail-reason {
    color: #f83528;
  }

  .text-success {
    font-size: 22rpx;
    color: #19be6b;
  }

  .text-danger {
    font-size: 22rpx;
    color: #f83528;
  }

  .text-default {
    font-size: 22rpx;
    color: #999;
  }
</style>
