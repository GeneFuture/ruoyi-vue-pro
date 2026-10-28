<!-- 班期详情
     视觉风格：与首页 pages/maritime/index.vue、课程详情页同一套 Apple 式设计语言
     （白底标题带 + 浅灰内容带 / 大圆角卡片 + 极淡投影 / 发丝分隔线 / 克制的强调色 / 1px = 2rpx）
     分区：头图 → 课程与班期 → 班期信息 → 费用 → 正在拼团 → 课程介绍 → 报名条件
           → 学校 → 教学条件 → 住宿条件 → 退款政策 → 底部报名栏
     说明：本页有 4 个入口（首页金刚区/Banner、首页近期开班、开班计划列表、课程详情可报班期），
           标题位放班级名（没有名称时退化为班期编号），课程名作为副标题展示 -->
<template>
  <s-layout title="班期详情" navbar="inner">
    <view v-if="state.session.id">
      <!-- 头图：无封面时用品牌渐变兜底，与首页班期卡一致 -->
      <view class="hero">
        <image
          v-if="session.coverImage"
          class="hero-img"
          :src="session.coverImage"
          mode="aspectFill"
        />
        <text v-else class="hero-cert">{{ session.certificateType }}</text>

        <!-- 班级与课程：半透明玻璃层，漂浮在头图底部 -->
        <view class="hero-overlay">
          <text class="intro-title">{{ sessionTitle }}</text>
          <view v-if="session.certificateType || cityText" class="intro-meta">
            <text v-if="session.certificateType" class="intro-cert">
              {{ session.certificateType }}
            </text>
            <view v-if="cityText" class="intro-city">
              <text class="intro-city-text">{{ cityText }}</text>
              <view
                v-if="hasCoordinate"
                class="intro-map-btn"
                hover-class="tap-hover"
                @tap.stop="openLocation"
              >
                <text class="cicon-place intro-city-icon" />
              </view>
            </view>
          </view>
          <!--          <view class="intro-session">-->
          <!--            <text v-if="courseTitle && courseTitle !== sessionTitle" class="intro-session-name">-->
          <!--              {{ courseTitle }}-->
          <!--            </text>-->
          <!--            <text class="status-pill" :class="{ 'is-open': isOpen }">-->
          <!--              {{ sessionStatusText(session.sessionStatus) }}-->
          <!--            </text>-->
          <!--          </view>-->
        </view>
      </view>

      <!-- 好友拼团邀请：邀请码写入当前班期，报名时自动带入 -->
      <view v-if="shareParams.grouponInviteCode" class="invite-bar">
        <text class="invite-bar-text">
          好友邀请你拼团，报名时将自动加入（邀请码 {{ shareParams.grouponInviteCode }}）
        </text>
      </view>

      <!-- 班期信息 -->
      <view class="band band-gray">
        <view class="floor-head"><text class="floor-title">班期信息</text></view>
        <view class="card card-facts">
          <view class="fact">
            <text class="fact-label">上课时间</text>
            <text class="fact-value">{{ dateRangeText }}</text>
          </view>
          <view class="fact" v-if="session.enrollmentDeadline">
            <text class="fact-label">报名截止</text>
            <text class="fact-value">{{ session.enrollmentDeadline }}</text>
          </view>
          <view class="fact" v-if="session.instructorInfo">
            <text class="fact-label">授课讲师</text>
            <text class="fact-value">{{ session.instructorInfo }}</text>
          </view>
          <view class="fact">
            <text class="fact-label">剩余名额</text>
            <view class="seats">
              <view class="seats-track">
                <view class="seats-fill" :style="{ width: seatPercent(session) + '%' }" />
              </view>
              <text class="seats-text">{{ seatText }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 费用信息 -->
      <view class="band band-gray" v-if="session.tuitionAmount || extraFees.length">
        <view class="floor-head"><text class="floor-title">费用信息</text></view>
        <view class="card card-facts">
          <view class="fact" v-if="session.tuitionAmount">
            <text class="fact-label">学费总额</text>
            <view class="fact-value fact-price-row">
              <text class="fact-price">¥{{ yuan(session.tuitionAmount) }}</text>
              <!-- 住宿可能是学费里含的，也可能到校另缴，学员最关心这点，直接标在学费旁边 -->
              <text
                class="tag"
                :class="session.tuitionIncludesAccommodation ? 'is-included' : 'is-excluded'"
              >
                {{ session.tuitionIncludesAccommodation ? '含住宿' : '不含住宿' }}
              </text>
            </view>
          </view>
          <view class="fact" v-if="session.depositAmount && !isFullPayment">
            <text class="fact-label">报名定金</text>
            <text class="fact-value">¥{{ yuan(session.depositAmount) }}</text>
          </view>
          <view class="fact" v-if="isFullPayment">
            <text class="fact-label">报名方式</text>
            <text class="fact-value">全款报名，报名时一次付清</text>
          </view>
          <view
            class="fact"
            v-if="session.isGrouponEnabled && !isFullPayment && session.grouponDiscountAmount"
          >
            <text class="fact-label">拼团优惠</text>
            <view class="fact-value fact-price-row">
              <text class="fact-price">-¥{{ yuan(session.grouponDiscountAmount) }}</text>
              <text class="fact-note-text">成团后尾款抵扣，未成团按原价</text>
            </view>
          </view>

          <!-- 另行缴费：到校报到时缴纳，与在线支付的学费 / 定金分开列，必须项与可选项一眼可分 -->
          <view class="fact" v-if="extraFees.length">
            <text class="fact-label">另行缴费</text>
            <text class="fact-note-text">到校报到时缴纳</text>
          </view>
          <view class="fact" v-for="(item, index) in extraFees" :key="index">
            <text class="fact-label">{{ item.name }}</text>
            <view class="fact-value extra-fee">
              <text v-if="hasAmount(item)" class="extra-fee-amount">
                ¥{{ yuan(item.amount) }}
              </text>
              <text class="tag" :class="item.required ? 'is-required' : 'is-optional'">
                {{ item.required ? '必须' : '可选' }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 正在拼团（全款报名没有定金，不参与拼团） -->
      <view class="band band-gray" v-if="groupons.length && !isFullPayment">
        <view class="floor-head"><text class="floor-title">正在拼团</text></view>
        <view class="card card-list">
          <view
            v-for="(groupon, index) in groupons"
            :key="groupon.id"
            class="groupon"
            :class="{ 'is-divided': index > 0 }"
          >
            <view class="groupon-main">
              <text class="groupon-progress">{{ grouponText(groupon) }}</text>
              <text class="groupon-expire">{{ grouponExpireText(groupon) }}</text>
            </view>
            <button
              class="ss-reset-button join-btn"
              hover-class="tap-hover"
              @tap="goEnroll(true, groupon.inviteCode)"
            >
              加入拼团
            </button>
          </view>
        </view>
      </view>

      <!-- 课程介绍：默认折叠，首屏先能看清是什么课，正文再看长文 -->
      <view class="band band-gray" v-if="session.description">
        <view class="floor-head"><text class="floor-title">课程介绍</text></view>
        <view class="card card-detail">
          <view class="detail-wrap">
            <view class="detail-body" :class="{ 'is-clamped': needCollapse && !expanded }">
              <mp-html :content="detailContent" :tag-style="RICH_TAG_STYLE" />
            </view>
            <view v-if="needCollapse && !expanded" class="detail-fade" />
          </view>
          <view
            v-if="needCollapse"
            class="detail-toggle"
            hover-class="tap-hover"
            @tap="expanded = !expanded"
          >
            <text>{{ expanded ? '收起' : '展开全部' }}</text>
          </view>
        </view>
      </view>

      <!-- 报名条件 -->
      <view class="band band-gray" v-if="conditionItems.length">
        <view class="floor-head"><text class="floor-title">报名条件</text></view>
        <view class="card card-list">
          <view
            v-for="(item, index) in conditionItems"
            :key="index"
            class="condition"
            :class="{ 'is-divided': index > 0 }"
          >
            <text v-if="conditionItems.length > 1" class="condition-index">{{ index + 1 }}</text>
            <text class="condition-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 学校（名称/地址/坐标 + 概况） -->
      <view class="band band-gray" v-if="hasCampus">
        <view class="floor-head"><text class="floor-title">学校</text></view>
        <view class="card card-venue">
          <text v-if="venue.campusName" class="venue-name">{{ venue.campusName }}</text>
          <view v-if="addressText" class="venue-row" hover-class="tap-hover" @tap="copyAddress">
            <text class="venue-row-label">地址</text>
            <text class="venue-row-value">{{ addressText }}</text>
            <text class="venue-row-hint">复制</text>
          </view>
          <view v-if="hasCoordinate" class="venue-row" hover-class="tap-hover" @tap="openLocation">
            <text class="venue-row-label">位置</text>
            <text class="venue-row-value">在地图中查看</text>
            <text class="venue-row-hint">导航</text>
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
          <view v-if="venue.campusIntro" class="venue-intro">
            <mp-html :content="campusIntroContent" :tag-style="RICH_TAG_STYLE" />
          </view>
        </view>
      </view>

      <!-- 教学条件 -->
      <view class="band band-gray" v-if="venue.classroomIntro">
        <view class="floor-head"><text class="floor-title">教学条件</text></view>
        <view class="card card-detail">
          <mp-html :content="classroomContent" :tag-style="RICH_TAG_STYLE" />
        </view>
      </view>

      <!-- 住宿条件 -->
      <view class="band band-gray" v-if="venue.accommodationIntro">
        <view class="floor-head"><text class="floor-title">住宿条件</text></view>
        <view class="card card-detail">
          <mp-html :content="accommodationContent" :tag-style="RICH_TAG_STYLE" />
        </view>
      </view>

      <!-- 退款政策 -->
      <view class="band band-gray" v-if="session.refundPolicyText">
        <view class="floor-head"><text class="floor-title">退款政策</text></view>
        <view class="card card-detail">
          <text class="policy-text">{{ session.refundPolicyText }}</text>
        </view>
      </view>

      <!-- 分区带只有上留白，末尾补一段，避免最后一张卡片贴住底部栏 -->
      <view class="ss-m-t-40" />

      <!-- 底部报名栏：placeholder 已预留栏高，这里只需补上方的间隙 -->
      <su-fixed bottom placeholder bg="bg-white">
        <view class="foot-box">
          <!-- 拼团模板：先讲清楚几人成团、优惠多少，失败按原价 -->
          <view v-if="session.isGrouponEnabled && !isFullPayment" class="foot-groupon">
            {{ session.grouponRequiredCount || 3 }} 人拼团，成团立减 ¥{{
              yuan(session.grouponDiscountAmount)
            }}，未成团按原价
          </view>
          <view class="foot-main">
            <view class="foot-price-box">
              <text class="foot-price">
                ¥{{ yuan(isFullPayment ? session.tuitionAmount : session.depositAmount) }}
              </text>
              <text class="foot-label">{{ isFullPayment ? '全款' : '定金' }}</text>
            </view>
            <button
              class="ss-reset-button enroll-btn"
              hover-class="tap-hover"
              @tap="
                goEnroll(
                  !isFullPayment && (session.isGrouponEnabled || !!shareParams.grouponInviteCode),
                )
              "
            >
              {{
                isFullPayment
                  ? '报名并支付全款'
                  : session.isGrouponEnabled || shareParams.grouponInviteCode
                  ? '拼团报名'
                  : '立即报名'
              }}
            </button>
          </view>
        </view>
      </su-fixed>
    </view>

    <s-empty v-else-if="!state.loading" text="班期不存在" icon="/static/data-empty.png" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive, ref } from 'vue';
  import { onLoad } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import SessionApi from '@/sheep/api/maritime/session';
  import {
    RICH_TAG_STYLE,
    normalizeRichText,
    parseConditionItems,
    fullAddress,
    yuan,
    seatPercent,
    sessionStatusText,
  } from '@/utils/maritime';

  /** 折叠阈值：按纯文本字数判断，避免带图的短文案被误判为长文 */
  const COLLAPSE_THRESHOLD = 320;

  const state = reactive({
    loading: false,
    session: {},
  });

  /** 分享链接带入的拼团邀请码：写入当前班期，报名时自动传递 */
  const shareParams = reactive({
    grouponInviteCode: '',
  });

  const expanded = ref(false);

  const session = computed(() => state.session);

  /** 场地信息可能整条缺失，统一兜底成空对象，模板里不必层层判空 */
  const venue = computed(() => state.session.venue || {});

  /** 学校地址：省市区 + 详细地址拼成完整地址，展示、复制、导航共用一份 */
  const addressText = computed(() => fullAddress(venue.value));

  /** 学校城市：优先取场地快照的 city；快照没填省市区时，从详细地址里取「XX市」兜底 */
  const cityText = computed(() => {
    const city = String(venue.value.city ?? '').trim();
    if (city) return city;
    const address = String(venue.value.address ?? '').trim();
    const match = address.replace(/^.*?(?:省|自治区)/, '').match(/^(.+?市)/);
    return match ? match[1] : '';
  });

  /** 另行缴费项目（到校报到时缴纳）：没配时整块不展示 */
  const extraFees = computed(() => state.session.extraFeeItems || []);

  /** 项目金额允许留空（到校才知道具体数额），只有填了才显示金额 */
  function hasAmount(item) {
    return item.amount !== null && item.amount !== undefined && item.amount !== '';
  }

  const groupons = computed(() => state.session.activeGroupons || []);

  const isOpen = computed(() => state.session.sessionStatus === 'OPEN');

  /** 定金为 0 或 ≥ 学费时按全款报名；全款模式不参与拼团 */
  const isFullPayment = computed(() => {
    const tuition = Number(state.session.tuitionAmount || 0);
    const deposit = Number(state.session.depositAmount || 0);
    return deposit <= 0 || (tuition > 0 && deposit >= tuition);
  });

  /** 标题位放班级名；没有班级名时退化为班期编号，再兜底课程名，保证标题不空 */
  const sessionTitle = computed(
    () => state.session.sessionName || state.session.sessionCode || state.session.courseName || '',
  );

  /** 课程名作为副标题展示 */
  const courseTitle = computed(() => state.session.courseName || '');

  const dateRangeText = computed(() => {
    const s = state.session;
    if (!s.startDate) return '';
    const days = s.durationDays ? ` · ${s.durationDays}天` : '';
    return `${s.startDate} ~ ${s.endDate}${days}`;
  });

  const seatText = computed(
    () => `余 ${state.session.remainingCount}/${state.session.maxStudents}`,
  );

  const conditionItems = computed(() => parseConditionItems(state.session.enrollmentConditionText));

  const detailContent = computed(() => normalizeRichText(state.session.description));
  const campusIntroContent = computed(() => normalizeRichText(venue.value.campusIntro));
  const accommodationContent = computed(() => normalizeRichText(venue.value.accommodationIntro));
  const classroomContent = computed(() => normalizeRichText(venue.value.classroomIntro));

  /** 经纬度必须成对才可用于导航，只配了一个视为未配置 */
  const hasCoordinate = computed(
    () =>
      Number.isFinite(toCoordinate(venue.value.latitude)) &&
      Number.isFinite(toCoordinate(venue.value.longitude)),
  );

  /**
   * 学校整块是否展示。
   * 必须逐字段判断，不能对 venue 做 Object.values().some()——
   * 接口返回的 id/sessionId 恒为真值，那样判断会恒成立
   */
  const hasCampus = computed(
    () =>
      hasCoordinate.value ||
      !!addressText.value ||
      ['campusName', 'contactPhone', 'campusIntro'].some((key) =>
        String(venue.value[key] ?? '').trim(),
      ),
  );

  /** 长文才折叠：纯文本字数超阈值才给「展开全部」 */
  const needCollapse = computed(() => {
    const plain = String(state.session.description || '')
      .replace(/<[^>]*>/g, '')
      .replace(/\s+/g, '');
    return plain.length > COLLAPSE_THRESHOLD;
  });

  // 拼成整串再插值：<text> 里首字符若是汉字，
  // 模板换行会被折叠成一个前导空格，把这一行顶得比同组其它行靠右
  function grouponText(groupon) {
    return `已有 ${groupon.currentCount}/${groupon.requiredCount} 人，还差 ${groupon.remainingCount} 人成团`;
  }

  function grouponExpireText(groupon) {
    const time = sheep.$helper.timeFormat(groupon.expireTime, 'mm-dd hh:MM');
    return time ? `${time} 截止` : '';
  }

  function copyAddress() {
    sheep.$helper.copyText(addressText.value);
  }

  /** 后端经纬度是 BigDecimal，序列化后可能是字符串，转数字后再交给地图；非法值返回 NaN */
  function toCoordinate(value) {
    if (value === undefined || value === null || value === '') return NaN;
    return Number(value);
  }

  /** 打开微信内置地图查看学校位置，并可直接发起导航 */
  function openLocation() {
    if (!hasCoordinate.value) return;
    uni.openLocation({
      latitude: toCoordinate(venue.value.latitude),
      longitude: toCoordinate(venue.value.longitude),
      name: String(venue.value.campusName || ''),
      address: addressText.value,
      // 微信内置地图依赖腾讯位置服务，AppID/域名/key 未配置时会失败；
      // 失败时把原因打出来，并兜底提供「复制地址」，不打断用户行程
      fail: (err) => {
        console.error('[openLocation] 打开地图失败:', err);
        const address = addressText.value;
        uni.showModal({
          title: '打开地图失败',
          content: `${
            err?.errMsg || '当前环境不支持打开地图'
          }\n\n可复制学校地址，在地图 App 中搜索：\n${address || '未填写地址'}`,
          confirmText: address ? '复制地址' : '知道了',
          showCancel: !!address,
          success: (res) => {
            if (res.confirm && address) sheep.$helper.copyText(address);
          },
        });
      },
    });
  }

  function callCampus() {
    const phone = String(venue.value.contactPhone || '');
    if (phone) {
      uni.makePhoneCall({ phoneNumber: phone });
    }
  }

  async function getDetail(id) {
    state.loading = true;
    try {
      const { code, data } = await SessionApi.getSessionDetail(id);
      if (code !== 0) return;
      state.session = data;
      // 把邀请码写入当前班期，后续报名直接使用
      state.session.grouponInviteCode = shareParams.grouponInviteCode;
      expanded.value = false;
    } catch (e) {
      // 请求层已提示，这里只兜住异常避免未处理 Promise
    } finally {
      state.loading = false;
    }
  }

  function goEnroll(joinGroupon, inviteCode) {
    if (!isOpen.value) {
      sheep.$helper.toast('当前班期不在招生状态');
      return;
    }
    // 全款报名没有定金，拼团不适用，兜底强制关闭
    if (isFullPayment.value) {
      joinGroupon = false;
    }
    // 优先使用拼团列表传入的邀请码，其次使用分享链接带入的邀请码
    const effectiveInviteCode = inviteCode || shareParams.grouponInviteCode || '';
    if (effectiveInviteCode && !isFullPayment.value) {
      joinGroupon = true;
    }
    const query = { sessionId: state.session.id };
    if (joinGroupon) {
      query.joinGroupon = 'true';
      if (effectiveInviteCode) query.grouponInviteCode = effectiveInviteCode;
    }
    sheep.$router.go('/pages/maritime-sub/enrollment', query);
  }

  onLoad((options) => {
    shareParams.grouponInviteCode = options.grouponInviteCode || '';
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

  // ---- 拼团邀请提示 ----
  .invite-bar {
    padding: 20rpx $side;
    background: #fff7e6;

    .invite-bar-text {
      font-size: 24rpx;
      line-height: 1.5;
      color: #d46b08;
    }
  }

  // ---- 头图 ----
  .hero {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 560rpx;
    overflow: hidden;
    background: linear-gradient(135deg, #2e6bff, #0b3b8c);
  }

  // 半透明玻璃层：吸附在头图底部，左右通栏，只保留上方圆角
  .hero-overlay {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    padding: 26rpx $side 32rpx;
    border-radius: 32rpx 32rpx 0 0;
    background: rgba(10, 14, 22, 0.28);
    border-top: 1rpx solid rgba(255, 255, 255, 0.14);
    backdrop-filter: blur(16rpx);
    -webkit-backdrop-filter: blur(16rpx);

    .intro-title {
      color: $white;
      text-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.3);
    }

    .intro-cert {
      color: rgba(255, 255, 255, 0.72);
    }

    .intro-meta {
      display: flex;
      align-items: center;
      margin-top: 16rpx;
      font-size: 24rpx;
    }

    .intro-meta .intro-cert {
      margin-top: 0;
    }

    // 城市 + 地图导航入口：图标只在有经纬度时出现
    .intro-city {
      display: flex;
      align-items: center;
      margin-left: 14rpx;
    }

    .intro-city-text {
      color: rgba(255, 255, 255, 0.72);
    }

    .intro-map-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: 4rpx;
      padding: 6rpx 8rpx;
    }

    .intro-city-icon {
      font-size: 30rpx;
      color: rgba(255, 255, 255, 0.92);
    }

    .intro-session-name {
      color: rgba(255, 255, 255, 0.78);
    }

    .status-pill {
      color: rgba(255, 255, 255, 0.9);
      background: rgba(255, 255, 255, 0.16);

      &.is-open {
        color: $white;
        background: rgba(25, 190, 107, 0.85);
      }
    }
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

  // ---- 分区带：只有上留白，标题带白底、内容带浅灰 ----
  .band {
    padding: 64rpx $side 0;
    background: $white;

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

  .intro-session {
    display: flex;
    align-items: center;
    margin-top: 24rpx;
  }

  .intro-session-name {
    font-size: 28rpx;
    color: $ink-2;
    font-variant-numeric: tabular-nums;
  }

  .status-pill {
    margin-left: 16rpx;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    color: $ink-3;
    background: $band;
    flex-shrink: 0;

    // 只有「招生中」是可报名状态，其余状态统一灰色，
    // 避免用同一种强调色表达「能报」和「不能报」两种相反含义
    &.is-open {
      color: var(--ui-success);
      background: rgba(25, 190, 107, 0.08);
    }
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

  .card-facts {
    padding: 8rpx 32rpx;
  }

  .card-venue {
    padding: 32rpx;
  }

  .is-divided {
    border-top: 1rpx solid $line;
  }

  // ---- 事实行：左标签右取值 ----
  .fact {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
  }

  .fact + .fact {
    border-top: 1rpx solid $line;
  }

  .fact-label {
    width: 140rpx;
    flex-shrink: 0;
    font-size: 26rpx;
    color: $ink-3;
  }

  .fact-value {
    flex: 1;
    font-size: 28rpx;
    color: $ink;
    font-variant-numeric: tabular-nums;
  }

  .fact-price {
    font-size: 40rpx;
    font-weight: 600;
    letter-spacing: -0.02em;
  }

  // 学费那行要同时放价格与住宿标记，价格保持自身的字号
  .fact-price-row {
    display: flex;
    align-items: center;
  }

  // ---- 费用标记（含住宿 / 必须 / 可选）----
  // 只有「必须」与「含住宿」是需要留意的信息，用强调色；其余统一灰色，
  // 避免同一排标签里出现多种强调色互相抢注意力
  .tag {
    flex-shrink: 0;
    margin-left: 16rpx;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    font-size: 22rpx;
    color: $ink-3;
    background: $band;

    &.is-included,
    &.is-required {
      color: var(--ui-BG-Main);
      background: rgba(46, 107, 255, 0.08);
    }
  }

  .extra-fee {
    display: flex;
    align-items: center;
  }

  .extra-fee-amount {
    font-variant-numeric: tabular-nums;
  }

  // 另行缴费的说明行：点明这类费用不在线上收取
  .fact-note-text {
    flex: 1;
    font-size: 24rpx;
    color: $ink-3;
  }

  // ---- 余位进度 ----
  .seats {
    flex: 1;
    display: flex;
    align-items: center;
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

  // ---- 正在拼团 ----
  .groupon {
    display: flex;
    align-items: center;
    padding: 28rpx 0;
  }

  .groupon-main {
    flex: 1;
    min-width: 0;
  }

  .groupon-progress {
    display: block;
    font-size: 28rpx;
    color: $ink;
  }

  .groupon-expire {
    display: block;
    margin-top: 8rpx;
    font-size: 22rpx;
    color: $ink-3;
    font-variant-numeric: tabular-nums;
  }

  .join-btn {
    flex-shrink: 0;
    width: 176rpx;
    height: 64rpx;
    line-height: 64rpx;
    margin-left: 24rpx;
    font-size: 24rpx;
    color: #fff;
    background: linear-gradient(90deg, #f8a13c, #ff9900);
    border-radius: 32rpx;
  }

  // ---- 课程介绍折叠 ----
  .detail-wrap {
    position: relative;
  }

  .detail-body {
    &.is-clamped {
      max-height: 1100rpx;
      overflow: hidden;
    }
  }

  // 底部渐隐：提示下面还有内容
  .detail-fade {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 160rpx;
    background: linear-gradient(rgba(255, 255, 255, 0), #fff);
  }

  // 展开按钮放在折叠区之外，避免渐隐盖住点击区域
  .detail-toggle {
    margin-top: 16rpx;
    text-align: center;
    font-size: 26rpx;
    color: var(--ui-BG-Main);
  }

  // ---- 报名条件 ----
  .condition {
    display: flex;
    align-items: flex-start;
    padding: 24rpx 0;
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

  // ---- 学校 ----
  .venue-name {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    letter-spacing: -0.012em;
    line-height: 1.32;
    color: $ink;
  }

  .venue-row {
    display: flex;
    align-items: baseline;
    margin-top: 24rpx;
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

  .venue-intro {
    margin-top: 32rpx;
    padding-top: 32rpx;
    border-top: 1rpx solid $line;
  }

  // ---- 退款政策 ----
  .policy-text {
    font-size: 28rpx;
    line-height: 1.6;
    color: $ink-2;
  }

  // ---- 底部报名栏 ----
  .foot-box {
    display: flex;
    flex-direction: column;
    padding: 12rpx $side 16rpx;
  }

  .foot-groupon {
    margin-bottom: 8rpx;
    font-size: 22rpx;
    color: #f08c00;
    line-height: 1.4;
  }

  .foot-main {
    display: flex;
    align-items: center;
  }

  .foot-price-box {
    flex: 1;
    display: flex;
    align-items: baseline;
    min-width: 0;
  }

  .foot-price {
    font-size: 40rpx;
    font-weight: 600;
    letter-spacing: -0.02em;
    color: $ink;
    font-variant-numeric: tabular-nums;
  }

  .foot-label {
    margin-left: 8rpx;
    font-size: 22rpx;
    color: $ink-3;
  }

  .enroll-btn {
    flex-shrink: 0;
    width: 220rpx;
    height: 80rpx;
    line-height: 80rpx;
    font-size: 28rpx;
    color: #fff;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 40rpx;
  }
</style>
