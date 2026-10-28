<!-- 报名详情 -->
<template>
  <s-layout title="报名详情" navbar="inner" :onShareAppMessage="shareInfo">
    <view v-if="state.detail.id">
      <!-- 状态卡片 -->
      <view class="status-card ss-p-30">
        <view class="status-text">{{ statusText }}</view>
        <view class="enroll-no ss-m-t-10">报名单号 {{ state.detail.enrollmentNo }}</view>
      </view>

      <!-- 人工处理提示 -->
      <view v-if="isManualProcessing" class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">人工处理</view>
        <view class="info-row ss-flex ss-row-between">
          <text>处理类型</text>
          <text>{{ MANUAL_TASK_TYPE_TEXT[state.detail.manualTaskType] || '其他' }}</text>
        </view>
        <view class="info-row ss-m-t-12">
          <text>{{ state.detail.manualTaskReason || '客服正在处理，请耐心等待' }}</text>
        </view>
      </view>

      <!-- 班期信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">班期信息</view>
        <view class="row">{{ state.detail.sessionName || state.detail.sessionCode }}</view>
        <view class="row sub-row ss-m-t-10">{{ sessionMetaText }}</view>
      </view>

      <!-- 报名人信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">报名人信息</view>
        <view class="info-row ss-flex ss-row-between"
          ><text>姓名</text><text>{{ state.detail.realName }}</text></view
        >
        <view class="info-row ss-flex ss-row-between ss-m-t-12"
          ><text>身份证号</text><text>{{ state.detail.idCardMasked }}</text></view
        >
        <view class="info-row ss-flex ss-row-between ss-m-t-12"
          ><text>手机号</text><text>{{ state.detail.phoneMasked }}</text></view
        >
      </view>

      <!-- 费用信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">费用信息</view>
        <view class="info-row ss-flex ss-row-between"
          ><text>学费总额</text><text>¥{{ toYuan(state.detail.totalAmount) }}</text></view
        >
        <template v-if="state.detail.fullPayment">
          <view class="info-row ss-flex ss-row-between ss-m-t-12">
            <text>报名方式</text><text>全款报名</text>
          </view>
        </template>
        <template v-else>
          <view class="info-row ss-flex ss-row-between ss-m-t-12"
            ><text>定金</text><text>¥{{ toYuan(state.detail.depositAmountSnapshot) }}</text></view
          >
          <view class="info-row ss-flex ss-row-between ss-m-t-12"
            ><text>尾款</text><text>¥{{ toYuan(state.detail.balanceAmount) }}</text></view
          >
          <view
            class="info-row ss-flex ss-row-between ss-m-t-12"
            v-if="state.detail.grouponDiscountAmount"
          >
            <text>拼团优惠</text><text>-¥{{ toYuan(state.detail.grouponDiscountAmount) }}</text>
          </view>
        </template>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"
          ><text>已支付</text><text>¥{{ toYuan(state.detail.paidAmount) }}</text></view
        >
        <view
          class="info-row ss-flex ss-row-between ss-m-t-12"
          v-if="!state.detail.fullPayment && state.detail.balanceDueDate"
        >
          <text>尾款截止</text><text>{{ state.detail.balanceDueDate }}</text>
        </view>
      </view>

      <!-- 订单列表 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.detail.orders?.length">
        <view class="section-title ss-m-b-16">支付订单</view>
        <view
          v-for="order in state.detail.orders"
          :key="order.id"
          class="order-row"
        >
          <view class="order-head ss-flex ss-row-between ss-col-center">
            <view class="order-type">{{ orderTypeText(order.orderType) }}</view>
            <view class="order-head-right ss-flex ss-col-center">
              <text class="order-amount">¥{{ toYuan(order.amount) }}</text>
              <text class="order-status">{{ orderStatusText(order.orderStatus) }}</text>
            </view>
          </view>
          <view class="order-sub ss-flex ss-row-between ss-col-center">
            <text class="order-no ss-flex-1 ss-line-1">{{ order.orderNo }}</text>
            <text class="order-time">{{ orderTimeText(order) }}</text>
          </view>
        </view>
      </view>

      <!-- 拼团入口 -->
      <view
        class="section ss-m-t-20 bg-white ss-p-30 ss-flex ss-row-between ss-col-center"
        @tap="goGroupon"
      >
        <text class="section-title">拼团状态</text>
        <text class="cicon-forward" />
      </view>

      <!-- 退费状态 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.refundStatus">
        <view class="section-title ss-m-b-16">退费申请</view>
        <view class="info-row ss-flex ss-row-between">
          <text>状态</text><text>{{ refundStatusText(state.refundStatus.applyStatus) }}</text>
        </view>
        <view
          class="info-row ss-flex ss-row-between ss-m-t-12"
          v-if="state.refundStatus.refundAmount"
        >
          <text>退费金额</text><text>¥{{ toYuan(state.refundStatus.refundAmount) }}</text>
        </view>
        <view
          class="info-row ss-flex ss-row-between ss-m-t-12"
          v-if="state.refundStatus.adminRemark"
        >
          <text>备注</text><text>{{ state.refundStatus.adminRemark }}</text>
        </view>
      </view>

      <view class="ss-m-t-40" />

      <!-- 操作栏：已付定金后展示 申请退费 / 拼团优惠 / 支付尾款 -->
      <su-fixed v-if="showFooter" bottom placeholder bg="bg-white">
        <view class="foot-box ss-flex ss-col-center ss-p-x-30 ss-p-y-16">
          <template v-if="state.detail.enrollmentStatus === 'PENDING_DEPOSIT'">
            <button class="ss-reset-button cancel-btn foot-btn" @tap="onCancel">取消报名</button>
            <button
              class="ss-reset-button main-btn foot-btn"
              @tap="goPay(state.detail.fullPayment ? 'full' : 'deposit')"
            >
              {{ state.detail.fullPayment ? '去支付全款' : '去支付定金' }}
            </button>
          </template>
          <template v-else>
            <button
              v-if="canApply"
              class="ss-reset-button plain-btn foot-btn"
              @tap="state.showRefundModal = true"
            >
              申请退费
            </button>
            <!-- 参与拼团且拼团进行中：展示拼团优惠 -->
            <button
              v-if="showGrouponShare"
              class="ss-reset-button plain-btn foot-btn"
              @tap="openGrouponModal"
            >
              拼团优惠
            </button>
            <!-- 人工处理中：查看处理进度；尾款逾期场景仍可补缴 -->
            <button
              v-if="isManualProcessing"
              class="ss-reset-button plain-btn foot-btn"
              @tap="showManualInfo"
            >
              处理进度
            </button>
            <button
              v-if="canPayBalance"
              class="ss-reset-button main-btn foot-btn"
              @tap="onPayBalance"
            >
              支付尾款
            </button>
          </template>
        </view>
      </su-fixed>
    </view>

    <!-- 退费申请弹窗 -->
    <su-popup
      :show="state.showRefundModal"
      round="10"
      :showClose="true"
      @close="state.showRefundModal = false"
    >
      <view class="modal-box ss-p-30">
        <view class="modal-title ss-m-b-20">申请退费</view>
        <uni-easyinput
          v-model="state.refundReason"
          type="textarea"
          maxlength="200"
          placeholder="请填写退费原因"
        />
        <button class="ss-reset-button confirm-btn ss-m-t-30" @tap="submitRefund">提交申请</button>
      </view>
    </su-popup>

    <!-- 拼团优惠弹窗 -->
    <su-popup
      :show="state.showGrouponModal"
      round="10"
      :showClose="true"
      @close="state.showGrouponModal = false"
    >
      <view class="modal-box ss-p-30">
        <view class="modal-title ss-m-b-20">拼团优惠</view>
        <view v-if="state.groupon" class="groupon-modal">
          <view class="groupon-stat">
            还差
            <text class="groupon-highlight">{{ state.groupon.remainingCount }}</text> 人成团（{{
              state.groupon.currentCount
            }}/{{ state.groupon.requiredCount }}）
          </view>
          <view v-if="grouponDiscount > 0" class="groupon-discount">
            成团后尾款立减 ¥{{ toYuan(grouponDiscount) }}
          </view>
          <view class="groupon-code-row">
            <text class="groupon-code-label">邀请码</text>
            <text class="groupon-code">{{ state.groupon.inviteCode }}</text>
            <text class="groupon-copy" @tap="copyInviteCode">复制</text>
          </view>
          <view class="groupon-hint">分享给好友，或复制拼团文案发到朋友圈</view>
          <button
            class="ss-reset-button confirm-btn ss-m-t-20"
            open-type="share"
            @tap="state.showGrouponModal = false"
          >
            分享给好友
          </button>
          <button
            class="ss-reset-button plain-btn ss-m-t-12 groupon-copy-btn"
            @tap="copyGrouponText"
          >
            复制拼团信息（可发朋友圈）
          </button>
        </view>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import { h5Url } from '@/sheep/config';
  import EnrollmentApi from '@/sheep/api/maritime/enrollment';
  import GrouponApi from '@/sheep/api/maritime/groupon';
  import SessionApi from '@/sheep/api/maritime/session';

  const state = reactive({
    detail: {},
    session: {},
    groupon: null,
    refundStatus: null,
    showRefundModal: false,
    showGrouponModal: false,
    refundReason: '',
  });

  const STATUS_TEXT = {
    PENDING_DEPOSIT: '待付定金',
    DEPOSITED: '已付定金',
    PAID_OFF: '已付清/待开班',
    MANUAL_PROCESSING: '人工处理',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  };

  const MANUAL_TASK_TYPE_TEXT = {
    BALANCE_OVERDUE: '尾款逾期',
    REFUND_APPLY: '退费申请',
    REFUND_AMOUNT_CONFIRM: '退款金额确认',
    OTHER: '其他',
  };

  const ORDER_TYPE_TEXT = { DEPOSIT: '定金', BALANCE: '尾款', FULL: '全款' };
  const ORDER_STATUS_TEXT = {
    PENDING: '待支付',
    PAID: '已支付',
    CLOSED: '已关闭',
    REFUNDED: '已退款',
  };
  const REFUND_STATUS_TEXT = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    REFUNDED: '已退款',
  };

  const statusText = computed(() => {
    if (state.detail.fullPayment) {
      if (state.detail.enrollmentStatus === 'PENDING_DEPOSIT') return '待付全款';
      if (state.detail.enrollmentStatus === 'PAID_OFF') return '已付全款/待开班';
    }
    return STATUS_TEXT[state.detail.enrollmentStatus] || state.detail.enrollmentStatus;
  });
  const isManualProcessing = computed(() => state.detail.enrollmentStatus === 'MANUAL_PROCESSING');
  const canApply = computed(() =>
    ['DEPOSITED', 'PAID_OFF'].includes(state.detail.enrollmentStatus),
  );

  /** 已付定金且有尾款时展示「支付尾款」；人工处理中仅尾款逾期场景允许补缴 */
  const canPayBalance = computed(() => {
    const status = state.detail.enrollmentStatus;
    if (state.detail.fullPayment || Number(state.detail.balanceAmount || 0) <= 0) {
      return false;
    }
    if (status === 'DEPOSITED') return true;
    return status === 'MANUAL_PROCESSING' && state.detail.manualTaskType === 'BALANCE_OVERDUE';
  });

  /** 参与拼团且拼团进行中时，展示「拼团优惠」 */
  const showGrouponShare = computed(
    () =>
      state.groupon?.grouponStatus === 'IN_PROGRESS' &&
      state.detail.enrollmentStatus === 'DEPOSITED',
  );

  /** 有操作按钮时才渲染底部栏 */
  const showFooter = computed(
    () =>
      state.detail.enrollmentStatus === 'PENDING_DEPOSIT' ||
      canApply.value ||
      canPayBalance.value ||
      isManualProcessing.value,
  );

  /** 拼团优惠金额：拼团进行中尚未抵扣，取班期配置；成团后详情接口会返回已抵扣金额 */
  const grouponDiscount = computed(() =>
    Number(state.session.grouponDiscountAmount ?? state.detail.grouponDiscountAmount ?? 0),
  );

  /** 分享卡片：拼团中指向班级详情页（带邀请码），由详情页报名时自动带入邀请码 */
  const shareInfo = computed(() => {
    const d = state.detail;
    const g = state.groupon;
    if (g?.grouponStatus === 'IN_PROGRESS') {
      return {
        title: `一起拼团报名「${d.sessionName || d.sessionCode || '海员培训'}」，还差 ${
          g.remainingCount
        } 人`,
        forward: {
          path: `pages/maritime-sub/session-detail?id=${d.sessionId}&grouponInviteCode=${g.inviteCode}`,
        },
      };
    }
    return {
      title: d.sessionName || d.sessionCode || '海员培训报名',
      forward: {
        path: `pages/maritime-sub/session-detail?id=${d.sessionId}`,
      },
    };
  });

  /** 班期日期：日期段 + 城市，城市为空时只留日期段，不留悬空的分隔符 */
  const sessionMetaText = computed(() => {
    const d = state.detail;
    const dateRange = `${d.startDate} ~ ${d.endDate}`;
    return d.city ? `${dateRange} · ${d.city}` : dateRange;
  });

  function toYuan(amount) {
    return amount ? Number(amount).toFixed(2) : '0.00';
  }

  function orderTypeText(type) {
    return ORDER_TYPE_TEXT[type] || type;
  }

  function orderStatusText(status) {
    return ORDER_STATUS_TEXT[status] || status;
  }

  /** 订单时间：已支付显示支付时间，待支付显示支付截止时间 */
  function orderTimeText(order) {
    const time = order.payTime || order.expireTime;
    return time ? sheep.$helper.timeFormat(time, 'yyyy-mm-dd hh:MM') : '';
  }

  function refundStatusText(status) {
    return REFUND_STATUS_TEXT[status] || status;
  }

  async function getDetail(id) {
    try {
      const { code, data } = await EnrollmentApi.getEnrollment(id);
      if (code !== 0) return;
      state.detail = data;

      // 静默加载拼团状态与班期费用配置：详情页需要展示拼团优惠、支付尾款入口
      const [grouponRes, sessionRes] = await Promise.all([
        GrouponApi.getMyGrouponStatus(id, true).catch(() => null),
        data.sessionId
          ? SessionApi.getSessionDetail(data.sessionId, {
              showLoading: false,
              showError: false,
            }).catch(() => null)
          : null,
      ]);
      if (grouponRes?.code === 0 && grouponRes.data) {
        state.groupon = grouponRes.data;
      }
      if (sessionRes?.code === 0 && sessionRes.data) {
        state.session = sessionRes.data;
      }

      if (data.enrollmentStatus === 'MANUAL_PROCESSING' && data.manualTaskType === 'REFUND_APPLY') {
        const res = await EnrollmentApi.getRefundStatus(id);
        if (res.code === 0) state.refundStatus = res.data;
      }
    } catch (e) {
      // 请求层已经提示；这里兜住异常，避免页面出现未处理 Promise
    }
  }

  function goPay(type) {
    if (type === 'deposit' || type === 'full') {
      const orderType = type === 'full' ? 'FULL' : 'DEPOSIT';
      const pendingOrder = state.detail.orders?.find(
        (o) => o.orderType === orderType && o.orderStatus === 'PENDING',
      );
      sheep.$router.go('/pages/maritime-sub/enrollment-pay', {
        orderId: pendingOrder?.id,
        enrollmentId: state.detail.id,
        type,
      });
      return;
    }
    sheep.$router.go('/pages/maritime-sub/enrollment-pay', {
      enrollmentId: state.detail.id,
      type: 'balance',
    });
  }

  function goGroupon() {
    sheep.$router.go('/pages/maritime-sub/groupon', {
      enrollmentId: state.detail.id,
      sessionId: state.detail.sessionId,
    });
  }

  /** 打开拼团优惠弹窗；首次加载失败时重试一次 */
  async function openGrouponModal() {
    if (!state.groupon) {
      const res = await GrouponApi.getMyGrouponStatus(state.detail.id, true).catch(() => null);
      if (res?.code === 0 && res.data) {
        state.groupon = res.data;
      }
    }
    if (!state.groupon) {
      sheep.$helper.toast('暂未获取到拼团信息');
      return;
    }
    state.showGrouponModal = true;
  }

  function copyInviteCode() {
    if (state.groupon?.inviteCode) {
      sheep.$helper.copyText(state.groupon.inviteCode);
    }
  }

  /** 复制可直接发朋友圈 / 群聊的拼团文案 */
  function copyGrouponText() {
    const d = state.detail;
    const g = state.groupon;
    if (!g) return;
    // 班级详情 H5 链接：邀请码写入链接，打开后报名会自动带入
    const base = String(h5Url || '').replace(/\/+$/, '');
    const detailLink = `${base}/pages/maritime-sub/session-detail?id=${d.sessionId}&grouponInviteCode=${g.inviteCode}`;
    const lines = [
      '【海员培训拼团优惠】',
      `班级：${d.sessionName || d.sessionCode || ''}`,
      d.startDate ? `开班：${d.startDate} ~ ${d.endDate || ''}` : '',
      `还差 ${g.remainingCount} 人成团（${g.currentCount}/${g.requiredCount}）`,
      grouponDiscount.value > 0 ? `成团后尾款立减 ¥${toYuan(grouponDiscount.value)}` : '',
      `邀请码：${g.inviteCode}`,
      `班级详情：${detailLink}`,
      '打开链接报名，邀请码会自动带入',
    ].filter(Boolean);
    const text = lines.join('\n');
    // setClipboardData 会由微信原生提示「内容已复制」，这里不再叠加 toast / modal
    uni.setClipboardData({
      data: text,
      success: () => {
        state.showGrouponModal = false;
      },
      fail: () => sheep.$helper.toast('复制失败，请重试'),
    });
  }

  /** 支付尾款：拼团未成团时先提醒，避免提前支付后无法享受拼团优惠 */
  function onPayBalance() {
    if (state.groupon?.grouponStatus === 'IN_PROGRESS') {
      uni.showModal({
        title: '拼团进行中',
        content: `拼团还未成团，现在支付尾款将按原价 ¥${toYuan(
          state.detail.balanceAmount,
        )} 支付，成团后的优惠无法再抵扣。建议成团后再支付尾款。`,
        confirmText: '继续支付',
        cancelText: '再等等',
        success: (res) => {
          if (res.confirm) {
            goPay('balance');
          }
        },
      });
      return;
    }
    goPay('balance');
  }

  function showManualInfo() {
    const typeText = MANUAL_TASK_TYPE_TEXT[state.detail.manualTaskType] || '其他';
    uni.showModal({
      title: '人工处理中',
      content: `${typeText}：${state.detail.manualTaskReason || '客服正在处理，请耐心等待'}`,
      showCancel: false,
      confirmText: '知道了',
    });
  }

  async function onCancel() {
    uni.showModal({
      title: '确认取消',
      content: '取消后定金订单将关闭，确定取消报名吗？',
      success: async (res) => {
        if (!res.confirm) return;
        const { code } = await EnrollmentApi.cancelEnrollment(state.detail.id);
        if (code === 0) getDetail(state.detail.id);
      },
    });
  }

  async function submitRefund() {
    if (!state.refundReason) {
      sheep.$helper.toast('请填写退费原因');
      return;
    }
    const { code } = await EnrollmentApi.applyRefund({
      enrollmentId: state.detail.id,
      applyReason: state.refundReason,
    });
    if (code === 0) {
      state.showRefundModal = false;
      getDetail(state.detail.id);
    }
  }

  onLoad((options) => {
    if (options.id) {
      getDetail(options.id);
    }
  });
</script>

<style lang="scss" scoped>
  .status-card {
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #fff;
  }

  .status-text {
    font-size: 32rpx;
    font-weight: 600;
  }

  .enroll-no {
    font-size: 22rpx;
    opacity: 0.85;
  }

  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #222;
  }

  .row {
    font-size: 28rpx;
    color: #333;
  }

  .sub-row {
    font-size: 24rpx;
    color: #999;
  }

  .info-row {
    font-size: 26rpx;
    color: #666;
  }

  .order-row {
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }
  }

  .order-type {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .order-head-right {
    flex-shrink: 0;
  }

  .order-sub {
    margin-top: 10rpx;
  }

  .order-no {
    min-width: 0;
    font-size: 22rpx;
    color: #999;
  }

  .order-time {
    margin-left: 16rpx;
    flex-shrink: 0;
    font-size: 22rpx;
    color: #999;
  }

  .order-amount {
    font-size: 30rpx;
    color: #f83528;
    font-weight: 600;
  }

  .order-status {
    margin-left: 16rpx;
    font-size: 24rpx;
    color: #999;
  }

  .foot-box {
    gap: 16rpx;

    button {
      margin-left: 0;
    }
  }

  .foot-btn {
    flex: 1;
    padding: 0 8rpx;
    white-space: nowrap;
  }

  .main-btn {
    height: 72rpx;
    line-height: 72rpx;
    padding: 0 30rpx;
    font-size: 26rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 36rpx;
  }

  .cancel-btn,
  .plain-btn {
    height: 72rpx;
    line-height: 70rpx;
    padding: 0 24rpx;
    font-size: 26rpx;
    color: #595959;
    border: 1rpx solid #dfdfdf;
    border-radius: 36rpx;
  }

  .modal-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }

  .confirm-btn {
    height: 76rpx;
    line-height: 76rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 38rpx;
  }

  // ---- 拼团优惠弹窗 ----
  .groupon-stat {
    font-size: 28rpx;
    color: #333;
  }

  .groupon-highlight {
    font-weight: 600;
    color: #ff9900;
  }

  .groupon-discount {
    margin-top: 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #f83528;
  }

  .groupon-code-row {
    display: flex;
    align-items: center;
    margin-top: 24rpx;
    padding: 20rpx;
    background: #f7f8fa;
    border-radius: 12rpx;
  }

  .groupon-code-label {
    font-size: 26rpx;
    color: #999;
  }

  .groupon-code {
    flex: 1;
    margin-left: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    letter-spacing: 2rpx;
    color: #222;
  }

  .groupon-copy {
    font-size: 26rpx;
    color: var(--ui-BG-Main);
  }

  .groupon-hint {
    margin-top: 20rpx;
    font-size: 24rpx;
    color: #999;
  }

  .groupon-copy-btn {
    width: 100%;
  }
</style>
