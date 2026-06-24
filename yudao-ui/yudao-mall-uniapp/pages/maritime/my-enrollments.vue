<!-- 我的报名 -->
<template>
  <s-layout title="我的报名" navbar="inner">
    <!-- 快捷入口 -->
    <view class="quick-nav ss-flex ss-m-20">
      <view class="nav-item ss-flex-1" @tap="sheep.$router.go('/pages/maritime/course-list')">
        <text class="cicon-fill-similar nav-icon" />
        <text class="ss-m-l-10">课程列表</text>
      </view>
      <view class="nav-item ss-flex-1" @tap="sheep.$router.go('/pages/maritime/referral')">
        <text class="cicon-fill-friendfill nav-icon" />
        <text class="ss-m-l-10">推荐中心</text>
      </view>
    </view>

    <view class="ss-p-x-20 ss-p-t-20">
      <view
        v-for="item in state.list"
        :key="item.id"
        class="enroll-card ss-m-b-20 ss-p-30"
        @tap="goDetail(item.id)"
      >
        <view class="ss-flex ss-row-between ss-col-center">
          <view class="session-name ss-line-1">{{ item.sessionName || item.sessionCode }}</view>
          <view class="status-tag" :class="statusClass(item.enrollmentStatus)">
            {{ statusText(item.enrollmentStatus) }}
          </view>
        </view>
        <view class="meta-row ss-m-t-16" v-if="item.startDate">
          {{ item.startDate }} 开班 · {{ item.location }}
        </view>
        <view class="meta-row ss-m-t-10">报名单号 {{ item.enrollmentNo }}</view>
        <view class="ss-flex ss-row-between ss-col-center ss-m-t-16">
          <text class="time-text">{{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM') }}</text>
          <text class="deposit-num">定金 ¥{{ Number(item.depositAmountSnapshot || 0).toFixed(2) }}</text>
        </view>
      </view>
    </view>

    <s-empty
      v-if="!state.loading && state.list.length === 0"
      text="暂无报名记录"
      icon="/static/data-empty.png"
    >
      <button class="ss-reset-button go-course-btn" @tap="sheep.$router.go('/pages/maritime/course-list')">
        去看看课程
      </button>
    </s-empty>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import EnrollmentApi from '@/sheep/api/maritime/enrollment';

  const state = reactive({
    loading: false,
    list: [],
  });

  const STATUS_TEXT = {
    PENDING_DEPOSIT: '待付定金',
    DEPOSITED: '已付定金',
    PENDING_BALANCE: '待付尾款',
    IN_PROGRESS: '上课中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDING: '退款中',
  };

  function statusText(status) {
    return STATUS_TEXT[status] || status;
  }

  function statusClass(status) {
    if (['PENDING_DEPOSIT', 'PENDING_BALANCE'].includes(status)) return 'tag-warning';
    if (['CANCELLED', 'REFUNDING'].includes(status)) return 'tag-default';
    if (status === 'COMPLETED') return 'tag-success';
    return 'tag-primary';
  }

  async function getList() {
    state.loading = true;
    const { code, data } = await EnrollmentApi.getMyEnrollments();
    state.loading = false;
    if (code !== 0) return;
    state.list = data;
    uni.stopPullDownRefresh();
  }

  function goDetail(id) {
    sheep.$router.go('/pages/maritime/enrollment-detail', { id });
  }

  onLoad(() => {
    getList();
  });

  onPullDownRefresh(() => {
    getList();
  });
</script>

<style lang="scss" scoped>
  .quick-nav {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 0;
  }

  .nav-item {
    justify-content: center;
    align-items: center;
    font-size: 26rpx;
    color: #333;
  }

  .nav-icon {
    color: var(--ui-BG-Main);
    font-size: 32rpx;
  }

  .enroll-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .session-name {
    font-size: 28rpx;
    font-weight: 600;
    color: #222;
    max-width: 480rpx;
  }

  .status-tag {
    font-size: 22rpx;
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
    flex-shrink: 0;
  }

  .tag-warning {
    color: #ff9900;
    background: rgba(255, 153, 0, 0.1);
  }

  .tag-primary {
    color: var(--ui-BG-Main);
    background: rgba(255, 90, 95, 0.1);
  }

  .tag-success {
    color: #19be6b;
    background: rgba(25, 190, 107, 0.1);
  }

  .tag-default {
    color: #999;
    background: #f5f5f5;
  }

  .meta-row {
    font-size: 24rpx;
    color: #999;
  }

  .time-text {
    font-size: 22rpx;
    color: #bbb;
  }

  .deposit-num {
    font-size: 26rpx;
    color: #f83528;
    font-weight: 500;
  }

  .go-course-btn {
    margin-top: 30rpx;
    width: 300rpx;
    height: 76rpx;
    line-height: 76rpx;
    font-size: 26rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 38rpx;
  }
</style>
