<template>
  <s-layout title="消息中心" navbar="inner">
    <!-- Tab 栏 -->
    <su-sticky>
      <su-tabs
        :list="tabList"
        :current="state.currentTab"
        :scrollable="false"
        @change="onTabChange"
      />
    </su-sticky>

    <!-- 全部已读按钮 -->
    <view v-if="state.pagination.list.length > 0" class="ss-flex ss-row-right ss-p-x-20 ss-p-y-10">
      <view class="read-all-btn" @tap="handleReadAll">全部已读</view>
    </view>

    <!-- 消息列表 -->
    <view v-if="state.pagination.list.length > 0">
      <view
        v-for="item in state.pagination.list"
        :key="item.id"
        class="msg-item ss-flex ss-col-center"
        :class="{ unread: !item.readStatus }"
        @tap="handleMsgTap(item)"
      >
        <!-- 未读红点 -->
        <view class="dot-wrap">
          <view v-if="!item.readStatus" class="red-dot" />
        </view>

        <!-- 消息图标 -->
        <view class="msg-icon-wrap">
          <image class="msg-icon" :src="getMsgIcon(item.templateCode)" mode="aspectFit" />
        </view>

        <!-- 消息内容 -->
        <view class="msg-body ss-flex-1">
          <view class="msg-title ss-line-2">{{ item.templateContent }}</view>
          <view class="msg-time ss-m-t-6">
            {{ sheep.$helper.timeFormat(item.createTime, 'yyyy-mm-dd hh:MM') }}
          </view>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <s-empty v-if="!state.loading && state.pagination.list.length === 0" text="暂无消息" icon="/static/data-empty.png" />

    <!-- 加载更多 -->
    <uni-load-more
      v-if="state.pagination.list.length > 0"
      :status="state.loadStatus"
      :content-text="{ contentdown: '上拉加载更多' }"
    />
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom, onShow } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import NotifyApi from '@/sheep/api/maritime/notify';

  const tabList = [
    { name: '全部', value: '' },
    { name: '支付类', value: 'pay' },
    { name: '推荐类', value: 'referral' },
  ];

  // 模板编码前缀与 tab 的映射
  const TAB_CODE_PREFIXES = {
    pay: ['MARITIME_DEPOSIT_PAID', 'MARITIME_BALANCE_PAID', 'MARITIME_BALANCE_REMINDER', 'MARITIME_GROUPON'],
    referral: ['MARITIME_REFERRAL_SUCCESS', 'MARITIME_COMMISSION'],
  };

  const state = reactive({
    currentTab: 0,
    loading: false,
    loadStatus: '',
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
  });

  /** 加载消息列表 */
  async function getList() {
    state.loadStatus = 'loading';
    state.loading = true;
    const { code, data } = await NotifyApi.getNotifyMessagePage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
    });
    state.loading = false;
    if (code !== 0) {
      state.loadStatus = 'noMore';
      return;
    }
    // 客户端按 tab 过滤
    const tabValue = tabList[state.currentTab].value;
    const filtered = tabValue
      ? data.list.filter((msg) =>
          TAB_CODE_PREFIXES[tabValue].some((prefix) => msg.templateCode?.startsWith(prefix))
        )
      : data.list;

    state.pagination.list = concat(state.pagination.list, filtered);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  /** 重置并刷新 */
  function resetAndLoad() {
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 1;
    state.loadStatus = '';
    getList();
  }

  /** Tab 切换 */
  function onTabChange(index) {
    if (state.currentTab === index) return;
    state.currentTab = index;
    resetAndLoad();
  }

  /** 点击消息：标记已读并跳转 */
  async function handleMsgTap(item) {
    if (!item.readStatus) {
      await NotifyApi.updateReadStatus([item.id]);
      item.readStatus = true;
      refreshUnreadBadge();
    }
  }

  /** 全部已读 */
  async function handleReadAll() {
    await NotifyApi.updateAllReadStatus();
    state.pagination.list.forEach((msg) => (msg.readStatus = true));
    refreshUnreadBadge();
  }

  /** 刷新 TabBar 未读红点 */
  async function refreshUnreadBadge() {
    const { code, data } = await NotifyApi.getUnreadCount();
    if (code !== 0) return;
    const count = Number(data) || 0;
    // 消息中心入口在个人中心 tab (index 3)
    if (count > 0) {
      uni.setTabBarBadge({ index: 3, text: String(count > 99 ? '99+' : count) });
    } else {
      uni.removeTabBarBadge({ index: 3 });
    }
  }

  /** 返回消息图标（根据模板编码决定图标） */
  function getMsgIcon(templateCode) {
    if (!templateCode) return '/static/img/shop/notify/default.png';
    if (templateCode.includes('DEPOSIT') || templateCode.includes('BALANCE') || templateCode.includes('COMMISSION')) {
      return '/static/img/shop/notify/pay.png';
    }
    if (templateCode.includes('REFERRAL')) {
      return '/static/img/shop/notify/referral.png';
    }
    if (templateCode.includes('GROUPON')) {
      return '/static/img/shop/notify/groupon.png';
    }
    if (templateCode.includes('ENROLL')) {
      return '/static/img/shop/notify/enroll.png';
    }
    return '/static/img/shop/notify/default.png';
  }

  onLoad(() => {
    resetAndLoad();
  });

  onShow(() => {
    refreshUnreadBadge();
  });

  onReachBottom(() => {
    if (state.loadStatus === 'noMore') return;
    state.pagination.pageNo += 1;
    getList();
  });
</script>

<style lang="scss" scoped>
  .read-all-btn {
    font-size: 24rpx;
    color: #999;
    padding: 10rpx 20rpx;
  }

  .msg-item {
    padding: 30rpx 30rpx;
    border-bottom: 1rpx solid #f5f5f5;
    background: #fff;

    &.unread {
      background: #fafcff;
    }
  }

  .dot-wrap {
    width: 20rpx;
    margin-right: 10rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .red-dot {
    width: 14rpx;
    height: 14rpx;
    border-radius: 50%;
    background: #f83528;
  }

  .msg-icon-wrap {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: #f0f4ff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;
    flex-shrink: 0;
  }

  .msg-icon {
    width: 48rpx;
    height: 48rpx;
  }

  .msg-body {
    overflow: hidden;
  }

  .msg-title {
    font-size: 28rpx;
    color: #333;
    line-height: 1.5;
  }

  .msg-time {
    font-size: 22rpx;
    color: #aaa;
  }
</style>
