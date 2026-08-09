<!-- 报名详情 -->
<template>
  <s-layout title="报名详情" navbar="inner">
    <view v-if="state.detail.id">
      <!-- 状态卡片 -->
      <view class="status-card ss-p-30">
        <view class="status-text">{{ statusText }}</view>
        <view class="enroll-no ss-m-t-10">报名单号 {{ state.detail.enrollmentNo }}</view>
      </view>

      <!-- 班期信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">班期信息</view>
        <view class="row">{{ state.detail.sessionName || state.detail.sessionCode }}</view>
        <view class="row sub-row ss-m-t-10">
          {{ state.detail.startDate }} ~ {{ state.detail.endDate }} · {{ state.detail.location }}
        </view>
      </view>

      <!-- 报名人信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">报名人信息</view>
        <view class="info-row ss-flex ss-row-between"><text>姓名</text><text>{{ state.detail.realName }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"><text>身份证号</text><text>{{ state.detail.idCardMasked }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"><text>手机号</text><text>{{ state.detail.phoneMasked }}</text></view>
      </view>

      <!-- 费用信息 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-16">费用信息</view>
        <view class="info-row ss-flex ss-row-between"><text>学费总额</text><text>¥{{ toYuan(state.detail.totalAmount) }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"><text>定金</text><text>¥{{ toYuan(state.detail.depositAmountSnapshot) }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"><text>尾款</text><text>¥{{ toYuan(state.detail.balanceAmount) }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12"><text>已支付</text><text>¥{{ toYuan(state.detail.paidAmount) }}</text></view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12" v-if="state.detail.balanceDueDate">
          <text>尾款截止</text><text>{{ state.detail.balanceDueDate }}</text>
        </view>
      </view>

      <!-- 订单列表 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.detail.orders?.length">
        <view class="section-title ss-m-b-16">支付订单</view>
        <view
          v-for="order in state.detail.orders"
          :key="order.id"
          class="order-row ss-flex ss-row-between ss-col-center"
        >
          <view>
            <view class="order-type">{{ orderTypeText(order.orderType) }}</view>
            <view class="order-no">{{ order.orderNo }}</view>
          </view>
          <view class="ss-flex-col ss-row-right">
            <text class="order-amount">¥{{ toYuan(order.amount) }}</text>
            <text class="order-status">{{ orderStatusText(order.orderStatus) }}</text>
          </view>
        </view>
      </view>

      <!-- 拼团入口 -->
      <view class="section ss-m-t-20 bg-white ss-p-30 ss-flex ss-row-between ss-col-center" @tap="goGroupon">
        <text class="section-title">拼团状态</text>
        <text class="cicon-forward" />
      </view>

      <!-- 退费状态 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.refundStatus">
        <view class="section-title ss-m-b-16">退费申请</view>
        <view class="info-row ss-flex ss-row-between">
          <text>状态</text><text>{{ refundStatusText(state.refundStatus.applyStatus) }}</text>
        </view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12" v-if="state.refundStatus.refundAmount">
          <text>退费金额</text><text>¥{{ toYuan(state.refundStatus.refundAmount) }}</text>
        </view>
        <view class="info-row ss-flex ss-row-between ss-m-t-12" v-if="state.refundStatus.adminRemark">
          <text>备注</text><text>{{ state.refundStatus.adminRemark }}</text>
        </view>
      </view>

      <view class="ss-m-t-40" />

      <!-- 操作栏 -->
      <su-fixed bottom placeholder bg="bg-white">
        <view class="foot-box ss-flex ss-col-center ss-row-right ss-p-x-30 ss-p-y-16">
          <button
            v-if="state.detail.enrollmentStatus === 'PENDING_DEPOSIT'"
            class="ss-reset-button cancel-btn"
            @tap="onCancel"
          >
            取消报名
          </button>
          <button
            v-if="state.detail.enrollmentStatus === 'PENDING_DEPOSIT'"
            class="ss-reset-button main-btn"
            @tap="goPay('deposit')"
          >
            去支付定金
          </button>
          <button
            v-if="state.detail.enrollmentStatus === 'PENDING_BALANCE'"
            class="ss-reset-button main-btn"
            @tap="goPay('balance')"
          >
            去支付尾款
          </button>
          <button
            v-if="canApply"
            class="ss-reset-button plain-btn"
            @tap="state.showRefundModal = true"
          >
            申请退费
          </button>
          <button v-if="canApply" class="ss-reset-button plain-btn" @tap="state.showTransferModal = true">
            申请转课
          </button>
        </view>
      </su-fixed>
    </view>

    <!-- 退费申请弹窗 -->
    <su-popup :show="state.showRefundModal" round="10" :showClose="true" @close="state.showRefundModal = false">
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

    <!-- 转课申请弹窗 -->
    <su-popup :show="state.showTransferModal" round="10" :showClose="true" @close="state.showTransferModal = false">
      <view class="modal-box ss-p-30">
        <view class="modal-title ss-m-b-20">申请转课</view>
        <uni-easyinput
          v-model="state.transferReason"
          type="textarea"
          maxlength="200"
          placeholder="请填写转课原因"
        />
        <button class="ss-reset-button confirm-btn ss-m-t-30" @tap="submitTransfer">提交申请</button>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import EnrollmentApi from '@/sheep/api/maritime/enrollment';

  const state = reactive({
    detail: {},
    refundStatus: null,
    showRefundModal: false,
    showTransferModal: false,
    refundReason: '',
    transferReason: '',
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

  const ORDER_TYPE_TEXT = { DEPOSIT: '定金', BALANCE: '尾款', FULL: '全款' };
  const ORDER_STATUS_TEXT = { PENDING: '待支付', PAID: '已支付', CLOSED: '已关闭', REFUNDED: '已退款' };
  const REFUND_STATUS_TEXT = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝', REFUNDED: '已退款' };

  const statusText = computed(() => STATUS_TEXT[state.detail.enrollmentStatus] || state.detail.enrollmentStatus);
  const canApply = computed(() =>
    ['DEPOSITED', 'PENDING_BALANCE', 'IN_PROGRESS'].includes(state.detail.enrollmentStatus),
  );

  function toYuan(amount) {
    return amount ? Number(amount).toFixed(2) : '0.00';
  }

  function orderTypeText(type) {
    return ORDER_TYPE_TEXT[type] || type;
  }

  function orderStatusText(status) {
    return ORDER_STATUS_TEXT[status] || status;
  }

  function refundStatusText(status) {
    return REFUND_STATUS_TEXT[status] || status;
  }

  async function getDetail(id) {
    const { code, data } = await EnrollmentApi.getEnrollment(id);
    if (code !== 0) return;
    state.detail = data;

    if (data.enrollmentStatus === 'REFUNDING') {
      const res = await EnrollmentApi.getRefundStatus(id);
      if (res.code === 0) state.refundStatus = res.data;
    }
  }

  function goPay(type) {
    if (type === 'deposit') {
      const pendingOrder = state.detail.orders?.find(
        (o) => o.orderType === 'DEPOSIT' && o.orderStatus === 'PENDING',
      );
      sheep.$router.go('/pages/maritime-sub/enrollment-pay', {
        orderId: pendingOrder?.id,
        enrollmentId: state.detail.id,
        type: 'deposit',
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

  async function submitTransfer() {
    if (!state.transferReason) {
      sheep.$helper.toast('请填写转课原因');
      return;
    }
    const { code } = await EnrollmentApi.applyTransfer({
      enrollmentId: state.detail.id,
      applyReason: state.transferReason,
    });
    if (code === 0) {
      state.showTransferModal = false;
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
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }
  }

  .order-type {
    font-size: 26rpx;
    color: #333;
  }

  .order-no {
    font-size: 22rpx;
    color: #999;
  }

  .order-amount {
    font-size: 28rpx;
    color: #f83528;
    font-weight: 500;
  }

  .order-status {
    font-size: 22rpx;
    color: #999;
  }

  .foot-box {
    button {
      margin-left: 16rpx;
    }
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
</style>
