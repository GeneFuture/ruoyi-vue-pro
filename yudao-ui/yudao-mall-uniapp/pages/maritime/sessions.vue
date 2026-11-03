<!-- 开班（时间驱动的班期浏览：什么时候学） -->
<template>
  <s-layout title="开班计划" navbar="inner">
    <!-- 筛选栏 -->
    <su-sticky>
      <view class="filter-bar ss-flex ss-col-center ss-p-x-20 ss-p-y-16">
        <picker
          mode="date"
          fields="month"
          class="ss-flex-1"
          :value="state.query.month"
          @change="onMonthChange"
        >
          <view class="month-picker ss-flex ss-col-center ss-row-between ss-p-x-20">
            <text>{{ state.query.month || '全部月份' }}</text>
            <text class="cicon-arrow" />
          </view>
        </picker>
        <view class="location-input ss-flex ss-col-center ss-flex-1 ss-m-l-16 ss-p-x-20">
          <input
            v-model="state.query.city"
            class="ss-flex-1"
            placeholder="搜索城市"
            confirm-type="search"
            @confirm="resetAndLoad"
          />
          <text v-if="state.query.city" class="cicon-close" @tap="clearCity" />
        </view>
        <view class="cert-btn ss-m-l-16 ss-p-x-20 ss-p-y-10" @tap="state.showFilter = true">
          <text :class="state.query.certificateType ? 'cert-active' : ''">证书</text>
        </view>
      </view>
    </su-sticky>

    <!-- 班期列表 -->
    <view class="ss-p-20">
      <view
        v-for="item in state.pagination.list"
        :key="item.id"
        class="session-card ss-m-b-20"
        @tap="goSession(item.id)"
      >
        <!-- 课程信息头 -->
        <view class="ss-flex ss-row-between ss-col-center">
          <view class="ss-flex ss-col-center ss-flex-1">
            <text class="session-course ss-line-1">{{ item.courseName }}</text>
            <text class="session-tag ss-m-l-12" v-if="item.certificateType">
              {{ item.certificateType }}
            </text>
          </view>
          <text class="deadline-text" v-if="deadlineText(item)">
            {{ deadlineText(item) }}
          </text>
        </view>

        <!-- 班期信息 -->
        <view class="ss-flex ss-col-center ss-m-t-16">
          <text class="cicon-time session-icon" />
          <text class="session-meta ss-m-l-8">
            {{ item.startDate }} ~ {{ item.endDate }}（{{ item.durationDays }}天）
          </text>
        </view>
        <view class="ss-flex ss-col-center ss-m-t-10" v-if="item.city">
          <text class="cicon-place session-icon" />
          <text class="session-meta ss-m-l-8">{{ item.city }}</text>
        </view>

        <!-- 余位进度条 -->
        <view class="ss-flex ss-col-center ss-m-t-16">
          <view class="seat-progress ss-flex-1">
            <view
              class="seat-progress-inner"
              :style="{ width: seatPercent(item) + '%' }"
            />
          </view>
          <text class="seat-text ss-m-l-16">
            余位 {{ item.remainingCount }}/{{ item.maxStudents }}
          </text>
        </view>

        <!-- 底部：价格 + 拼团 -->
        <view class="ss-flex ss-row-between ss-col-center ss-m-t-20 card-footer">
          <view class="ss-flex ss-col-center" v-if="item.depositAmount">
            <text class="price-label">定金</text>
            <text class="price-num ss-m-l-8">¥{{ Number(item.depositAmount).toFixed(0) }}</text>
            <text class="groupon-badge ss-m-l-12" v-if="item.isGrouponEnabled">可拼团</text>
          </view>
          <view v-else />
          <view class="go-btn">查看班期</view>
        </view>
      </view>
    </view>

    <s-empty
      v-if="state.loadError && state.pagination.list.length === 0"
      text="开班信息加载失败，请检查网络"
      icon="/static/internet-empty.png"
      showAction
      actionText="重新加载"
      @clickAction="resetAndLoad"
    />

    <s-empty
      v-else-if="!state.loading && state.pagination.list.length === 0"
      text="暂无招生中的班期"
      icon="/static/data-empty.png"
    >
      <view class="empty-tip ss-m-t-20">可切换月份或地点，也可以联系客服登记需求</view>
    </s-empty>

    <uni-load-more
      v-if="state.pagination.list.length > 0"
      :status="state.loadStatus"
      :content-text="{ contentdown: '上拉加载更多' }"
    />

    <!-- 证书类型筛选弹窗 -->
    <su-popup :show="state.showFilter" round="10" :showClose="true" @close="state.showFilter = false">
      <view class="filter-modal ss-p-30">
        <view class="filter-title ss-m-b-20">证书类型</view>
        <input
          v-model="state.query.certificateType"
          class="filter-input ss-m-b-30"
          placeholder="如 STCW"
        />
        <view class="filter-actions ss-flex ss-m-t-20">
          <button class="ss-reset-button reset-btn ss-flex-1" @tap="onResetCert">重置</button>
          <button class="ss-reset-button confirm-btn ss-flex-1" @tap="onConfirmCert">确定</button>
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
  import SessionApi from '@/sheep/api/maritime/session';
  import { seatPercent } from '@/utils/maritime';

  const state = reactive({
    loading: false,
    loadStatus: '',
    loadError: false,
    showFilter: false,
    query: {
      month: '',
      city: '',
      certificateType: '',
    },
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
  });

  /** 报名截止紧迫提示（7 天内） */
  function deadlineText(item) {
    if (!item.enrollmentDeadline) return '';
    const days = Math.ceil(
      (new Date(item.enrollmentDeadline).getTime() - Date.now()) / (24 * 60 * 60 * 1000),
    );
    if (days < 0) return '';
    if (days === 0) return '今日截止';
    if (days <= 7) return `截止剩 ${days} 天`;
    return '';
  }

  let listRequestSeq = 0;

  async function getList({ reset = false } = {}) {
    if (state.loading && !reset) return;
    if (!reset && state.loadStatus === 'noMore') return;
    const requestSeq = ++listRequestSeq;
    state.loading = true;
    state.loadStatus = 'loading';
    try {
      const pageNo = reset ? 1 : state.pagination.pageNo + 1;
      const { code, data } = await SessionApi.getSessionPage({
        pageNo,
        pageSize: state.pagination.pageSize,
        month: state.query.month || undefined,
        city: state.query.city || undefined,
        certificateType: state.query.certificateType || undefined,
      });
      if (requestSeq !== listRequestSeq) return;
      if (code !== 0) {
        state.loadError = true;
        state.loadStatus = 'more';
        return;
      }
      state.loadError = false;
      const list = data?.list || [];
      state.pagination.pageNo = pageNo;
      state.pagination.list = reset ? list : concat(state.pagination.list, list);
      state.pagination.total = data?.total || 0;
      state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    } catch (e) {
      if (requestSeq !== listRequestSeq) return;
      // 网络异常时保留已有列表，只展示错误态，避免 loading 永久卡住
      state.loadError = true;
      state.loadStatus = 'more';
    } finally {
      if (requestSeq === listRequestSeq) {
        state.loading = false;
      }
    }
  }

  function resetAndLoad() {
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 0;
    state.loadStatus = '';
    state.loadError = false;
    getList({ reset: true });
  }

  function onMonthChange(e) {
    state.query.month = e.detail.value;
    resetAndLoad();
  }

  function clearCity() {
    state.query.city = '';
    resetAndLoad();
  }

  function onResetCert() {
    state.query.certificateType = '';
    state.showFilter = false;
    resetAndLoad();
  }

  function onConfirmCert() {
    state.showFilter = false;
    resetAndLoad();
  }

  function goSession(id) {
    sheep.$router.go('/pages/maritime-sub/session-detail', { id });
  }

  onLoad(() => {
    resetAndLoad();
  });

  onReachBottom(() => {
    if (state.loading || state.loadStatus === 'noMore') return;
    getList();
  });
</script>

<style lang="scss" scoped>
  $brand-blue: #1e5eff;

  .filter-bar {
    background: #fff;

    .month-picker {
      height: 64rpx;
      background: #f5f7fa;
      border-radius: 32rpx;
      font-size: 26rpx;
      color: #333;

      .cicon-arrow {
        font-size: 22rpx;
        color: #999;
      }
    }

    .location-input {
      height: 64rpx;
      background: #f5f7fa;
      border-radius: 32rpx;
      font-size: 26rpx;

      .cicon-close {
        color: #bbb;
        font-size: 24rpx;
      }
    }

    .cert-btn {
      background: #f5f7fa;
      border-radius: 32rpx;
      font-size: 26rpx;
      color: #333;
      flex-shrink: 0;

      .cert-active {
        color: $brand-blue;
        font-weight: 600;
      }
    }
  }

  .session-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;

    .session-course {
      font-size: 30rpx;
      font-weight: 600;
      color: #222;
      max-width: 420rpx;
    }

    .session-tag {
      font-size: 20rpx;
      color: $brand-blue;
      background: rgba(30, 94, 255, 0.08);
      border-radius: 6rpx;
      padding: 4rpx 12rpx;
      flex-shrink: 0;
    }

    .deadline-text {
      font-size: 22rpx;
      color: #f83528;
      background: rgba(248, 53, 40, 0.06);
      border-radius: 6rpx;
      padding: 4rpx 12rpx;
      flex-shrink: 0;
    }

    .session-icon {
      color: #bbb;
      font-size: 26rpx;
    }

    .session-meta {
      font-size: 24rpx;
      color: #666;
    }

    .seat-progress {
      height: 10rpx;
      background: #eef1f5;
      border-radius: 5rpx;
      overflow: hidden;

      .seat-progress-inner {
        height: 100%;
        background: linear-gradient(90deg, $brand-blue, #6c9bff);
        border-radius: 5rpx;
      }
    }

    .seat-text {
      font-size: 22rpx;
      color: #999;
      flex-shrink: 0;
    }

    .card-footer {
      border-top: 1rpx solid #f2f4f7;
      padding-top: 20rpx;

      .price-label {
        font-size: 22rpx;
        color: #999;
      }

      .price-num {
        font-size: 32rpx;
        font-weight: 700;
        color: $brand-blue;
      }

      .groupon-badge {
        font-size: 20rpx;
        color: #ff9900;
        border: 1rpx solid #ff9900;
        border-radius: 6rpx;
        padding: 2rpx 8rpx;
      }

      .go-btn {
        font-size: 24rpx;
        color: #fff;
        background: linear-gradient(90deg, $brand-blue, #0b3b8c);
        border-radius: 28rpx;
        padding: 10rpx 28rpx;
      }
    }
  }

  .empty-tip {
    font-size: 24rpx;
    color: #999;
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
    background: #f5f7fa;
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
    background: linear-gradient(90deg, $brand-blue, #0b3b8c);
  }
</style>
