<!-- 消息中心（tab 页）
     与旧版 pages/maritime-sub/notify/index.vue 的差异：
     1. 移到主包 —— tabBar 页不能放在 subPackages 里
     2. tab 由旧版分类改为「全部/未读」—— 后端 my-page 只支持 readStatus 过滤，
        不支持 templateCode 过滤，旧版在客户端 filter 会和分页打架（loadStatus 永远停在 more）
     3. 补上未登录态（原生 tabBar 点击不经过 router，绕过了 meta.auth 拦截）
     4. 补上错误态与重试、下拉刷新、分页并发保护
     5. 图标改用 cicon 字体图标，替掉 5 个不存在的 /static/img/shop/notify/*.png -->
<template>
  <s-layout title="消息中心" navbar="inner">
    <!-- 未登录：原生 tabBar 可以直接点进来，必须自己兜住 -->
    <s-empty
      v-if="!isLogin"
      text="登录后查看消息"
      icon="/static/data-empty.png"
      showAction
      actionText="立即登录"
      @clickAction="onLogin"
    />

    <template v-else>
      <!-- Tab 栏 + 全部已读 -->
      <su-sticky>
        <view class="tab-row ss-flex ss-col-center ss-row-between">
          <view class="tab-list ss-flex">
            <view
              v-for="(tab, index) in tabList"
              :key="tab.value ?? 'all'"
              class="tab-item"
              :class="{ 'tab-item-active': state.currentTab === index }"
              @tap="onTabChange(index)"
            >
              {{ tab.name }}
            </view>
          </view>
          <view
            v-if="state.pagination.list.length > 0"
            class="read-all"
            @tap="handleReadAll"
          >
            全部已读
          </view>
        </view>
      </su-sticky>

      <!-- 加载失败 -->
      <s-empty
        v-if="state.loadError"
        text="消息加载失败，请检查网络"
        icon="/static/internet-empty.png"
        showAction
        actionText="重新加载"
        @clickAction="reload"
      />

      <template v-else>
        <!-- 消息列表 -->
        <view v-if="state.pagination.list.length > 0" class="msg-list">
          <view
            v-for="item in state.pagination.list"
            :key="item.id"
            class="msg-item ss-flex"
            :class="{ 'msg-item-unread': !item.readStatus }"
            @tap="handleMsgTap(item)"
          >
            <view class="msg-icon-wrap ss-flex ss-row-center ss-col-center">
              <text :class="getMsgIcon(item.templateCode)" class="msg-icon" />
            </view>

            <view class="msg-body ss-flex-1">
              <view class="ss-flex ss-col-center ss-row-between">
                <text class="msg-title ss-line-1 ss-flex-1">
                  {{ item.templateNickname || '系统通知' }}
                </text>
                <text class="msg-time">{{ timeText(item.createTime) }}</text>
              </view>
              <view class="msg-content ss-line-2">{{ item.templateContent }}</view>
            </view>
          </view>
        </view>

        <!-- 空态 -->
        <s-empty
          v-if="!state.loading && state.pagination.list.length === 0"
          :text="state.currentTab === 1 ? '没有未读消息' : '暂无消息'"
          icon="/static/data-empty.png"
        />

        <!-- 加载更多 -->
        <uni-load-more
          v-if="state.pagination.list.length > 0"
          :status="state.loadStatus"
          :content-text="{ contentdown: '上拉加载更多', contentrefresh: '正在加载...', contentnomore: '没有更多了' }"
        />
      </template>
    </template>

    <!-- 消息详情弹窗 -->
    <su-popup
      :show="state.showDetail"
      round="10"
      :showClose="true"
      @close="state.showDetail = false"
    >
      <view v-if="state.currentMsg" class="msg-detail ss-p-30">
        <view class="msg-detail-title">{{ state.currentMsg.templateNickname || '系统通知' }}</view>
        <view class="msg-detail-time">{{ detailTimeText(state.currentMsg.createTime) }}</view>
        <view class="msg-detail-content-wrap">
          <text class="msg-detail-content">{{ state.currentMsg.templateContent }}</text>
        </view>
        <button
          class="ss-reset-button msg-detail-close ss-m-t-30"
          @tap="state.showDetail = false"
        >
          知道了
        </button>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onShow, onUnload, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import { showAuthModal } from '@/sheep/hooks/useModal';
  import NotifyApi from '@/sheep/api/maritime/notify';

  /** 消息 tab 在 pages.json tabBar.list 中的下标，用于设置未读角标 */
  const TAB_INDEX_MESSAGE = 2;

  // 后端 my-page 仅支持 readStatus 过滤（不支持 templateCode），因此这里用「全部 / 未读」
  const tabList = [
    { name: '全部', value: undefined },
    { name: '未读', value: false },
  ];

  const state = reactive({
    currentTab: 0,
    loading: false,
    readingAll: false,
    loadError: false,
    loadStatus: '',
    showDetail: false,
    currentMsg: null,
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
  });

  const isLogin = computed(() => sheep.$store('user').isLogin);

  /** 消息图标：按模板编码归类，全部使用工程内已有的 cicon 字体图标 */
  function getMsgIcon(templateCode) {
    const code = templateCode || '';
    if (code.includes('DEPOSIT') || code.includes('BALANCE') || code.includes('PAY')) return 'cicon-wechat-pay';
    if (code.includes('REFUND')) return 'cicon-back';
    if (code.includes('GROUPON')) return 'cicon-group';
    if (code.includes('ENROLL')) return 'cicon-order';
    return 'cicon-notice';
  }

  function timeText(time) {
    if (!time) return '';
    return sheep.$helper.timeFormat(time, 'mm-dd hh:MM');
  }

  function detailTimeText(time) {
    if (!time) return '';
    return sheep.$helper.timeFormat(time, 'yyyy-mm-dd hh:MM');
  }

  let listRequestSeq = 0;

  async function getList({ reset = false } = {}) {
    // 翻页时不并发请求；切换 Tab 时允许打断上一页请求
    if (state.loading && !reset) return;
    const requestSeq = ++listRequestSeq;
    state.loading = true;
    state.loadStatus = 'loading';

    try {
      const pageNo = reset ? 1 : state.pagination.pageNo + 1;
      const { code, data } = await NotifyApi.getNotifyMessagePage({
        pageNo,
        pageSize: state.pagination.pageSize,
        // undefined 会被 request 层序列化时跳过，等于不带该筛选
        readStatus: tabList[state.currentTab].value,
        templateCodePrefix: 'MARITIME_',
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
      // 网络异常 / 登录态失效时 request 会 reject：必须兜住，否则 loading 永远为 true，页面空白
      state.loadError = true;
      state.loadStatus = 'more';
    } finally {
      if (requestSeq === listRequestSeq) {
        state.loading = false;
      }
    }
  }

  function reload() {
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 0;
    state.loadStatus = '';
    state.loadError = false;
    getList({ reset: true });
    refreshUnreadBadge();
  }

  function onTabChange(index) {
    if (state.currentTab === index) return;
    state.currentTab = index;
    reload();
  }

  /** 点击消息：展示详情，并标记已读 */
  function handleMsgTap(item) {
    state.currentMsg = item;
    state.showDetail = true;
    if (item.readStatus) return;

    item.readStatus = true;
    NotifyApi.updateReadStatus([item.id])
      .then(({ code }) => {
        if (code !== 0) {
          item.readStatus = false;
          return;
        }
        refreshUnreadBadge();
      })
      .catch(() => {
        item.readStatus = false;
      });
  }

  async function handleReadAll() {
    if (state.readingAll) return;
    const confirm = await new Promise((resolve) => {
      uni.showModal({
        title: '全部已读',
        content: '确定将所有消息标记为已读吗？',
        success: (res) => resolve(res.confirm),
        fail: () => resolve(false),
      });
    });
    if (!confirm) return;

    state.readingAll = true;
    try {
      const { code } = await NotifyApi.updateAllReadStatus();
      if (code !== 0) return;
      state.pagination.list.forEach((msg) => (msg.readStatus = true));
      // 「未读」tab 下全部已读后列表应为空
      if (state.currentTab === 1) reload();
    } catch (e) {
      // 请求层已提示，避免未处理 Promise
    } finally {
      state.readingAll = false;
      refreshUnreadBadge();
    }
  }

  /** 同步 tabBar 未读角标 */
  async function refreshUnreadBadge() {
    if (!isLogin.value) {
      uni.removeTabBarBadge({ index: TAB_INDEX_MESSAGE, fail: () => {} });
      return;
    }
    const { code, data } = await NotifyApi.getUnreadCount({
      templateCodePrefix: 'MARITIME_',
    }).catch(() => ({}));
    if (code !== 0) return;
    const count = Number(data) || 0;
    if (count > 0) {
      uni.setTabBarBadge({
        index: TAB_INDEX_MESSAGE,
        text: count > 99 ? '99+' : String(count),
        fail: () => {},
      });
    } else {
      uni.removeTabBarBadge({ index: TAB_INDEX_MESSAGE, fail: () => {} });
    }
  }

  function onLogin() {
    showAuthModal();
  }

  /** 授权弹窗登录成功后刷新列表（弹窗关闭不会触发页面 onShow） */
  function onAuthSuccess() {
    reload();
  }

  onLoad(() => {
    if (isLogin.value) reload();
    uni.$on('auth:success', onAuthSuccess);
  });

  onUnload(() => {
    uni.$off('auth:success', onAuthSuccess);
  });

  // tab 页：每次切回来都同步一次角标，列表数据保持不重置以免打断阅读
  onShow(() => {
    if (!isLogin.value) return;
    refreshUnreadBadge();
    if (state.pagination.list.length === 0) reload();
  });

  onPullDownRefresh(() => {
    if (!isLogin.value) {
      uni.stopPullDownRefresh();
      return;
    }
    Promise.resolve(reload()).finally(() => uni.stopPullDownRefresh());
  });

  onReachBottom(() => {
    if (state.loadStatus === 'noMore' || state.loading) return;
    getList();
  });
</script>

<style lang="scss" scoped>
  // ---- Tab 栏 ----
  .tab-row {
    background: #fff;
    padding: 0 24rpx;
  }

  .tab-item {
    position: relative;
    padding: 24rpx 0;
    margin-right: 48rpx;
    font-size: 28rpx;
    color: var(--ui-TC-2);

    &-active {
      color: var(--ui-TC);
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        left: 50%;
        bottom: 8rpx;
        transform: translateX(-50%);
        width: 40rpx;
        height: 6rpx;
        border-radius: 3rpx;
        background: var(--ui-BG-Main);
      }
    }
  }

  .read-all {
    // 保证 ≥88rpx(44pt) 的可点击高度，避免点击区域过小
    height: 88rpx;
    line-height: 88rpx;
    padding-left: 24rpx;
    font-size: 26rpx;
    color: var(--ui-TC-2);
  }

  // ---- 消息列表 ----
  .msg-list {
    background: #fff;
    margin-top: 16rpx;
  }

  .msg-item {
    padding: 28rpx 24rpx;
    align-items: flex-start;

    & + & {
      border-top: 1rpx solid #f2f4f7;
    }

    &-unread .msg-title {
      font-weight: 600;
      color: var(--ui-TC);
    }
  }

  .msg-icon-wrap {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: var(--ui-BG-Main-opacity-1);
    flex-shrink: 0;
    margin-right: 20rpx;
  }

  .msg-icon {
    font-size: 36rpx;
    color: var(--ui-BG-Main);
  }

  .msg-body {
    min-width: 0; // 让 ss-line-1/2 的省略号在 flex 子项里生效
  }

  .msg-title {
    font-size: 28rpx;
    color: var(--ui-TC-1);
  }

  .msg-time {
    font-size: 22rpx;
    color: var(--ui-TC-3);
    margin-left: 16rpx;
    flex-shrink: 0;
  }

  .msg-content {
    margin-top: 8rpx;
    font-size: 24rpx;
    color: var(--ui-TC-2);
    line-height: 1.5;
  }

  // ---- 消息详情弹窗 ----
  .msg-detail {
    background: #fff;
  }

  .msg-detail-title {
    padding-right: 48rpx;
    font-size: 32rpx;
    font-weight: 600;
    color: var(--ui-TC);
  }

  .msg-detail-time {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: var(--ui-TC-3);
  }

  .msg-detail-content-wrap {
    margin-top: 24rpx;
    max-height: 50vh;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }

  .msg-detail-content {
    font-size: 28rpx;
    line-height: 1.7;
    color: var(--ui-TC-2);
    white-space: pre-wrap;
    word-break: break-all;
  }

  .msg-detail-close {
    width: 100%;
    height: 80rpx;
    line-height: 80rpx;
    border-radius: 40rpx;
    background: var(--ui-BG-Main);
    color: #fff;
    font-size: 28rpx;
  }
</style>
