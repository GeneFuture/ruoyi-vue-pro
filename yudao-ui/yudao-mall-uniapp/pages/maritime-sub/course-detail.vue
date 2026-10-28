<!-- 课程详情
     视觉风格：与首页 pages/maritime/index.vue 同一套 Apple 式设计语言
     （白底标题带 + 浅灰内容带 / 大圆角卡片 + 极淡投影 / 发丝分隔线 / 克制的强调色 / 1px = 2rpx）
     分区：头图 → 课程名称 → 可报班期 → 报名条件 → 课程详情
     说明：无班期时「可报班期」整段不渲染 -->
<template>
  <s-layout title="课程详情" navbar="inner" :onShareAppMessage="shareInfo">
    <view v-if="state.course.id">
      <!-- 头图：无封面时用品牌渐变兜底，与首页班期卡一致 -->
      <view class="hero">
        <image
          v-if="state.course.coverImage"
          class="hero-img"
          :src="state.course.coverImage"
          mode="aspectFill"
        />
        <text v-else class="hero-cert">{{ state.course.certificateType }}</text>
      </view>

      <!-- 课程名称 -->
      <view class="band band-intro">
        <text class="intro-title">{{ state.course.name }}</text>
        <text v-if="state.course.certificateType" class="intro-cert">
          {{ state.course.certificateType }}
        </text>
      </view>

      <!-- 可报班期：没有班期时整段不展示 -->
      <view v-if="sessions.length" class="band band-gray">
        <view class="floor-head">
          <text class="floor-title">可报班期</text>
        </view>

        <view
          v-for="session in sessions"
          :key="session.id"
          class="session"
          hover-class="tap-hover"
          @tap="goSession(session.id)"
        >
          <text class="session-name ss-line-2">
            {{ session.sessionName || session.sessionCode }}
          </text>
          <text class="session-meta">
            {{ session.startDate }} ~ {{ session.endDate }} · {{ session.durationDays }}天
          </text>
          <text v-if="session.city" class="session-meta">{{ session.city }}</text>
          <text v-if="session.enrollmentDeadline" class="session-meta">
            {{ deadlineText(session) }}
          </text>

          <view class="seats">
            <view class="seats-track">
              <view class="seats-fill" :style="{ width: seatPercent(session) + '%' }" />
            </view>
            <text class="seats-text">余 {{ session.remainingCount }}/{{ session.maxStudents }}</text>
          </view>

          <view v-if="session.tuitionAmount" class="session-foot">
            <text class="price">¥{{ yuan(session.tuitionAmount) }}</text>
            <text v-if="session.depositAmount" class="deposit">
              定金 ¥{{ yuan(session.depositAmount) }}
            </text>
          </view>
          <text v-if="grouponSave(session)" class="session-save">
            {{ grouponSaveText(session) }}
          </text>
        </view>
      </view>

      <!-- 报名条件 -->
      <view v-if="conditionItems.length" class="band band-gray">
        <view class="floor-head">
          <text class="floor-title">报名条件</text>
        </view>

        <view class="card card-list">
          <view v-for="(item, index) in conditionItems" :key="index" class="condition">
            <text v-if="conditionItems.length > 1" class="condition-index">
              {{ index + 1 }}
            </text>
            <text class="condition-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 课程详情（富文本） -->
      <view v-if="state.course.description" class="band band-gray">
        <view class="floor-head">
          <text class="floor-title">课程详情</text>
        </view>

        <view class="card card-detail">
          <mp-html :content="detailContent" :tag-style="RICH_TAG_STYLE" />
        </view>
      </view>

      <view class="tail" />
    </view>

    <s-empty v-else-if="!state.loading" text="课程不存在或已下架" icon="/static/data-empty.png" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';
  import {
    RICH_TAG_STYLE,
    normalizeRichText,
    parseConditionItems,
    yuan,
    seatPercent,
    grouponSave,
    deadlineText,
    grouponSaveText,
  } from '@/utils/maritime';

  const state = reactive({
    loading: false,
    course: {},
  });

  /** 可报名班期 */
  const sessions = computed(() => state.course.openSessions || []);

  /** 报名条件：把后台的分行纯文本拆成列表条目 */
  const conditionItems = computed(() =>
    parseConditionItems(state.course.enrollmentConditionText),
  );

  /** 课程详情：富文本，统一字体排版并压掉首个块级标签的上边距 */
  const detailContent = computed(() => normalizeRichText(state.course.description));

  async function getDetail(id) {
    state.loading = true;
    try {
      const { code, data } = await CourseApi.getCourseDetail(id);
      if (code !== 0) return;
      state.course = data;
    } catch (e) {
      // 请求层已提示，这里只兜住异常避免未处理 Promise
    } finally {
      state.loading = false;
    }
  }

  function goSession(sessionId) {
    sheep.$router.go('/pages/maritime-sub/session-detail', { id: sessionId });
  }

  const shareInfo = computed(() => {
    return {
      title: state.course.name,
      image: state.course.coverImage,
      forward: { path: `pages/maritime-sub/course-detail?id=${state.course.id}` },
    };
  });

  onLoad((options) => {
    if (options.id) {
      getDetail(options.id);
    }
  });
</script>

<style lang="scss" scoped>
  // 设计令牌：与首页 pages/maritime/index.vue 保持一致
  $ink: #12161f; // 主文字（近黑，不用纯黑）
  $ink-2: #5b6472; // 次要文字
  $ink-3: #8a93a3; // 说明 / 占位
  $line: #e8ebf0; // 发丝分隔线
  $band: #f5f7fa; // 浅灰分区带
  $white: #ffffff;
  $radius: 40rpx; // 大圆角（卡片）
  $side: 40rpx; // 分区带左右留白

  // 点击反馈：小程序对 <view> 的 :active 支持不可靠，统一用 hover-class
  .tap-hover {
    opacity: 0.6;
  }

  // ---- 头图（无封面时用品牌渐变兜底，取首页班期卡同一处理）----
  .hero {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 420rpx;
    background: linear-gradient(135deg, #2e6bff, #0b3b8c);
  }

  .hero-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .hero-cert {
    font-size: 48rpx;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: rgba(255, 255, 255, 0.92);
    text-align: center;
    padding: 0 $side;
  }

  // ---- 分区带：只有上留白，交替白 / 浅灰 ----
  .band {
    padding: 64rpx $side 0;
    background: $white;

    // 紧跟头图，避免标题上方出现双倍留白
    &-intro {
      padding-top: 48rpx;
    }

    &-gray {
      background: $band;
    }
  }

  .intro-title {
    display: block;
    font-size: 48rpx;
    font-weight: 600;
    letter-spacing: -0.02em;
    line-height: 1.24;
    color: $ink;
  }

  .intro-cert {
    display: block;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: $ink-2;
  }

  // ---- 楼层标题 ----
  .floor-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 32rpx;
  }

  .floor-title {
    font-size: 44rpx;
    font-weight: 600;
    letter-spacing: -0.018em;
    line-height: 1.2;
    color: $ink;
  }

  // ---- 卡片 ----
  .card {
    border-radius: $radius;
    background: $white;
    // 几乎不可见的阴影：在浅灰带上建立层次，又不产生「贴纸感」
    box-shadow: 0 4rpx 20rpx rgba(20, 30, 60, 0.06);
  }

  // 行式列表：上下留白收进行内，首行不再叠加卡片内边距
  .card-list {
    padding: 8rpx 32rpx;
  }

  .card-detail {
    padding: 32rpx;
  }

  // ---- 可报班期 ----
  .session {
    display: block;
    padding: 32rpx;
    border-radius: $radius;
    background: $white;
    box-shadow: 0 4rpx 20rpx rgba(20, 30, 60, 0.06);

    & + .session {
      margin-top: 24rpx;
    }
  }

  .session-name {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    letter-spacing: -0.012em;
    line-height: 1.32;
    color: $ink;
  }

  .session-meta {
    display: block;
    margin-top: 8rpx;
    font-size: 24rpx;
    color: $ink-2;
    font-variant-numeric: tabular-nums;
  }

  .session-name + .session-meta {
    margin-top: 16rpx;
  }

  .seats {
    display: flex;
    align-items: center;
    margin-top: 24rpx;
  }

  .seats-track {
    flex: 1;
    height: 8rpx;
    border-radius: 4rpx;
    background: $line;
    overflow: hidden;
  }

  .seats-fill {
    height: 100%;
    border-radius: 4rpx;
    background: var(--ui-BG-Main);
  }

  .seats-text {
    margin-left: 16rpx;
    font-size: 22rpx;
    color: $ink-2;
    flex-shrink: 0;
    font-variant-numeric: tabular-nums;
  }

  .session-foot {
    display: flex;
    align-items: baseline;
    margin-top: 24rpx;
  }

  .price {
    font-size: 40rpx;
    font-weight: 600;
    letter-spacing: -0.02em;
    color: $ink;
    font-variant-numeric: tabular-nums;
  }

  .deposit {
    margin-left: 16rpx;
    font-size: 24rpx;
    color: $ink-2;
    font-variant-numeric: tabular-nums;
  }

  .session-save {
    display: block;
    margin-top: 8rpx;
    font-size: 24rpx;
    color: var(--ui-price);
    font-variant-numeric: tabular-nums;
  }

  // ---- 报名条件 ----
  .condition {
    display: flex;
    align-items: flex-start;
    padding: 24rpx 0;
  }

  .condition + .condition {
    border-top: 1rpx solid $line;
  }

  .condition-index {
    flex-shrink: 0;
    width: 40rpx;
    height: 40rpx;
    line-height: 40rpx;
    margin-right: 24rpx;
    border-radius: 50%;
    background: $band; // 序号是结构信息不是操作入口，用近黑而非强调色
    color: $ink;
    font-size: 22rpx;
    font-weight: 600;
    text-align: center;
    font-variant-numeric: tabular-nums;
  }

  .condition-text {
    flex: 1;
    font-size: 28rpx;
    line-height: 1.6;
    color: $ink-2;
  }

  // ---- 页面收尾：分区带只有上留白，末尾补一段并让开安全区 ----
  .tail {
    height: 64rpx;
    padding-bottom: constant(safe-area-inset-bottom);
    padding-bottom: env(safe-area-inset-bottom);
  }
</style>
