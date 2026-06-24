<!-- 课程详情 -->
<template>
  <s-layout title="课程详情" navbar="inner" :onShareAppMessage="shareInfo">
    <view v-if="state.course.id">
      <image class="cover-img" :src="state.course.coverImage" mode="aspectFill" />

      <view class="ss-p-x-30 ss-p-y-20 bg-white">
        <view class="course-name">{{ state.course.name }}</view>
        <view class="tag ss-m-t-16">{{ state.course.certificateType }}</view>
      </view>

      <!-- 可报名班期 -->
      <view class="section ss-m-t-20 bg-white ss-p-30">
        <view class="section-title ss-m-b-20">可报名班期</view>
        <view
          v-for="session in state.course.openSessions"
          :key="session.id"
          class="session-item ss-flex ss-col-center ss-row-between"
          @tap="goSession(session.id)"
        >
          <view class="ss-flex-1">
            <view class="session-name ss-line-1">
              {{ session.sessionName || session.sessionCode }}
            </view>
            <view class="session-meta ss-m-t-10">
              {{ session.startDate }} ~ {{ session.endDate }} · {{ session.location }}
            </view>
            <view class="session-meta ss-m-t-6">
              剩余名额 {{ session.remainingCount }}/{{ session.maxStudents }}
            </view>
          </view>
          <view class="ss-flex-col ss-row-right">
            <view class="session-price" v-if="session.tuitionAmount">
              ¥{{ Number(session.tuitionAmount).toFixed(0) }}
            </view>
            <view class="groupon-badge ss-m-t-10" v-if="session.isGrouponEnabled">可拼团</view>
          </view>
        </view>
        <s-empty
          v-if="!state.course.openSessions || state.course.openSessions.length === 0"
          text="暂无可报名班期"
          icon="/static/data-empty.png"
        />
      </view>

      <!-- 报名条件 -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.course.enrollmentConditionText">
        <view class="section-title ss-m-b-20">报名条件</view>
        <view class="section-text">{{ state.course.enrollmentConditionText }}</view>
      </view>

      <!-- 课程详情（富文本） -->
      <view class="section ss-m-t-20 bg-white ss-p-30" v-if="state.course.description">
        <view class="section-title ss-m-b-20">课程详情</view>
        <rich-text :nodes="state.course.description" />
      </view>
    </view>

    <s-empty v-else-if="!state.loading" text="课程不存在或已下架" icon="/static/data-empty.png" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';
  import ReferralApi from '@/sheep/api/maritime/referral';

  const state = reactive({
    loading: false,
    course: {},
    myReferralCode: '',
  });

  async function getDetail(id) {
    state.loading = true;
    const { code, data } = await CourseApi.getCourseDetail(id);
    state.loading = false;
    if (code !== 0) return;
    state.course = data;
  }

  async function getMyReferralCode() {
    if (!sheep.$store('user').isLogin) return;
    const { code, data } = await ReferralApi.getMyReferralInfo().catch(() => ({}));
    if (code === 0) state.myReferralCode = data.referralCode;
  }

  function goSession(sessionId) {
    sheep.$router.go('/pages/maritime/session-detail', { id: sessionId });
  }

  const shareInfo = computed(() => {
    const refQuery = state.myReferralCode ? `&ref=${state.myReferralCode}` : '';
    return {
      title: state.course.name,
      image: state.course.coverImage,
      forward: { path: `pages/maritime/course-detail?id=${state.course.id}${refQuery}` },
    };
  });

  onLoad((options) => {
    if (options.ref) {
      uni.setStorageSync('maritime_referral_code', options.ref);
    }
    if (options.id) {
      getDetail(options.id);
    }
    getMyReferralCode();
  });
</script>

<style lang="scss" scoped>
  .cover-img {
    width: 100%;
    height: 420rpx;
  }

  .course-name {
    font-size: 34rpx;
    font-weight: 600;
    color: #222;
  }

  .tag {
    display: inline-block;
    font-size: 22rpx;
    color: var(--ui-BG-Main);
    background: rgba(255, 90, 95, 0.1);
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
  }

  .section-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #222;
  }

  .section-text {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
  }

  .session-item {
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }
  }

  .session-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .session-meta {
    font-size: 22rpx;
    color: #999;
  }

  .session-price {
    font-size: 30rpx;
    font-weight: 600;
    color: #f83528;
  }

  .groupon-badge {
    font-size: 20rpx;
    color: #ff9900;
    border: 1rpx solid #ff9900;
    padding: 2rpx 10rpx;
    border-radius: 6rpx;
  }
</style>
