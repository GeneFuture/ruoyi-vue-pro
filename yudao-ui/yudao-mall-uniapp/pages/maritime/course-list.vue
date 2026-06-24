<!-- 课程列表 -->
<template>
  <s-layout title="课程列表" navbar="inner">
    <!-- 搜索与筛选栏 -->
    <su-sticky>
      <view class="search-bar ss-flex ss-col-center ss-p-x-20 ss-p-y-16">
        <view class="search-input ss-flex-1 ss-flex ss-col-center ss-p-x-20">
          <text class="cicon-search" />
          <input
            v-model="state.query.location"
            class="ss-flex-1 ss-m-l-10"
            placeholder="搜索上课地点"
            confirm-type="search"
            @confirm="resetAndLoad"
          />
        </view>
        <view class="filter-btn ss-m-l-20" @tap="state.showFilter = true">筛选</view>
      </view>
    </su-sticky>

    <!-- 课程卡片列表 -->
    <view class="ss-p-x-20">
      <view
        v-for="item in state.pagination.list"
        :key="item.id"
        class="course-card ss-m-b-20"
        @tap="goDetail(item.id)"
      >
        <image class="course-cover" :src="item.coverImage" mode="aspectFill" />
        <view class="course-info">
          <view class="course-name ss-line-1">{{ item.name }}</view>
          <view class="ss-flex ss-col-center ss-m-t-10">
            <view class="tag">{{ item.certificateType }}</view>
            <text v-if="item.sessionStatus" class="status-text ss-m-l-10">
              {{ sessionStatusText(item.sessionStatus) }}
            </text>
          </view>
          <view class="course-meta ss-flex ss-col-center ss-m-t-10" v-if="item.nearestStartDate">
            <text class="cicon-fill-time" />
            <text class="ss-m-l-6">{{ item.nearestStartDate }} 开班</text>
            <text v-if="item.location" class="ss-m-l-20">{{ item.location }}</text>
          </view>
          <view class="course-price ss-m-t-10" v-if="item.minTuition">
            <text class="price-num">¥{{ formatPrice(item.minTuition) }}</text>
            <text v-if="item.maxTuition && item.maxTuition !== item.minTuition">
              ~{{ formatPrice(item.maxTuition) }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <s-empty
      v-if="!state.loading && state.pagination.list.length === 0"
      text="暂无符合条件的课程"
      icon="/static/data-empty.png"
    />

    <!-- 加载更多 -->
    <uni-load-more
      v-if="state.pagination.list.length > 0"
      :status="state.loadStatus"
      :content-text="{ contentdown: '上拉加载更多' }"
    />

    <!-- 筛选弹窗 -->
    <su-popup :show="state.showFilter" round="10" :showClose="true" @close="state.showFilter = false">
      <view class="filter-modal ss-p-30">
        <view class="filter-title ss-m-b-20">证书类型</view>
        <input
          v-model="state.query.certificateType"
          class="filter-input ss-m-b-30"
          placeholder="如 STCW"
        />
        <view class="filter-title ss-m-b-20">开班月份</view>
        <picker mode="date" fields="month" @change="onMonthChange">
          <view class="filter-input">{{ state.query.sessionMonth || '请选择月份' }}</view>
        </picker>
        <view class="filter-actions ss-flex ss-m-t-40">
          <button class="ss-reset-button reset-btn ss-flex-1" @tap="onResetFilter">重置</button>
          <button
            class="ss-reset-button confirm-btn ss-flex-1 ui-BG-Main-Gradient"
            @tap="onConfirmFilter"
          >
            确定
          </button>
        </view>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';

  const state = reactive({
    loading: false,
    loadStatus: '',
    showFilter: false,
    query: {
      certificateType: '',
      location: '',
      sessionMonth: '',
    },
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
  });

  const SESSION_STATUS_TEXT = {
    OPEN: '招生中',
    FULL: '名额已满',
    CLOSED: '已截止',
    COMPLETED: '已结班',
  };

  function sessionStatusText(status) {
    return SESSION_STATUS_TEXT[status] || status;
  }

  function formatPrice(amount) {
    return Number(amount).toFixed(0);
  }

  async function getList() {
    state.loadStatus = 'loading';
    state.loading = true;
    const { code, data } = await CourseApi.getCourseList({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      certificateType: state.query.certificateType || undefined,
      location: state.query.location || undefined,
      sessionMonth: state.query.sessionMonth || undefined,
    });
    state.loading = false;
    if (code !== 0) {
      state.loadStatus = 'noMore';
      return;
    }
    state.pagination.list = concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  function resetAndLoad() {
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 1;
    state.loadStatus = '';
    getList();
  }

  function onMonthChange(e) {
    state.query.sessionMonth = e.detail.value;
  }

  function onResetFilter() {
    state.query.certificateType = '';
    state.query.sessionMonth = '';
    state.showFilter = false;
    resetAndLoad();
  }

  function onConfirmFilter() {
    state.showFilter = false;
    resetAndLoad();
  }

  function goDetail(id) {
    sheep.$router.go('/pages/maritime/course-detail', { id });
  }

  onLoad((options) => {
    // 记录分享带入的推荐码，供报名时自动填入
    if (options.ref) {
      uni.setStorageSync('maritime_referral_code', options.ref);
    }
    resetAndLoad();
  });

  onReachBottom(() => {
    if (state.loadStatus === 'noMore') return;
    state.pagination.pageNo += 1;
    getList();
  });
</script>

<style lang="scss" scoped>
  .search-bar {
    background: #fff;
  }

  .search-input {
    height: 64rpx;
    background: #f5f5f5;
    border-radius: 32rpx;
    font-size: 26rpx;
    color: #999;
  }

  .filter-btn {
    font-size: 28rpx;
    color: #333;
    flex-shrink: 0;
  }

  .course-card {
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  .course-cover {
    width: 100%;
    height: 320rpx;
  }

  .course-info {
    padding: 20rpx 24rpx 24rpx;
  }

  .course-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }

  .tag {
    font-size: 22rpx;
    color: var(--ui-BG-Main);
    background: rgba(var(--ui-BG-Main-rgb, 255, 90, 95), 0.1);
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
  }

  .status-text {
    font-size: 22rpx;
    color: #999;
  }

  .course-meta {
    font-size: 24rpx;
    color: #999;
  }

  .course-price {
    font-size: 24rpx;
    color: #f83528;

    .price-num {
      font-size: 32rpx;
      font-weight: 600;
    }
  }

  .filter-title {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .filter-input {
    height: 80rpx;
    line-height: 80rpx;
    padding: 0 20rpx;
    background: #f5f5f5;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
  }

  .reset-btn {
    height: 80rpx;
    border-radius: 40rpx;
    border: 1rpx solid #dfdfdf;
    color: #595959;
    margin-right: 20rpx;
  }

  .confirm-btn {
    height: 80rpx;
    border-radius: 40rpx;
    color: #fff;
  }
</style>
