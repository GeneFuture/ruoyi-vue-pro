<!-- 推荐中心 -->
<template>
  <s-layout title="推荐中心" navbar="inner" :onShareAppMessage="shareInfo">
    <!-- 账户汇总卡片 -->
    <view class="account-card ss-p-30">
      <view class="ss-flex ss-row-between ss-col-center">
        <view>
          <view class="account-label">累计佣金（元）</view>
          <view class="account-amount">{{ toYuan(state.account.totalAmount) }}</view>
        </view>
        <button class="ss-reset-button withdraw-btn" @tap="goWithdrawal">提现</button>
      </view>
      <view class="ss-flex ss-row-between ss-m-t-30">
        <view class="sub-stat">
          <view class="sub-num">{{ toYuan(state.account.pendingAmount) }}</view>
          <view class="sub-label">待发放</view>
        </view>
        <view class="sub-stat">
          <view class="sub-num">{{ toYuan(state.account.paidAmount) }}</view>
          <view class="sub-label">已发放</view>
        </view>
        <view class="sub-stat">
          <view class="sub-num">{{ toYuan(state.account.frozenAmount) }}</view>
          <view class="sub-label">冻结中</view>
        </view>
        <view class="sub-stat">
          <view class="sub-num">{{ state.account.totalReferralCount || 0 }}</view>
          <view class="sub-label">推荐人数</view>
        </view>
      </view>
    </view>

    <!-- 推荐码与分享 -->
    <view class="invite-card ss-m-x-20 ss-m-t-20 ss-p-30 ss-flex ss-row-between ss-col-center">
      <view>
        <view class="invite-label">我的推荐码</view>
        <view class="invite-code">{{ state.info.referralCode || '-' }}</view>
        <view class="invite-tip ss-m-t-6" v-if="!state.info.hasReferralRight">
          支付定金后即可获得推荐资格
        </view>
      </view>
      <button class="ss-reset-button share-btn" open-type="share" :disabled="!state.info.hasReferralRight">
        分享邀请
      </button>
    </view>

    <!-- Tab 栏 -->
    <su-sticky class="ss-m-t-20">
      <su-tabs :list="tabList" :current="state.currentTab" :scrollable="false" @change="onTabChange" />
    </su-sticky>

    <!-- 推荐记录 -->
    <view v-if="state.currentTab === 0" class="ss-p-x-20 ss-p-t-20">
      <view v-for="item in state.referralList" :key="item.id" class="record-card ss-m-b-16 ss-p-24">
        <view class="ss-flex ss-row-between ss-col-center">
          <view class="record-title">{{ item.referredName }} · {{ item.sessionName }}</view>
          <view class="record-status" :class="item.relationStatus === 'ACTIVE' ? 'text-success' : 'text-default'">
            {{ item.relationStatus === 'ACTIVE' ? '有效' : '已失效' }}
          </view>
        </view>
        <view class="record-meta ss-m-t-10">
          {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM') }}
        </view>
        <view class="record-meta ss-m-t-6" v-if="item.commissionAmount">
          预计佣金 ¥{{ toYuan(item.commissionAmount) }}（{{ commissionStatusText(item.commissionStatus) }}）
        </view>
      </view>
      <s-empty v-if="!state.loading && state.referralList.length === 0" text="暂无推荐记录" icon="/static/data-empty.png" />
    </view>

    <!-- 佣金记录 -->
    <view v-if="state.currentTab === 1" class="ss-p-x-20 ss-p-t-20">
      <view v-for="item in state.commissionList" :key="item.id" class="record-card ss-m-b-16 ss-p-24">
        <view class="ss-flex ss-row-between ss-col-center">
          <view class="record-title">{{ item.sessionName }}</view>
          <view class="record-amount">¥{{ toYuan(item.commissionAmount) }}</view>
        </view>
        <view class="record-meta ss-m-t-10">{{ commissionStatusText(item.commissionStatus) }}</view>
        <view class="record-meta ss-m-t-6">
          触发时间 {{ sheep.$helper.timeFormat(item.triggerTime, 'yyyy-mm-dd hh:MM') }}
        </view>
        <view class="record-meta ss-m-t-6" v-if="item.expectedSettleDate">
          预计结算 {{ item.expectedSettleDate }}
        </view>
      </view>
      <s-empty v-if="!state.loading && state.commissionList.length === 0" text="暂无佣金记录" icon="/static/data-empty.png" />
    </view>

    <uni-load-more
      v-if="currentList.length > 0"
      :status="state.loadStatus"
      :content-text="{ contentdown: '上拉加载更多' }"
    />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import ReferralApi from '@/sheep/api/maritime/referral';

  const tabList = [
    { name: '推荐记录', value: 'referral' },
    { name: '佣金记录', value: 'commission' },
  ];

  const COMMISSION_STATUS_TEXT = {
    WAITING_FOR_CLASS: '待开课',
    PENDING_REVIEW: '待审核',
    PENDING_PAYOUT: '待发放',
    PAID: '已发放',
    FROZEN: '冻结中',
    REJECTED: '已拒绝',
  };

  function commissionStatusText(status) {
    return COMMISSION_STATUS_TEXT[status] || status;
  }

  const state = reactive({
    loading: false,
    loadStatus: '',
    currentTab: 0,
    info: {},
    account: {},
    referralList: [],
    commissionList: [],
    pageNo: 1,
    pageSize: 10,
    total: 0,
  });

  const currentList = computed(() =>
    state.currentTab === 0 ? state.referralList : state.commissionList,
  );

  function toYuan(amount) {
    return amount ? Number(amount).toFixed(2) : '0.00';
  }

  async function getOverview() {
    const [infoRes, accountRes] = await Promise.all([
      ReferralApi.getMyReferralInfo(),
      ReferralApi.getMyCommissionAccount(),
    ]);
    if (infoRes.code === 0) state.info = infoRes.data;
    if (accountRes.code === 0) state.account = accountRes.data;
  }

  async function getList() {
    state.loadStatus = 'loading';
    state.loading = true;
    const params = { pageNo: state.pageNo, pageSize: state.pageSize };
    const { code, data } =
      state.currentTab === 0
        ? await ReferralApi.getMyReferralList(params)
        : await ReferralApi.getMyCommissionRecords(params);
    state.loading = false;
    if (code !== 0) {
      state.loadStatus = 'noMore';
      return;
    }
    if (state.currentTab === 0) {
      state.referralList = concat(state.referralList, data.list);
    } else {
      state.commissionList = concat(state.commissionList, data.list);
    }
    state.total = data.total;
    state.loadStatus = currentList.value.length < state.total ? 'more' : 'noMore';
  }

  function resetAndLoad() {
    state.referralList = [];
    state.commissionList = [];
    state.pageNo = 1;
    state.total = 0;
    state.loadStatus = '';
    getList();
  }

  function onTabChange(index) {
    if (state.currentTab === index) return;
    state.currentTab = index;
    resetAndLoad();
  }

  function goWithdrawal() {
    sheep.$router.go('/pages/maritime-sub/withdrawal');
  }

  const shareInfo = computed(() => {
    return {
      title: '加入海员培训，享专业课程',
      forward: { path: `pages/maritime/course-list?ref=${state.info.referralCode || ''}` },
    };
  });

  onLoad(() => {
    getOverview();
    resetAndLoad();
  });

  onReachBottom(() => {
    if (state.loadStatus === 'noMore') return;
    state.pageNo += 1;
    getList();
  });
</script>

<style lang="scss" scoped>
  .account-card {
    background: linear-gradient(135deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #fff;
  }

  .account-label {
    font-size: 22rpx;
    opacity: 0.85;
  }

  .account-amount {
    font-size: 48rpx;
    font-weight: 600;
    margin-top: 6rpx;
  }

  .withdraw-btn {
    width: 160rpx;
    height: 64rpx;
    line-height: 64rpx;
    font-size: 26rpx;
    color: var(--ui-BG-Main);
    background: #fff;
    border-radius: 32rpx;
  }

  .sub-stat {
    text-align: center;
  }

  .sub-num {
    font-size: 28rpx;
    font-weight: 600;
  }

  .sub-label {
    font-size: 20rpx;
    opacity: 0.85;
    margin-top: 4rpx;
  }

  .invite-card {
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  }

  .invite-label {
    font-size: 22rpx;
    color: #999;
  }

  .invite-code {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    letter-spacing: 2rpx;
  }

  .invite-tip {
    font-size: 20rpx;
    color: #ff9900;
  }

  .share-btn {
    width: 180rpx;
    height: 68rpx;
    line-height: 68rpx;
    font-size: 26rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 34rpx;

    &[disabled] {
      opacity: 0.5;
    }
  }

  .record-card {
    background: #fff;
    border-radius: 12rpx;
  }

  .record-title {
    font-size: 26rpx;
    color: #333;
    font-weight: 500;
  }

  .record-amount {
    font-size: 28rpx;
    color: #f83528;
    font-weight: 600;
  }

  .record-meta {
    font-size: 22rpx;
    color: #999;
  }

  .text-success {
    font-size: 22rpx;
    color: #19be6b;
  }

  .text-default {
    font-size: 22rpx;
    color: #999;
  }
</style>
