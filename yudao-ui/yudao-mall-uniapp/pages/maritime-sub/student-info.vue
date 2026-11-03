<!-- 学员信息
     维护报名常用的姓名 / 身份证 / 手机号，报名页会自动带出；
     身份证号后端加密存储，展示/编辑仅本人可见。 -->
<template>
  <s-layout title="学员信息" navbar="inner">
    <view class="ss-m-20">
      <view class="intro-card ss-p-30">
        <view class="intro-title">报名常用信息</view>
        <view class="intro-desc">
          报名时会自动带出以下信息；身份证号加密保存，仅用于报名与海事局报备。
        </view>
      </view>

      <uni-forms
        ref="formRef"
        :model-value="formData"
        :rules="rules"
        label-position="top"
        class="ss-m-t-20"
      >
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
        </view>
      </uni-forms>

      <button class="ss-reset-button save-btn ss-m-t-40" :disabled="state.saving" @tap="submit">
        {{ state.saving ? '保存中...' : '保存' }}
      </button>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import StudentInfoApi from '@/sheep/api/maritime/studentInfo';

  defineOptions({ name: 'StudentInfo' });

  const formRef = ref(null);
  const state = reactive({
    saving: false,
  });
  const formData = reactive({
    realName: '',
    idCard: '',
    phone: '',
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

  async function loadStudentInfo() {
    const { code, data } = await StudentInfoApi.getStudentInfo().catch(() => ({}));
    if (code !== 0 || !data) {
      return;
    }
    formData.realName = data.realName || '';
    formData.idCard = data.idCard || '';
    formData.phone = data.phone || '';
  }

  async function submit() {
    if (state.saving) {
      return;
    }
    try {
      await formRef.value.validate();
    } catch (errors) {
      // 校验失败：字段下方虽有红字，但页面较长时容易被忽略，补一条显眼的 toast
      const first = Array.isArray(errors) ? errors[0] : errors;
      sheep.$helper.toast(first?.errorMessage || '请检查填写内容');
      return;
    }
    state.saving = true;
    try {
      const { code } = await StudentInfoApi.saveStudentInfo({
        realName: formData.realName.trim(),
        idCard: formData.idCard.trim(),
        phone: formData.phone.trim(),
      });
      if (code === 0) {
        setTimeout(() => sheep.$router.back(), 600);
      }
    } finally {
      state.saving = false;
    }
  }

  onLoad(() => {
    loadStudentInfo();
  });
</script>

<style lang="scss" scoped>
  .intro-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .intro-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }

  .intro-desc {
    margin-top: 12rpx;
    font-size: 24rpx;
    line-height: 1.6;
    color: #999;
  }

  .form-card {
    background: #fff;
    border-radius: 16rpx;
  }

  .save-btn {
    width: 100%;
    height: 88rpx;
    line-height: 88rpx;
    font-size: 30rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 44rpx;
  }
</style>
