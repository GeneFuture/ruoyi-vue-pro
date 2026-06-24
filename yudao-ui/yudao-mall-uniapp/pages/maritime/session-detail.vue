<!-- 班期详情 -->
<template>
  <s-layout title="班期详情" navbar="inner">
    <view v-if="state.session.id">
      <!-- 基本信息 -->
      <view class="ss-p-30 bg-white">
        <view class="session-name">{{ state.session.sessionName || state.session.sessionCode }}</view>
        <view class="status-badge ss-m-t-16">{{ statusText }}</view>
        <view class="info-row ss-m-t-20">
          <text class="cicon-fill-time" />
          <text class="ss-m-l-10">
            {{ state.session.startDate }} ~ {{ state.session.endDate }}（{{
              state.session.durationDays
            }}天）
          </text>
        </view>
        <view class="info-row ss-m-t-10">
          <text class="cicon-fill-place" />
          <text class="ss-m-l-10">{{ state.session.location }}</text>
        </view>
        <view class="info-row ss-m-t-10" v-if="state.session.instructorInfo">
          <text class="cicon-friendfill" />
          <text class="ss-m-l-10">{{ state.session.instructorInfo }}</text>
        </view>
        <view class="info-row ss-m-t-10">
          <text class="cicon-fill-similar" />
          <text class="ss-m-l-10">
            剩余名额 {{ state.session.remainingCount }}/{{ state.session.maxStudents }}
          </text>
        </view>
        <view class="info-row ss-m-t-10" v-if="state.session.enrollmentDeadline">
          <text class="cicon-fill-warning" />
          <text class="ss-m-l-10">报名截止 {{ state.session.enrollmentDeadline }}</text>
        </view>
      </view>

      <!-- 费用信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-20">费用信息</view>
        <view class="fee-row ss-flex ss-row-between">
          <text>学费总额</text>
          <text class="fee-num">¥{{ toYuan(state.session.tuitionAmount) }}</text>
        </view>
        <view class="fee-row ss-flex ss-row-between ss-m-t-16">
          <text>报名定金</text>
          <text class="fee-num">¥{{ toYuan(state.session.depositAmount) }}</text>
        </view>
        <view
          class="fee-row ss-flex ss-row-between ss-m-t-16"
          v-if="state.session.isGrouponEnabled"
        >
          <text>拼团定金（{{ state.session.grouponRequiredCount }}人成团）</text>
          <text class="fee-num groupon-num">¥{{ toYuan(state.session.grouponDepositAmount) }}</text>
        </view>
        <view
          v-for="(value, key) in state.session.tuitionDescription"
          :key="key"
          class="fee-row ss-flex ss-row-between ss-m-t-16 sub-fee"
        >
          <text>{{ key }}</text>
          <text>{{ value }}</text>
        </view>
      </view>

      <!-- 进行中的拼团 -->
      <view
        class="section ss-m-t-20 bg-white ss-p-30"
        v-if="state.session.isGrouponEnabled && state.session.activeGroupons?.length"
      >
        <view class="section-title ss-m-b-20">正在拼团（可直接加入）</view>
        <view
          v-for="groupon in state.session.activeGroupons"
          :key="groupon.id"
          class="groupon-item ss-flex ss-col-center ss-row-between"
        >
          <view>
            <view class="groupon-progress">
              已有 {{ groupon.currentCount }}/{{ groupon.requiredCount }} 人，
              还差 {{ groupon.remainingCount }} 人成团
            </view>
            <view class="groupon-expire ss-m-t-6">
              {{ sheep.$helper.timeFormat(groupon.expireTime, 'mm-dd hh:MM') }} 截止
            </view>
          </view>
          <button class="ss-reset-button join-btn" @tap="goEnroll(true, groupon.inviteCode)">
            加入拼团
          </button>
        </view>
      </view>

      <!-- 退款政策 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.session.refundPolicyText">
        <view class="section-title ss-m-b-20">退款政策</view>
        <view class="section-text">{{ state.session.refundPolicyText }}</view>
      </view>

      <!-- 底部操作栏 -->
      <su-fixed bottom placeholder bg="bg-white">
        <view class="foot-box ss-flex ss-col-center ss-p-x-30 ss-p-y-16">
          <view class="ss-flex-1">
            <text class="foot-price">¥{{ toYuan(state.session.depositAmount) }}</text>
            <text class="foot-price-label">起 · 定金</text>
          </view>
          <button
            v-if="state.session.isGrouponEnabled"
            class="ss-reset-button groupon-create-btn"
            @tap="goEnroll(true)"
          >
            发起拼团
          </button>
          <button class="ss-reset-button enroll-btn" @tap="goEnroll(false)">立即报名</button>
        </view>
      </su-fixed>
    </view>

    <s-empty v-else-if="!state.loading" text="班期不存在" icon="/static/data-empty.png" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import SessionApi from '@/sheep/api/maritime/session';

  const state = reactive({
    loading: false,
    session: {},
  });

  const SESSION_STATUS_TEXT = {
    OPEN: '招生中',
    FULL: '名额已满',
    CLOSED: '已截止',
    COMPLETED: '已结班',
  };

  const statusText = computed(() => {
    return SESSION_STATUS_TEXT[state.session.sessionStatus] || state.session.sessionStatus;
  });

  function toYuan(amount) {
    return amount ? Number(amount).toFixed(2) : '0.00';
  }

  async function getDetail(id) {
    state.loading = true;
    const { code, data } = await SessionApi.getSessionDetail(id);
    state.loading = false;
    if (code !== 0) return;
    state.session = data;
  }

  function goEnroll(joinGroupon, inviteCode) {
    if (state.session.sessionStatus !== 'OPEN') {
      sheep.$helper.toast('当前班期不在招生状态');
      return;
    }
    const params = { sessionId: state.session.id };
    if (joinGroupon) {
      params.joinGroupon = 'true';
      if (inviteCode) params.grouponInviteCode = inviteCode;
    }
    sheep.$router.go('/pages/maritime/enrollment', params);
  }

  onLoad((options) => {
    if (options.id) {
      getDetail(options.id);
    }
  });
</script>

<style lang="scss" scoped>
  .session-name {
    font-size: 32rpx;
    font-weight: 600;
    color: #222;
  }

  .status-badge {
    display: inline-block;
    font-size: 22rpx;
    color: var(--ui-BG-Main);
    background: rgba(255, 90, 95, 0.1);
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
  }

  .info-row {
    font-size: 26rpx;
    color: #666;
  }

  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #222;
  }

  .section-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }

  .fee-row {
    font-size: 26rpx;
    color: #666;
  }

  .fee-num {
    color: #333;
    font-weight: 500;
  }

  .groupon-num {
    color: #ff9900;
  }

  .sub-fee {
    font-size: 24rpx;
    color: #999;
  }

  .groupon-item {
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }
  }

  .groupon-progress {
    font-size: 26rpx;
    color: #333;
  }

  .groupon-expire {
    font-size: 22rpx;
    color: #999;
  }

  .join-btn {
    width: 160rpx;
    height: 60rpx;
    line-height: 60rpx;
    font-size: 24rpx;
    color: #fff;
    background: #ff9900;
    border-radius: 30rpx;
  }

  .foot-box {
    .foot-price {
      font-size: 36rpx;
      font-weight: 600;
      color: #f83528;

      &::before {
        content: '';
      }
    }

    .foot-price-label {
      font-size: 22rpx;
      color: #999;
      margin-left: 6rpx;
    }
  }

  .groupon-create-btn {
    width: 200rpx;
    height: 76rpx;
    line-height: 76rpx;
    font-size: 26rpx;
    color: #fff;
    background: #ff9900;
    border-radius: 38rpx;
    margin-right: 16rpx;
  }

  .enroll-btn {
    width: 200rpx;
    height: 76rpx;
    line-height: 76rpx;
    font-size: 26rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 38rpx;
  }
</style>
