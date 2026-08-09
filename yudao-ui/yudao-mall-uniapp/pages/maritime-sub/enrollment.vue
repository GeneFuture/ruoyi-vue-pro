<!-- 报名表单 -->
<template>
  <s-layout title="填写报名信息" navbar="inner">
    <view v-if="state.session.id">
      <!-- 班期摘要 -->
      <view class="summary-card ss-m-20 ss-p-30">
        <view class="summary-name ss-line-1">
          {{ state.session.sessionName || state.session.sessionCode }}
        </view>
        <view class="summary-meta ss-m-t-10">
          {{ state.session.startDate }} ~ {{ state.session.endDate }} · {{ state.session.location }}
        </view>
        <view class="summary-amount ss-m-t-16 ss-flex ss-row-between ss-col-center">
          <text>{{ isGroupon ? '拼团定金' : '报名定金' }}</text>
          <text class="amount-num">¥{{ depositAmountText }}</text>
        </view>
        <view class="groupon-tip ss-m-t-10" v-if="isGroupon">
          {{ params.grouponInviteCode ? '加入拼团（已有团，邀请码 ' + params.grouponInviteCode + '）' : '发起新拼团，邀请好友一起拼' }}
        </view>
      </view>

      <!-- 报名信息表单 -->
      <uni-forms ref="formRef" :model="formData" :rules="rules" label-position="top" class="ss-m-20">
        <view class="form-card ss-p-30">
          <uni-forms-item label="真实姓名" name="realName" required>
            <uni-easyinput v-model="formData.realName" placeholder="请输入与证件一致的姓名" />
          </uni-forms-item>
          <uni-forms-item label="身份证号" name="idCard" required>
            <uni-easyinput v-model="formData.idCard" placeholder="请输入身份证号" maxlength="18" />
          </uni-forms-item>
          <uni-forms-item label="手机号" name="phone" required>
            <uni-easyinput
              v-model="formData.phone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
            />
          </uni-forms-item>
          <uni-forms-item label="推荐码（选填）" name="referralCode">
            <uni-easyinput v-model="formData.referralCode" placeholder="好友分享的推荐码" />
          </uni-forms-item>
        </view>
      </uni-forms>

      <view class="agreement ss-flex ss-col-center ss-m-x-20 ss-m-t-10" @tap="state.agree = !state.agree">
        <text :class="state.agree ? 'cicon-check-circle' : 'cicon-circle'" class="agree-icon" />
        <text class="agreement-text ss-m-l-10">我已阅读并同意《报名协议》及退款政策</text>
      </view>

      <!-- 底部提交栏 -->
      <su-fixed bottom placeholder bg="bg-white">
        <view class="foot-box ss-flex ss-col-center ss-p-x-30 ss-p-y-16">
          <view class="ss-flex-1">
            <text class="foot-price">¥{{ depositAmountText }}</text>
          </view>
          <button class="ss-reset-button submit-btn" @tap="submit">提交报名</button>
        </view>
      </su-fixed>
    </view>
  </s-layout>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import SessionApi from '@/sheep/api/maritime/session';
  import EnrollmentApi from '@/sheep/api/maritime/enrollment';

  const formRef = ref(null);
  const params = reactive({
    sessionId: '',
    joinGroupon: false,
    grouponInviteCode: '',
  });

  const state = reactive({
    session: {},
    agree: false,
  });

  const formData = reactive({
    realName: '',
    idCard: '',
    phone: '',
    referralCode: '',
  });

  const rules = {
    realName: {
      rules: [{ required: true, errorMessage: '请输入真实姓名' }],
    },
    idCard: {
      rules: [
        { required: true, errorMessage: '请输入身份证号' },
        {
          pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/,
          errorMessage: '身份证号格式不正确',
        },
      ],
    },
    phone: {
      rules: [
        { required: true, errorMessage: '请输入手机号' },
        { pattern: /^1[3-9]\d{9}$/, errorMessage: '手机号格式不正确' },
      ],
    },
  };

  const isGroupon = computed(() => params.joinGroupon);

  const depositAmountText = computed(() => {
    const amount = isGroupon.value
      ? state.session.grouponDepositAmount
      : state.session.depositAmount;
    return amount ? Number(amount).toFixed(2) : '0.00';
  });

  async function getSession(id) {
    const { code, data } = await SessionApi.getSessionDetail(id);
    if (code !== 0) return;
    state.session = data;
  }

  async function submit() {
    if (!state.agree) {
      sheep.$helper.toast('请先阅读并同意报名协议');
      return;
    }
    const valid = await formRef.value.validate().catch(() => false);
    if (!valid) return;

    const { code, data } = await EnrollmentApi.createEnrollment({
      sessionId: params.sessionId,
      realName: formData.realName,
      idCard: formData.idCard,
      phone: formData.phone,
      referralCode: formData.referralCode || undefined,
      joinGroupon: params.joinGroupon,
      grouponInviteCode: params.grouponInviteCode || undefined,
    });
    if (code !== 0) return;

    sheep.$router.redirect('/pages/maritime-sub/enrollment-pay', {
      orderId: data.orderId,
      enrollmentId: data.enrollmentId,
      type: 'deposit',
    });
  }

  onLoad((options) => {
    params.sessionId = options.sessionId;
    params.joinGroupon = options.joinGroupon === 'true';
    params.grouponInviteCode = options.grouponInviteCode || '';

    // 优先使用本地缓存的分享推荐码
    const cachedRef = uni.getStorageSync('maritime_referral_code');
    if (cachedRef) {
      formData.referralCode = cachedRef;
    }

    if (params.sessionId) {
      getSession(params.sessionId);
    }
  });
</script>

<style lang="scss" scoped>
  .summary-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .summary-name {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }

  .summary-meta {
    font-size: 24rpx;
    color: #999;
  }

  .summary-amount {
    font-size: 26rpx;
    color: #666;
  }

  .amount-num {
    font-size: 32rpx;
    font-weight: 600;
    color: #f83528;
  }

  .groupon-tip {
    font-size: 22rpx;
    color: #ff9900;
  }

  .form-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .agreement {
    font-size: 24rpx;
    color: #999;
  }

  .agree-icon {
    color: var(--ui-BG-Main);
    font-size: 32rpx;
  }

  .foot-box {
    .foot-price {
      font-size: 36rpx;
      font-weight: 600;
      color: #f83528;
    }
  }

  .submit-btn {
    width: 320rpx;
    height: 80rpx;
    line-height: 80rpx;
    font-size: 28rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 40rpx;
  }
</style>
