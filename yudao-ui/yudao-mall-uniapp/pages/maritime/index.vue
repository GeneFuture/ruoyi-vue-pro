<!-- 首页
     视觉风格：Apple 式布局语言（大字号居中标题 / 分区带交替 / 图片大圆角 / 克制的强调色 / 发丝分隔线）
     楼层：品牌头 → 金刚区 → 近期开班 → 课程列表 → 我的消息 → 报名流程
     数据来源：GET /maritime/home/get 一次性聚合（平台统计 + 近期开班）；金刚区为页面静态入口
     本页样式对照 scripts/homepage-design-preview.html 的视觉稿实现，1px = 2rpx -->
<template>
  <s-layout
    title="海员培训"
    navbar="inner"
    navbar-immersive
    :bg-style="{ color: '#f5f5f7' }"
    :navbar-solid="state.navbarSolid"
    :navbar-title-visible="state.navbarSolid"
    :onShareAppMessage="shareInfo"
  >
    <!-- F0 品牌头：海面图片 + 深蓝遮罩，保证白字可读 -->
    <view class="hero" :style="{ paddingTop: navbarHeight + 'px' }">
      <image class="hero-bg" src="/static/hero-sea.jpg" mode="aspectFill" />
      <view class="hero-mask" />
      <text class="hero-title">持证上岗{{ '\n' }}从这里开始</text>
      <text class="hero-sub">STCW 四小证 · 适任证书 · 特种培训</text>

      <view class="hero-search">
        <text class="cicon-search hero-search-icon" @tap="onSearch" />
        <input
          v-model="state.query.keyword"
          class="hero-search-input"
          placeholder="搜索课程名称，如：三管轮"
          placeholder-class="hero-search-ph"
          confirm-type="search"
          maxlength="30"
          @confirm="onSearch"
        />
        <text v-if="state.query.keyword" class="cicon-close hero-search-clear" @tap="onClearSearch" />
      </view>
    </view>

    <!-- F1 金刚区：静态入口，不再由后台配置 -->
    <view class="band">
      <view class="menu-grid">
        <view
          v-for="item in QUICK_LINKS"
          :key="item.title"
          class="menu-cell"
          hover-class="tap-hover"
          @tap="navigateByTarget(item)"
        >
          <view class="menu-tile">
            <text :class="item.icon" class="menu-tile-glyph" />
          </view>
          <text class="menu-label">{{ item.title }}</text>
        </view>
      </view>
    </view>

    <!-- F2 近期开班：图片主导的大卡片横滑 -->
    <view v-if="state.home.upcomingSessions?.length" class="band">
      <view class="floor-head">
        <text class="floor-title">近期开班</text>
        <text class="floor-link" @tap="goSessionTab">全部 ›</text>
      </view>

      <scroll-view scroll-x class="rail" :show-scrollbar="false">
        <view class="rail-inner">
          <view
            v-for="session in state.home.upcomingSessions"
            :key="session.id"
            class="session"
            hover-class="tap-hover"
            @tap="goSession(session.id)"
          >
            <view class="session-cover">
              <image
                v-if="session.coverImage"
                class="session-cover-img"
                :src="session.coverImage"
                mode="aspectFill"
                lazy-load
              />
              <!-- 无封面时展示证书类型：不要重复课程名，卡片标题就在下面 -->
              <text v-else class="session-cover-cert">{{ session.certificateType }}</text>
              <text class="session-countdown">{{ countdownText(session.startDate) }}</text>
            </view>

            <view class="session-body">
              <text class="session-name ss-line-2">{{ session.courseName }}</text>
              <!-- 班期卡承担「什么时候 / 在哪 / 多少钱」，开班日期从课程卡移到这里 -->
              <text class="session-meta ss-line-1">
                {{ session.sessionName || session.sessionCode }} · {{ shortDate(session.startDate) }} 开班
              </text>
              <text class="session-meta ss-line-1">{{ sessionMetaText(session) }}</text>

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
                拼团立减 ¥{{ yuan(grouponSave(session)) }}
              </text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- F3 课程列表：发丝分隔的扁平列表 -->
    <view id="courseFloor" class="band">
      <view class="floor-head">
        <text class="floor-title">全部课程</text>
      </view>

      <!-- 筛选 chips：证书类型取自已加载课程的实际上架类型，避免文本输入查不到 -->
      <scroll-view
        v-if="certificateChips.length > 1"
        scroll-x
        class="chips"
        :show-scrollbar="false"
      >
        <view class="chips-inner">
          <text
            v-for="chip in certificateChips"
            :key="chip.value || 'all'"
            class="chip"
            :class="{ 'chip-active': state.query.certificateType === chip.value }"
            @tap="onSelectCert(chip.value)"
          >
            {{ chip.label }}
          </text>
        </view>
      </scroll-view>

      <!-- 首屏骨架 -->
      <view v-if="state.firstLoading">
        <view v-for="n in 3" :key="n" class="course">
          <view class="course-thumb skeleton-block" />
          <view class="course-main">
            <view class="skeleton-line" style="width: 72%" />
            <view class="skeleton-line" style="width: 42%; margin-top: 20rpx" />
            <view class="skeleton-line" style="width: 56%; margin-top: 20rpx" />
          </view>
        </view>
      </view>

      <template v-else>
        <s-empty
          v-if="state.loadError"
          text="课程加载失败，请检查网络"
          icon="/static/internet-empty.png"
          showAction
          actionText="重新加载"
          @clickAction="resetAndLoad"
        />

        <view v-else>
          <view
            v-for="item in state.pagination.list"
            :key="item.id"
            class="course"
            hover-class="tap-hover"
            @tap="goCourse(item)"
          >
            <image class="course-thumb" :src="item.coverImage" mode="aspectFill" lazy-load />
            <!-- 课程卡只展示课程本身：开班日期 / 上课地点 / 学费属于班期信息，
                 统一由「近期开班」楼层和课程详情页承担，避免两张卡讲同一件事 -->
            <view class="course-main">
              <text class="course-name ss-line-2">{{ item.name }}</text>
              <view class="course-foot">
                <text class="course-cert">{{ item.certificateType }}</text>
                <text
                  v-if="item.sessionStatus"
                  class="course-status"
                  :class="{ 'course-status-open': item.sessionStatus === 'OPEN' }"
                >
                  {{ sessionStatusText(item.sessionStatus) }}
                </text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空态：区分「筛选无结果」与「全站无课程」，并给出下一步 -->
        <s-empty
          v-if="!state.loading && state.pagination.list.length === 0"
          :text="hasFilter ? '没有符合条件的课程，换个条件试试' : '暂无课程'"
          icon="/static/data-empty.png"
          :showAction="hasFilter"
          actionText="清除筛选"
          @clickAction="onClearFilter"
        />

        <uni-load-more
          v-if="state.pagination.list.length > 0"
          :status="state.loadStatus"
          :content-text="{
            contentdown: '上拉加载更多',
            contentrefresh: '正在加载...',
            contentnomore: '没有更多了',
          }"
        />
      </template>
    </view>

    <!-- F4 我的消息：最近 3 条，未登录或没有消息时整块隐藏 -->
    <view v-if="state.recentMessages.length" class="band">
      <view class="floor-head">
        <text class="floor-title">我的消息</text>
        <text class="floor-link" @tap="goMessageCenter">全部 ›</text>
      </view>
      <view class="notices">
        <view
          v-for="msg in state.recentMessages"
          :key="msg.id"
          class="notice"
          hover-class="tap-hover"
          @tap="goMessageCenter"
        >
          <view class="notice-main">
            <text class="notice-title ss-line-1">{{ messagePreview(msg) }}</text>
          </view>
          <view class="notice-right">
            <view v-if="!msg.readStatus" class="notice-dot" />
            <text class="notice-time">{{ sheep.$helper.timeFormat(msg.createTime, 'mm-dd hh:MM') }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- F5 报名流程（静态引导）
         注意：课程列表（F3）是无限分页的，本楼层排在它之后，
         用户需滚完已加载的全部课程才能看到。 -->
    <view class="band band-last">
      <view class="floor-head">
        <text class="floor-title">报名流程</text>
      </view>

      <view class="flow">
        <view v-for="(step, index) in FLOW_STEPS" :key="step.title" class="flow-step">
          <text class="flow-node">{{ index + 1 }}</text>
          <text class="flow-step-title">{{ step.title }}</text>
          <text class="flow-step-desc">{{ step.desc }}</text>
        </view>
      </view>

      <text class="flow-note">{{ FLOW_NOTE }}</text>
    </view>

    <view class="ss-safe-bottom" />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onShow, onPullDownRefresh, onReachBottom, onPageScroll } from '@dcloudio/uni-app';
  import { concat } from 'lodash-es';
  import sheep from '@/sheep';
  import CourseApi from '@/sheep/api/maritime/course';
  import HomeApi from '@/sheep/api/maritime/home';
  import NotifyApi from '@/sheep/api/maritime/notify';
  import { grouponSave, seatPercent, sessionStatusText, yuan } from '@/utils/maritime';

  /** 导航栏高度（状态栏 + 44px）。沉浸式头图需要自己让出这段高度 */
  const navbarHeight = sheep.$platform.navbar;

  /**
   * 报名流程四步（静态文案）
   *
   * 口径依据 EnrollmentStatusEnum 与 docs/开发任务/T07-尾款支付与退费.md：
   *   报名(PENDING_DEPOSIT) → 付定金(DEPOSITED) → 付清尾款(PAID_OFF) → 结班(COMPLETED)
   * 尾款 = 学费总额 − 已付定金，到期日由各期 fee_config.balanceDueDaysBeforeStart 单独配置，
   * 因此文案里不写死具体天数。
   */
  const FLOW_STEPS = [
    { title: '选班期', desc: '挑时间地点' },
    { title: '付定金', desc: '锁定名额' },
    { title: '付尾款', desc: '开班前结清' },
    { title: '参加培训', desc: '结业拿证' },
  ];

  /** 首页金刚区静态入口：首页配置功能已下线，入口固定在页面代码中维护 */
  const QUICK_LINKS = [
    { title: '找课程', targetType: 'courseList', targetUrl: '', icon: 'cicon-search' },
    { title: '开班计划', targetType: 'page', targetUrl: '/pages/maritime/sessions', icon: 'cicon-time' },
    {
      title: '我的报名',
      targetType: 'page',
      targetUrl: '/pages/maritime-sub/enrollment-list',
      icon: 'cicon-order',
    },
  ];

  /**
   * 退费口径（易引发客诉，务必与后端一致）：
   * PENDING_DEPOSIT 未付定金可直接取消，不走退费流程；
   * DEPOSITED / PAID_OFF 可申请退费；COMPLETED 结班后不支持退费。
   */
  const FLOW_NOTE = '未付定金可直接取消；开班前可申请退费，开班后不支持';

  const state = reactive({
    /** 首屏骨架：仅首次加载为 true，下拉刷新时不闪骨架 */
    firstLoading: true,
    /** 沉浸式导航是否已滚动到需要白色背景的位置 */
    navbarSolid: false,
    loading: false,
    loadError: false,
    loadStatus: '',
    home: {},
    /** 最近 3 条站内消息；为空时首页整个消息楼层不展示 */
    recentMessages: [],
    /** 见过的证书类型，用于筛选 chips；只增不减，避免筛选后选项塌缩 */
    certTypes: [],
    query: {
      certificateType: '',
      keyword: '',
    },
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
  });

  const hasFilter = computed(() => !!state.query.certificateType || !!state.query.keyword);

  /**
   * 证书类型 chips：从课程数据里累积去重得到，保证点了一定有结果。
   *
   * 注意不能直接从 pagination.list 推导 —— 筛选后 list 只剩当前类型，
   * chips 会塌缩成一个选项导致无法切回其他类型。因此单独累积、不随筛选清空。
   */
  const certificateChips = computed(() => {
    if (state.certTypes.length === 0) return [];
    return [
      { label: '全部', value: '' },
      ...state.certTypes.map((t) => ({ label: t, value: t })),
    ];
  });

  /** 开班日期简写：2026-09-27 → 09-27 */
  function shortDate(date) {
    if (!date) return '';
    return String(date).slice(5);
  }

  /** 开班倒计时文案 */
  function countdownText(startDate) {
    if (!startDate) return '';
    const days = Math.ceil((new Date(startDate).getTime() - Date.now()) / (24 * 60 * 60 * 1000));
    if (days <= 0) return '今日开班';
    if (days === 1) return '明日开班';
    return `${days} 天后开班`;
  }

  /** 班期卡第二行：城市 + 天数，城市为空时不留悬空的分隔符 */
  function sessionMetaText(session) {
    const parts = [];
    if (session.city) parts.push(session.city);
    if (session.durationDays) parts.push(`${session.durationDays}天`);
    return parts.join(' · ');
  }

  /** 统一跳转协议：金刚区与 Banner 共用 */
  /** 金刚区静态入口跳转 */
  function navigateByTarget(item) {
    const { targetType, targetUrl } = item;
    switch (targetType) {
      case 'course':
        sheep.$router.go('/pages/maritime-sub/course-detail', { id: targetUrl });
        break;
      case 'session':
        sheep.$router.go('/pages/maritime-sub/session-detail', { id: targetUrl });
        break;
      case 'page':
        sheep.$router.go(targetUrl);
        break;
      case 'h5':
        sheep.$router.go('/pages/public/webview', { url: targetUrl });
        break;
      case 'courseList':
        state.query.certificateType = targetUrl || '';
        resetAndLoad();
        uni.pageScrollTo({ selector: '#courseFloor', duration: 300 });
        break;
      default:
        break;
    }
  }

  async function getHome() {
    try {
      const { code, data } = await HomeApi.getHome();
      if (code !== 0) return;
      state.home = data || {};
    } catch (e) {
      // 首页配置失败不阻塞课程列表
    }
  }

  /** 首页消息预览：优先展示消息内容，超过 12 个字截断 */
  function messagePreview(msg) {
    const content = String(msg.templateContent || msg.templateNickname || '系统通知')
      .replace(/\s+/g, ' ')
      .trim();
    const chars = Array.from(content);
    return chars.length > 12 ? `${chars.slice(0, 12).join('')}…` : content;
  }

  /** 最近 3 条站内消息；未登录或接口异常都按「没有消息」处理，楼层整体隐藏 */
  async function getRecentMessages() {
    if (!sheep.$store('user').isLogin) {
      state.recentMessages = [];
      return;
    }
    const { code, data } = await NotifyApi.getNotifyMessagePage({
      pageNo: 1,
      pageSize: 3,
      templateCodePrefix: 'MARITIME_',
    }).catch(() => ({}));
    state.recentMessages = code === 0 ? (data?.list || []).slice(0, 3) : [];
  }

  let listRequestSeq = 0;

  async function getList({ reset = false } = {}) {
    // 翻页时不并发请求；筛选/搜索重置时允许打断上一页请求
    if (state.loading && !reset) return;
    const requestSeq = ++listRequestSeq;
    state.loading = true;
    state.loadStatus = 'loading';
    try {
      const pageNo = reset ? 1 : state.pagination.pageNo + 1;
      const { code, data } = await CourseApi.getCourseList({
        pageNo,
        pageSize: state.pagination.pageSize,
        certificateType: state.query.certificateType || undefined,
        keyword: state.query.keyword.trim() || undefined,
      });
      if (requestSeq !== listRequestSeq) return;
      if (code !== 0) {
        state.loadError = true;
        state.loadStatus = 'more';
        return;
      }
      state.loadError = false;
      const list = data?.list || [];
      // 累积证书类型供筛选 chips 使用（只增不减）
      list.forEach((item) => {
        if (item.certificateType && !state.certTypes.includes(item.certificateType)) {
          state.certTypes.push(item.certificateType);
        }
      });
      state.pagination.pageNo = pageNo;
      state.pagination.list = reset ? list : concat(state.pagination.list, list);
      state.pagination.total = data?.total || 0;
      state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    } catch (e) {
      if (requestSeq !== listRequestSeq) return;
      state.loadError = true;
      state.loadStatus = 'more';
    } finally {
      if (requestSeq === listRequestSeq) {
        state.loading = false;
        state.firstLoading = false;
      }
    }
  }

  function resetAndLoad() {
    state.pagination.list = [];
    state.pagination.total = 0;
    state.pagination.pageNo = 0;
    state.loadStatus = '';
    state.loadError = false;
    getList({ reset: true });
  }

  function onSearch() {
    uni.hideKeyboard();
    resetAndLoad();
  }

  function onClearSearch() {
    state.query.keyword = '';
    resetAndLoad();
  }

  function onSelectCert(value) {
    if (state.query.certificateType === value) return;
    state.query.certificateType = value;
    resetAndLoad();
  }

  function onClearFilter() {
    state.query.certificateType = '';
    state.query.keyword = '';
    resetAndLoad();
  }

  /**
   * 课程卡 → 课程详情。
   * 卡片只讲课程本身（不含班期信息），去向与内容保持一致：先看课程，再在详情页里选班期。
   */
  function goCourse(item) {
    sheep.$router.go('/pages/maritime-sub/course-detail', { id: item.id });
  }

  function goSession(id) {
    sheep.$router.go('/pages/maritime-sub/session-detail', { id });
  }

  function goSessionTab() {
    sheep.$router.go('/pages/maritime/sessions');
  }

  function goMessageCenter() {
    // 消息中心是 tabBar 页，sheep.$router 内部会自动改用 switchTab
    sheep.$router.go('/pages/maritime/notify');
  }

  // 分享首页
  const shareInfo = computed(() => ({
    title: '海员培训 · 持证上岗，快速开班',
    forward: {
      path: 'pages/maritime/index',
    },
  }));

  onPageScroll((e) => {
    // 首页是沉浸式头图：一滚动就淡入白色导航背景，避免导航文字与头图文字重叠
    state.navbarSolid = e.scrollTop > 8;
  });

  onLoad(() => {
    getHome();
    getRecentMessages();
    getList({ reset: true });
  });

  onShow(() => {
    // 从班期详情返回时，余位可能已变化；消息也同步刷新（可能刚在消息中心读过）
    if (!state.firstLoading) {
      getHome();
      getRecentMessages();
    }
  });

  onPullDownRefresh(() => {
    Promise.all([getHome(), getRecentMessages(), getList({ reset: true })]).finally(() =>
      uni.stopPullDownRefresh(),
    );
  });

  onReachBottom(() => {
    if (state.loadStatus === 'noMore' || state.loading) return;
    getList();
  });
</script>

<style lang="scss" scoped>
  // ============================================================
  //  设计令牌（对照 scripts/homepage-design-preview.html）
  //  仅作用于本页；方向确认后可提升为全局主题 token
  // ============================================================
  $ink: #1d1d1f; // 主文字：Apple 近黑，不用纯黑
  $ink-2: #6e6e73; // 次要文字
  $ink-3: #86868b; // 说明 / 占位
  $line: #e5e5ea; // 发丝分隔线
  $band: #f5f5f7; // Apple 浅灰画布 / 次级卡片
  $white: #ffffff;
  $radius: 32rpx; // 统一卡片圆角
  $side: 32rpx; // 卡片内边距
  $gap: 24rpx; // 卡片外边距

  // 点击反馈：小程序对 <view> 的 :active 支持不可靠，统一用 hover-class
  .tap-hover {
    opacity: 0.72;
  }

  // ---- 统一卡片容器：灰画布上的白色圆角卡片 ----
  .band {
    margin: $gap $gap 0;
    padding: $side;
    background: $white;
    border-radius: $radius;
    border: 1rpx solid rgba(29, 29, 31, 0.04);
    box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.04);
  }

  .band-last {
    // 原生 tabBar 会覆盖页面底部，预留 tabBar 高度
    margin-bottom: 120rpx;
  }

  // ---- 楼层标题 ----
  .floor-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    margin-bottom: 28rpx;
  }

  .floor-title {
    font-size: 46rpx;
    font-weight: 600;
    letter-spacing: -0.028em;
    line-height: 1.18;
    color: $ink;
  }

  // Apple 式文字链接：强调色 + 右箭头，不带按钮外观
  .floor-link {
    padding: 8rpx 0 8rpx 24rpx;
    font-size: 27rpx;
    font-weight: 500;
    color: var(--ui-BG-Main);
  }

  // ============================================================
  //  F0 品牌头
  // ============================================================
  .hero {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 $gap 56rpx;
    border-radius: 0 0 $radius $radius;
    overflow: hidden;
    // 图片加载前的兜底背景
    background: #0a2f7a;
  }

  .hero-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 0;
  }

  // 深蓝渐变遮罩：压住海面高光，保证白色标题/搜索框的对比度
  .hero-mask {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 0;
    background:
      linear-gradient(
        180deg,
        rgba(7, 38, 100, 0.38) 0%,
        rgba(10, 60, 150, 0.18) 36%,
        rgba(8, 45, 120, 0.3) 62%,
        rgba(4, 24, 70, 0.48) 100%
      ),
      radial-gradient(120% 78% at 84% -12%, rgba(255, 255, 255, 0.16), transparent 62%);
  }

  .hero-title,
  .hero-sub,
  .hero-search {
    position: relative;
    z-index: 1;
  }

  .hero-title {
    margin-top: 36rpx;
    font-size: 64rpx;
    font-weight: 600;
    letter-spacing: -0.032em;
    line-height: 1.18;
    color: #fff;
    text-align: center;
    white-space: pre-line; // 保留模板里的换行
    text-shadow: 0 4rpx 28rpx rgba(0, 16, 50, 0.28);
  }

  .hero-sub {
    margin-top: 20rpx;
    font-size: 28rpx;
    font-weight: 400;
    letter-spacing: 0.015em;
    color: rgba(255, 255, 255, 0.76);
  }

  .hero-search {
    display: flex;
    align-items: center;
    width: 100%;
    box-sizing: border-box;
    height: 92rpx;
    margin-top: 44rpx;
    padding: 0 30rpx;
    border-radius: 46rpx;
    background: rgba(255, 255, 255, 0.96);
    border: 1rpx solid rgba(255, 255, 255, 0.5);
    box-shadow:
      0 14rpx 36rpx rgba(0, 18, 60, 0.18),
      inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  }

  .hero-search-icon {
    font-size: 36rpx;
    color: $ink-3;
    flex-shrink: 0;
  }

  .hero-search-input {
    flex: 1;
    margin-left: 16rpx;
    font-size: 30rpx;
    color: $ink;
  }

  .hero-search-ph {
    color: $ink-3;
  }

  .hero-search-clear {
    padding: 16rpx;
    font-size: 30rpx;
    color: $ink-3;
    flex-shrink: 0;
  }

  // ============================================================
  //  F1 金刚区（静态入口）
  // ============================================================
  .menu-grid {
    display: flex;
    justify-content: space-between;
    padding: 2rpx 0 4rpx;
  }

  .menu-cell {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 33.3333%;
    padding: 0 8rpx;
    box-sizing: border-box;
  }

  .menu-tile {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 120rpx;
    height: 120rpx;
    border-radius: 28rpx;
    background: linear-gradient(180deg, #fbfbfd 0%, #f0f2f7 100%);
    border: 1rpx solid rgba(29, 29, 31, 0.04);
    box-shadow:
      0 10rpx 24rpx rgba(16, 24, 40, 0.06),
      inset 0 2rpx 0 rgba(255, 255, 255, 0.9);
  }

  .menu-tile-glyph {
    font-size: 56rpx;
    color: var(--ui-BG-Main);
  }

  .menu-label {
    margin-top: 16rpx;
    font-size: 26rpx;
    font-weight: 500;
    letter-spacing: 0.01em;
    color: $ink-2;
    text-align: center;
  }

  // ============================================================
  //  F2 近期开班
  // ============================================================
  .rail {
    margin: 0 (-$side);
    white-space: nowrap;
    scroll-snap-type: x proximity;
  }

  .rail-inner {
    display: inline-flex;
    padding: 0 $side 8rpx;
  }

  .session {
    display: inline-flex;
    flex-direction: column;
    width: 520rpx;
    margin-right: 20rpx;
    border-radius: 28rpx;
    background: $band;
    overflow: hidden;
    flex-shrink: 0;
    white-space: normal;
    scroll-snap-align: start;
    border: 1rpx solid rgba(29, 29, 31, 0.04);
  }

  .session-cover {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 296rpx;
    background: linear-gradient(135deg, #2e6bff, #0b3b8c);
  }

  .session-cover-img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .session-cover-cert {
    font-size: 48rpx;
    font-weight: 600;
    letter-spacing: 0.04em;
    color: rgba(255, 255, 255, 0.92);
    text-align: center;
    padding: 0 40rpx;
  }

  // 底部遮罩：让倒计时压图后依然可读
  .session-cover::after {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 128rpx;
    background: linear-gradient(rgba(0, 0, 0, 0), rgba(0, 0, 0, 0.42));
  }

  .session-countdown {
    position: absolute;
    z-index: 1;
    left: 20rpx;
    bottom: 18rpx;
    padding: 7rpx 16rpx;
    border-radius: 999rpx;
    background: rgba(0, 0, 0, 0.26);
    border: 1rpx solid rgba(255, 255, 255, 0.18);
    font-size: 22rpx;
    font-weight: 500;
    letter-spacing: 0.015em;
    color: #fff;
  }

  .session-body {
    padding: 30rpx;
  }

  .session-name {
    display: block;
    font-size: 33rpx;
    font-weight: 600;
    letter-spacing: -0.018em;
    line-height: 1.3;
    color: $ink;
  }

  // 班期卡有两行 meta（班级名+开班日期 / 城市+天数），行内收紧、与标题拉开
  .session-meta {
    display: block;
    margin-top: 8rpx;
    font-size: 24rpx;
    color: $ink-2;
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
    height: 6rpx;
    border-radius: 3rpx;
    background: $line;
    overflow: hidden;
  }

  .seats-fill {
    height: 100%;
    border-radius: 3rpx;
    background: linear-gradient(90deg, var(--ui-BG-Main), #6fa2ff);
  }

  .seats-text {
    margin-left: 14rpx;
    font-size: 22rpx;
    color: $ink-2;
    flex-shrink: 0;
    font-variant-numeric: tabular-nums;
  }

  .session-foot {
    display: flex;
    align-items: baseline;
    margin-top: 22rpx;
  }

  .price {
    font-size: 42rpx;
    font-weight: 600;
    letter-spacing: -0.03em;
    color: $ink;
    font-variant-numeric: tabular-nums;
  }

  .deposit {
    margin-left: 14rpx;
    font-size: 24rpx;
    color: $ink-2;
    font-variant-numeric: tabular-nums;
  }

  .session-save {
    display: inline-block;
    margin-top: 10rpx;
    padding: 5rpx 14rpx;
    border-radius: 999rpx;
    background: rgba(255, 59, 48, 0.08);
    font-size: 23rpx;
    font-weight: 500;
    color: var(--ui-price);
  }

  // ============================================================
  //  F3 课程列表
  // ============================================================
  .chips {
    margin: 0 (-$side) 10rpx;
    white-space: nowrap;
  }

  .chips-inner {
    display: inline-flex;
    padding: 0 $side 22rpx;
  }

  .chip {
    height: 62rpx;
    line-height: 62rpx;
    padding: 0 30rpx;
    margin-right: 14rpx;
    border-radius: 31rpx;
    font-size: 27rpx;
    font-weight: 500;
    color: $ink;
    background: #f2f2f7;
    border: 1rpx solid transparent;
    flex-shrink: 0;
    transition: background 0.18s ease, color 0.18s ease;

    &-active {
      color: #fff;
      background: $ink; // Apple 的选中态用近黑，而非主题色
      border-color: $ink;
    }
  }

  .course {
    position: relative;
    display: flex;
    align-items: center;
    padding: 30rpx 0;
  }

  // 分隔线内缩到文字左边缘（176rpx 缩略图 + 28rpx 间距），不通栏
  .course + .course::before {
    content: '';
    position: absolute;
    top: 0;
    left: 204rpx;
    right: 0;
    height: 1rpx;
    background: $line;
  }

  .course-thumb {
    width: 176rpx;
    height: 176rpx;
    border-radius: 28rpx;
    background: $band;
    border: 1rpx solid rgba(29, 29, 31, 0.04);
    flex-shrink: 0;
  }

  // 纵向排列 + foot 的 margin-top:auto，让状态角标始终贴右下角，
  // 标题一行或两行时整行高度都保持一致
  .course-main {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    margin-left: 28rpx;
  }

  .course-name {
    display: block;
    font-size: 32rpx;
    font-weight: 600;
    letter-spacing: -0.018em;
    line-height: 1.34;
    color: $ink;
  }

  .course-foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: auto;
    padding-top: 14rpx;
  }

  .course-cert {
    padding: 5rpx 14rpx;
    border-radius: 999rpx;
    background: #f5f5f7;
    font-size: 22rpx;
    color: $ink-2;
  }

  // 招生中给绿色，其余状态（名额已满 / 已截止 / 已结班）统一用灰色，
  // 避免用同一种强调色表达"可报名"和"不能报名"两种相反含义
  .course-status {
    font-size: 23rpx;
    font-weight: 500;
    color: $ink-3;

    &-open {
      color: var(--ui-success);
    }
  }

  // ---- 骨架屏 ----
  .skeleton-block,
  .skeleton-line {
    background: linear-gradient(90deg, #f0f0f2 25%, #e6e6e9 37%, #f0f0f2 63%);
    background-size: 400% 100%;
    animation: skeleton-loading 1.4s ease infinite;
  }

  .skeleton-line {
    height: 26rpx;
    border-radius: 6rpx;
  }

  @keyframes skeleton-loading {
    0% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0 50%;
    }
  }

  // ============================================================
  //  F4 我的消息
  // ============================================================
  .notices {
    border-radius: 28rpx;
    background: $band;
    padding: 0 28rpx;
    border: 1rpx solid rgba(29, 29, 31, 0.04);
  }

  .notice {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 106rpx;
  }

  .notice + .notice {
    border-top: 1rpx solid $line;
  }

  .notice-main {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
  }

  /* 未读小红点 */
  .notice-dot {
    width: 10rpx;
    height: 10rpx;
    margin-right: 12rpx;
    border-radius: 50%;
    background: #ff3b30;
    box-shadow: 0 0 0 4rpx rgba(255, 59, 48, 0.08);
    flex-shrink: 0;
  }

  .notice-title {
    flex: 1;
    font-size: 29rpx;
    color: $ink;
  }

  /* 右侧：未读红点 + 时间 */
  .notice-right {
    display: flex;
    align-items: center;
    margin-left: 28rpx;
    flex-shrink: 0;
  }

  .notice-time {
    margin-left: 0;
    font-size: 24rpx;
    color: $ink-3;
    flex-shrink: 0;
    font-variant-numeric: tabular-nums;
  }

  // ============================================================
  //  F5 报名流程
  // ============================================================
  .flow {
    display: flex;
    padding: 28rpx 12rpx 26rpx;
    border-radius: 28rpx;
    background: $band;
    border: 1rpx solid rgba(29, 29, 31, 0.04);
  }

  .flow-step {
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  // 连接线：从前一步圆心右侧连到当前步圆心左侧
  .flow-step + .flow-step::before {
    content: '';
    position: absolute;
    top: 28rpx;
    right: calc(50% + 46rpx);
    left: calc(-50% + 46rpx);
    height: 1rpx;
    background: #d1d1d6;
  }

  .flow-node {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    // 流程是结构信息不是操作入口，用近黑而非强调色
    background: $ink;
    color: #fff;
    font-size: 25rpx;
    font-weight: 600;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }

  .flow-step-title {
    margin-top: 20rpx;
    font-size: 25rpx;
    font-weight: 600;
    color: $ink;
  }

  .flow-step-desc {
    margin-top: 6rpx;
    font-size: 21rpx;
    color: $ink-3;
    text-align: center;
  }

  .flow-note {
    display: block;
    margin-top: 28rpx;
    margin-bottom: 8rpx;
    font-size: 23rpx;
    line-height: 1.65;
    color: $ink-2;
    text-align: center;
  }
</style>
