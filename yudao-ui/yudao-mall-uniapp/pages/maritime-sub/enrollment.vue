<!-- 确认报名信息
     学员信息只读展示（姓名/身份证/手机号在「学员信息」页维护），
     本页负责确认班级、学校、学费，然后创建报名并进入支付。 -->
<template>
  <s-layout title="确认报名信息" navbar="inner">
    <view v-if="state.session.id" class="page">
      <!-- 学员信息（只读） -->
      <view class="card">
        <view class="card-head">
          <text class="card-title">学员信息</text>
          <text class="card-link" @tap="goStudentInfo">维护 ›</text>
        </view>
        <view class="row">
          <text class="row-label">姓名</text>
          <text class="row-value" :class="{ 'is-empty': !student.realName }">
            {{ student.realName || '未填写' }}
          </text>
        </view>
        <view class="row">
          <text class="row-label">身份证号</text>
          <text class="row-value" :class="{ 'is-empty': !student.idCard }">
            {{ maskedIdCard || '未填写' }}
          </text>
        </view>
        <view class="row">
          <text class="row-label">手机号</text>
          <text class="row-value" :class="{ 'is-empty': !student.phone }">
            {{ maskedPhone || '未填写' }}
          </text>
        </view>
        <view v-if="!studentInfoComplete" class="info-warning" @tap="goStudentInfo">
          报名需要完整的姓名、身份证号和手机号，去完善 ›
        </view>
      </view>

      <!-- 班级信息 -->
      <view class="card">
        <view class="card-head">
          <text class="card-title">班级信息</text>
          <text v-if="session.certificateType" class="cert-tag">{{ session.certificateType }}</text>
        </view>
        <view class="row" v-if="session.courseName">
          <text class="row-label">课程</text>
          <text class="row-value">{{ session.courseName }}</text>
        </view>
        <view class="row">
          <text class="row-label">班级</text>
          <text class="row-value">{{ session.sessionName || session.sessionCode }}</text>
        </view>
        <view class="row" v-if="session.sessionName && session.sessionCode">
          <text class="row-label">班期编号</text>
          <text class="row-value">{{ session.sessionCode }}</text>
        </view>
        <view class="row" v-if="dateRangeText">
          <text class="row-label">开班日期</text>
          <text class="row-value">{{ dateRangeText }}</text>
        </view>
        <view class="row" v-if="session.enrollmentDeadline">
          <text class="row-label">报名截止</text>
          <text class="row-value">{{ session.enrollmentDeadline }}</text>
        </view>
        <view class="row" v-if="session.instructorInfo">
          <text class="row-label">授课讲师</text>
          <text class="row-value">{{ session.instructorInfo }}</text>
        </view>
        <view class="row" v-if="classLocationText">
          <text class="row-label">上课地点</text>
          <text class="row-value">{{ classLocationText }}</text>
        </view>
        <view class="row">
          <text class="row-label">剩余名额</text>
          <text class="row-value">{{ remainingText }}</text>
        </view>
        <view class="row">
          <text class="row-label">报名方式</text>
          <text class="row-value">{{ grouponEnabled ? '拼团报名' : '单独报名' }}</text>
        </view>
        <view v-if="grouponEnabled" class="groupon-tip">{{ grouponTipText }}</view>
      </view>

      <!-- 学校信息 -->
      <view class="card">
        <view class="card-head">
          <text class="card-title">学校信息</text>
        </view>
        <view v-if="hasCampus">
          <text v-if="venue.campusName" class="venue-name">{{ venue.campusName }}</text>
          <view v-if="addressText" class="venue-row" hover-class="tap-hover" @tap="copyAddress">
            <text class="venue-row-label">地址</text>
            <text class="venue-row-value">{{ addressText }}</text>
            <text class="venue-row-hint">复制</text>
          </view>
          <view
            v-if="venue.contactPhone"
            class="venue-row"
            hover-class="tap-hover"
            @tap="callCampus"
          >
            <text class="venue-row-label">电话</text>
            <text class="venue-row-value">{{ venue.contactPhone }}</text>
            <text class="venue-row-hint">拨打</text>
          </view>
        </view>
        <view v-else class="venue-empty">学校信息待确认，报名后可联系机构咨询</view>
      </view>

      <!-- 学费确认 -->
      <view class="card">
        <view class="card-head">
          <text class="card-title">学费确认</text>
        </view>
        <view class="row" v-if="session.tuitionAmount">
          <text class="row-label">学费总额</text>
          <view class="row-value amount-row">
            <text class="amount-strong">¥{{ yuan(session.tuitionAmount) }}</text>
            <text
              class="tag"
              :class="session.tuitionIncludesAccommodation ? 'is-included' : 'is-excluded'"
            >
              {{ session.tuitionIncludesAccommodation ? '含住宿' : '不含住宿' }}
            </text>
          </view>
        </view>
        <view class="row" v-if="isFullPayment">
          <text class="row-label">报名方式</text>
          <text class="row-value">全款报名，报名时一次付清</text>
        </view>
        <view class="row" v-else-if="session.depositAmount">
          <text class="row-label">报名定金</text>
          <text class="row-value">¥{{ yuan(session.depositAmount) }}</text>
        </view>
        <view class="row" v-if="balanceAmount > 0">
          <text class="row-label">尾款</text>
          <text class="row-value">¥{{ yuan(balanceAmount) }}（开班前付清）</text>
        </view>
        <view class="row" v-if="grouponDiscount > 0">
          <text class="row-label">拼团优惠</text>
          <view class="row-value amount-row">
            <text class="amount-discount">-¥{{ yuan(grouponDiscount) }}</text>
            <text class="row-note">成团后尾款抵扣，未成团按原价</text>
          </view>
        </view>

        <view v-if="extraFees.length" class="extra-fees">
          <text class="sub-title">另行缴费</text>
          <text class="sub-note">到校报到时缴纳，不含在本次支付中</text>
          <view v-for="(item, index) in extraFees" :key="index" class="fee-item">
            <text class="fee-name">{{ item.name }}</text>
            <view class="fee-right">
              <text v-if="hasAmount(item)" class="fee-amount">¥{{ yuan(item.amount) }}</text>
              <text class="tag" :class="item.required ? 'is-required' : 'is-optional'">
                {{ item.required ? '必须' : '可选' }}
              </text>
            </view>
          </view>
        </view>

        <view v-if="session.refundPolicyText" class="policy">
          <text class="sub-title">退款政策</text>
          <text class="policy-text">{{ session.refundPolicyText }}</text>
        </view>

        <view class="pay-row">
          <text class="pay-label">{{
            isFullPayment ? '本次支付（全款）' : '本次支付（定金）'
          }}</text>
          <text class="pay-amount">¥{{ payAmountText }}</text>
        </view>
      </view>

      <view
        class="agreement ss-flex ss-col-center ss-m-x-20 ss-m-t-10"
        @tap="state.agree = !state.agree"
      >
        <text :class="state.agree ? 'cicon-check-circle' : 'cicon-circle'" class="agree-icon" />
        <text class="agreement-text ss-m-l-10">
          我已确认以上班级、学校与学费信息，并同意《报名协议》及退款政策
        </text>
      </view>

      <view class="ss-m-t-40" />

      <!-- 底部提交栏 -->
      <su-fixed bottom placeholder bg="bg-white">
        <view class="foot-box ss-flex ss-col-center ss-p-x-30 ss-p-y-16">
          <view class="foot-price-box ss-flex-1">
            <text class="foot-price">¥{{ payAmountText }}</text>
            <text class="foot-label">{{ isFullPayment ? '全款' : '定金' }}</text>
          </view>
          <button class="ss-reset-button submit-btn" :disabled="state.submitting" @tap="submit">
            {{ state.submitting ? '提交中...' : '提交报名' }}
          </button>
        </view>
      </su-fixed>
    </view>

    <s-empty v-else-if="!state.loading" text="班期不存在" icon="/static/data-empty.png" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import SessionApi from '@/sheep/api/maritime/session';
  import EnrollmentApi from '@/sheep/api/maritime/enrollment';
  import StudentInfoApi from '@/sheep/api/maritime/studentInfo';
  import { fullAddress, yuan } from '@/utils/maritime';

  const ID_CARD_RE = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/;
  const PHONE_RE = /^1[3-9]\d{9}$/;

  const params = reactive({
    sessionId: '',
    joinGroupon: false,
    grouponInviteCode: '',
  });

  const state = reactive({
    loading: true,
    session: {},
    studentInfo: {},
    agree: false,
    submitting: false,
  });

  const session = computed(() => state.session);
  const venue = computed(() => state.session.venue || {});
  const student = computed(() => state.studentInfo || {});

  /** 学员信息是否完整（只读展示，不完整时引导去学员信息页填写） */
  const studentInfoComplete = computed(
    () => !!(student.value.realName && student.value.idCard && student.value.phone),
  );

  /** 身份证/手机号脱敏展示，完整信息在学员信息页查看 */
  function maskMiddle(value, head, tail) {
    const text = String(value || '');
    if (!text || text.length <= head + tail) return text;
    return `${text.slice(0, head)}${'*'.repeat(text.length - head - tail)}${text.slice(-tail)}`;
  }

  const maskedIdCard = computed(() => maskMiddle(student.value.idCard, 6, 4));
  const maskedPhone = computed(() => maskMiddle(student.value.phone, 3, 4));

  const addressText = computed(() => fullAddress(venue.value));

  /** 上课城市 / 区县，用于班级信息里的「上课地点」 */
  const classLocationText = computed(() =>
    [venue.value.city, venue.value.district]
      .map((part) => String(part ?? '').trim())
      .filter(Boolean)
      .join(' · '),
  );

  const hasCampus = computed(
    () =>
      !!addressText.value ||
      ['campusName', 'contactPhone'].some((key) => String(venue.value[key] ?? '').trim()),
  );

  const dateRangeText = computed(() => {
    const s = state.session;
    if (!s.startDate) return '';
    const days = s.durationDays ? ` · ${s.durationDays}天` : '';
    return `${s.startDate} ~ ${s.endDate || ''}${days}`;
  });

  const remainingText = computed(() => {
    const s = state.session;
    return `${s.remainingCount ?? 0}/${s.maxStudents ?? 0}`;
  });

  /** 定金为 0 或 ≥ 学费时按全款报名；全款模式不参与拼团 */
  const isFullPayment = computed(() => {
    const tuition = Number(state.session.tuitionAmount || 0);
    const deposit = Number(state.session.depositAmount || 0);
    return deposit <= 0 || (tuition > 0 && deposit >= tuition);
  });

  /** 实际是否按拼团报名：全款模式强制关闭 */
  const grouponEnabled = computed(() => params.joinGroupon && !isFullPayment.value);

  const payAmount = computed(() =>
    isFullPayment.value ? state.session.tuitionAmount : state.session.depositAmount,
  );
  const payAmountText = computed(() => yuan(payAmount.value));

  const balanceAmount = computed(() => {
    if (isFullPayment.value) return 0;
    return Math.max(
      0,
      Number(state.session.tuitionAmount || 0) - Number(state.session.depositAmount || 0),
    );
  });

  const grouponDiscount = computed(() =>
    grouponEnabled.value ? Number(state.session.grouponDiscountAmount || 0) : 0,
  );

  const extraFees = computed(() => state.session.extraFeeItems || []);

  function hasAmount(item) {
    return item.amount !== null && item.amount !== undefined && item.amount !== '';
  }

  const grouponTipText = computed(() => {
    const base = params.grouponInviteCode
      ? `加入拼团（邀请码 ${params.grouponInviteCode}）`
      : '发起新拼团，邀请好友一起拼';
    const requiredCount = state.session.grouponRequiredCount || 3;
    const discountText =
      grouponDiscount.value > 0
        ? `，成团后尾款立减 ¥${yuan(grouponDiscount.value)}（未成团按原价）`
        : '';
    return `${base}，${requiredCount} 人成团${discountText}`;
  });

  function copyAddress() {
    sheep.$helper.copyText(addressText.value);
  }

  function callCampus() {
    const phone = String(venue.value.contactPhone || '');
    if (phone) {
      uni.makePhoneCall({ phoneNumber: phone });
    }
  }

  async function getSession(id) {
    state.loading = true;
    const { code, data } = await SessionApi.getSessionDetail(id);
    state.loading = false;
    if (code !== 0) return;
    state.session = data;
  }

  async function loadStudentInfo() {
    const { code, data } = await StudentInfoApi.getStudentInfo().catch(() => ({}));
    if (code !== 0) return;
    state.studentInfo = data || {};
  }

  function goStudentInfo() {
    sheep.$router.go('/pages/maritime-sub/student-info');
  }

  function promptCompleteStudentInfo() {
    uni.showModal({
      title: '请先完善学员信息',
      content: '报名需要姓名、身份证号和手机号，是否前往学员信息页填写？',
      confirmText: '去填写',
      success: (res) => {
        if (res.confirm) goStudentInfo();
      },
    });
  }

  async function submit() {
    if (state.submitting) return;
    if (!studentInfoComplete.value) {
      promptCompleteStudentInfo();
      return;
    }
    if (!ID_CARD_RE.test(student.value.idCard)) {
      sheep.$helper.toast('身份证号格式不正确，请到学员信息中修改');
      return;
    }
    if (!PHONE_RE.test(student.value.phone)) {
      sheep.$helper.toast('手机号格式不正确，请到学员信息中修改');
      return;
    }
    if (!state.agree) {
      sheep.$helper.toast('请先确认班级、学校与学费信息');
      return;
    }

    state.submitting = true;
    try {
      const { code, data } = await EnrollmentApi.createEnrollment({
        sessionId: params.sessionId,
        realName: student.value.realName.trim(),
        idCard: student.value.idCard.trim(),
        phone: student.value.phone.trim(),
        joinGroupon: grouponEnabled.value,
        grouponInviteCode: grouponEnabled.value ? params.grouponInviteCode || undefined : undefined,
      });
      if (code !== 0) return;

      sheep.$router.redirect('/pages/maritime-sub/enrollment-pay', {
        orderId: data.orderId,
        enrollmentId: data.enrollmentId,
        type: isFullPayment.value ? 'full' : 'deposit',
      });
    } finally {
      state.submitting = false;
    }
  }

  onLoad((options) => {
    params.sessionId = options.sessionId;
    params.joinGroupon = options.joinGroupon === 'true';
    params.grouponInviteCode = options.grouponInviteCode || '';

    if (params.sessionId) {
      getSession(params.sessionId);
    } else {
      state.loading = false;
    }
  });

  // 每次显示都刷新学员信息：从「学员信息」页返回后能立刻反映修改
  onShow(() => {
    loadStudentInfo();
  });
</script>

<style lang="scss" scoped>
  // 设计令牌：与班期详情页保持一致
  $ink: #12161f;
  $ink-2: #5b6472;
  $ink-3: #8a93a3;
  $line: #e8ebf0;
  $band: #f5f7fa;
  $white: #ffffff;
  $radius: 32rpx;

  .tap-hover {
    opacity: 0.6;
  }

  .page {
    min-height: 100vh;
    padding: 20rpx 0 40rpx;
    background: $band;
  }

  .card {
    margin: 20rpx;
    padding: 30rpx;
    background: $white;
    border-radius: $radius;
  }

  .card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8rpx;
  }

  .card-title {
    font-size: 30rpx;
    font-weight: 600;
    letter-spacing: -0.01em;
    color: $ink;
  }

  .card-link {
    font-size: 24rpx;
    color: var(--ui-BG-Main);
  }

  .card-sub {
    font-size: 22rpx;
    color: $ink-3;
  }

  .cert-tag {
    padding: 4rpx 14rpx;
    font-size: 22rpx;
    color: var(--ui-BG-Main);
    background: rgba(30, 94, 255, 0.08);
    border-radius: 8rpx;
  }

  .row {
    display: flex;
    align-items: flex-start;
    margin-top: 22rpx;
  }

  .row-label {
    width: 150rpx;
    flex-shrink: 0;
    font-size: 26rpx;
    line-height: 1.5;
    color: $ink-3;
  }

  .row-value {
    flex: 1;
    font-size: 28rpx;
    line-height: 1.5;
    color: $ink;
    word-break: break-all;

    &.is-empty {
      color: $ink-3;
    }
  }

  .row-note {
    margin-left: 12rpx;
    font-size: 22rpx;
    color: $ink-3;
  }

  .info-warning {
    margin-top: 20rpx;
    padding: 16rpx 20rpx;
    font-size: 24rpx;
    line-height: 1.5;
    color: #d46b08;
    background: #fff7e6;
    border-radius: 12rpx;
  }

  .groupon-tip {
    margin-top: 20rpx;
    padding: 14rpx 18rpx;
    font-size: 22rpx;
    line-height: 1.5;
    color: #d46b08;
    background: #fff7e6;
    border-radius: 12rpx;
  }

  // ---- 学校 ----
  .venue-name {
    display: block;
    margin-top: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    line-height: 1.32;
    color: $ink;
  }

  .venue-row {
    display: flex;
    align-items: baseline;
    margin-top: 20rpx;
  }

  .venue-row-label {
    width: 80rpx;
    flex-shrink: 0;
    font-size: 26rpx;
    color: $ink-3;
  }

  .venue-row-value {
    flex: 1;
    font-size: 28rpx;
    line-height: 1.5;
    color: $ink-2;
  }

  .venue-row-hint {
    flex-shrink: 0;
    margin-left: 16rpx;
    font-size: 24rpx;
    color: var(--ui-BG-Main);
  }

  .venue-empty {
    margin-top: 16rpx;
    font-size: 26rpx;
    color: $ink-3;
  }

  // ---- 学费 ----
  .amount-row {
    display: flex;
    align-items: center;
  }

  .amount-strong {
    font-size: 30rpx;
    font-weight: 600;
    color: #f83528;
  }

  .amount-discount {
    font-size: 28rpx;
    font-weight: 600;
    color: #f83528;
  }

  .tag {
    margin-left: 12rpx;
    padding: 2rpx 10rpx;
    font-size: 20rpx;
    line-height: 1.6;
    border-radius: 6rpx;
    flex-shrink: 0;

    &.is-included {
      color: #0f9d58;
      background: rgba(15, 157, 88, 0.08);
    }

    &.is-excluded {
      color: #d46b08;
      background: #fff7e6;
    }

    &.is-required {
      color: #d4380d;
      background: #fff1f0;
    }

    &.is-optional {
      color: $ink-3;
      background: $band;
    }
  }

  .extra-fees,
  .policy {
    margin-top: 28rpx;
    padding-top: 24rpx;
    border-top: 1rpx solid $line;
  }

  .sub-title {
    display: block;
    font-size: 26rpx;
    font-weight: 600;
    color: $ink;
  }

  .sub-note {
    display: block;
    margin-top: 6rpx;
    font-size: 22rpx;
    color: $ink-3;
  }

  .fee-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;
  }

  .fee-name {
    font-size: 26rpx;
    color: $ink-2;
  }

  .fee-right {
    display: flex;
    align-items: center;
  }

  .fee-amount {
    font-size: 26rpx;
    color: $ink;
  }

  .policy-text {
    display: block;
    margin-top: 12rpx;
    font-size: 24rpx;
    line-height: 1.6;
    color: $ink-2;
    white-space: pre-wrap;
  }

  .pay-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-top: 28rpx;
    padding-top: 24rpx;
    border-top: 1rpx solid $line;
  }

  .pay-label {
    font-size: 26rpx;
    color: $ink-2;
  }

  .pay-amount {
    font-size: 40rpx;
    font-weight: 600;
    color: #f83528;
  }

  // ---- 协议 ----
  .agreement {
    font-size: 24rpx;
    color: $ink-3;
  }

  .agree-icon {
    color: var(--ui-BG-Main);
    font-size: 32rpx;
  }

  .agreement-text {
    flex: 1;
    line-height: 1.5;
  }

  // ---- 底部提交栏 ----
  .foot-price-box {
    display: flex;
    align-items: baseline;
  }

  .foot-price {
    font-size: 36rpx;
    font-weight: 600;
    color: #f83528;
  }

  .foot-label {
    margin-left: 8rpx;
    font-size: 22rpx;
    color: $ink-3;
  }

  .submit-btn {
    width: 300rpx;
    height: 80rpx;
    line-height: 80rpx;
    font-size: 28rpx;
    color: $white;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 40rpx;

    &[disabled] {
      opacity: 0.6;
      color: $white;
    }
  }
</style>
