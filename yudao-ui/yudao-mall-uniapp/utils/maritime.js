/**
 * 海员培训模块的公共展示逻辑
 *
 * 课程详情页与班期详情页都要渲染同一份课程富文本、同一份报名条件文本、
 * 同一套金额/余位展示，因此统一放在这里，避免两页各写一份。
 * 放在 utils/ 而非 sheep/：sheep/ 是商城框架代码，业务逻辑混进去
 * 会在后续同步上游时产生冲突。
 */

/**
 * mp-html 的 tag-style：给后台富文本统一排版。
 * 取值对齐首页 pages/maritime/index.vue 的设计令牌（字号/字重/行高/留白）。
 * 注意这里必须写死色值——tag-style 最终是行内样式，SCSS 变量够不到；
 * rpx 由 mp-html 内部按屏宽换算成 px，可以直接写。
 */
export const RICH_TAG_STYLE = {
  h1: 'font-size:40rpx;font-weight:600;letter-spacing:-0.02em;line-height:1.24;color:#1d1d1f;margin:48rpx 0 16rpx;',
  h2: 'font-size:36rpx;font-weight:600;letter-spacing:-0.016em;line-height:1.24;color:#1d1d1f;margin:48rpx 0 16rpx;',
  h3: 'font-size:32rpx;font-weight:600;letter-spacing:-0.012em;line-height:1.32;color:#1d1d1f;margin:48rpx 0 16rpx;',
  h4: 'font-size:28rpx;font-weight:600;letter-spacing:-0.01em;line-height:1.4;color:#1d1d1f;margin:32rpx 0 16rpx;',
  h5: 'font-size:26rpx;font-weight:600;line-height:1.4;color:#1d1d1f;margin:32rpx 0 16rpx;',
  p: 'font-size:28rpx;color:#6e6e73;line-height:1.6;margin:0 0 24rpx;',
  ul: 'margin:0 0 24rpx;padding-left:32rpx;',
  ol: 'margin:0 0 24rpx;padding-left:32rpx;',
  li: 'font-size:28rpx;color:#6e6e73;line-height:1.6;margin-bottom:16rpx;',
  strong: 'font-weight:600;color:#1d1d1f;',
  b: 'font-weight:600;color:#1d1d1f;',
  a: 'color:#1e5eff;',
  blockquote:
    'margin:24rpx 0;padding:24rpx 32rpx;border-left:4rpx solid #e5e5ea;background:#f5f5f7;border-radius:0 16rpx 16rpx 0;font-size:26rpx;color:#6e6e73;line-height:1.6;',
  img: 'max-width:100%;border-radius:28rpx;margin:24rpx 0;',
  table: 'width:100%;border-collapse:collapse;margin:24rpx 0;font-size:26rpx;color:#6e6e73;',
  th: 'padding:16rpx;border-bottom:1rpx solid #e5e5ea;font-weight:600;color:#1d1d1f;text-align:left;',
  td: 'padding:16rpx;border-bottom:1rpx solid #e5e5ea;',
  hr: 'border:none;border-top:1rpx solid #e5e5ea;margin:32rpx 0;',
};

/**
 * 后台编辑器里从网页粘贴正文时，会把网页自身的排版样式一起带进富文本，例如：
 *   <span style="color:rgba(0,0,0,.9);font-size:16px;font-family:-apple-system,…">
 * 行内样式优先级高于 tag-style，这些属性能把整段文字顶成网页的字体、字号、颜色，
 * 和页面其它文字对不上，所以渲染前统一摘掉，交回 RICH_TAG_STYLE 排版。
 * 只摘这几个排版属性，text-align、font-weight 等作者可能有意设置的样式保留。
 */
const PASTED_STYLE_PROPS = ['font-family', 'font-size', 'line-height', 'color', 'background-color'];

/**
 * 摘掉富文本行内样式里粘贴带进来的排版属性。
 *
 * 样式值里会出现 `&quot;` 这类 HTML 实体（粘贴时引号被转义了），
 * 实体本身就带着分号，直接按 `;` 拆声明会把 font-family 拦腰截断，
 * 所以先把实体换成占位符，拆完再还原。
 *
 * @param {string} html 后台富文本
 * @return {string} 处理后的富文本
 */
function stripPastedTypography(html) {
  return String(html || '').replace(
    // 要求 style 前是空白：既能吃掉属性删空后留下的空格，也不会误伤 data-style
    /(\s)style\s*=\s*(["'])(.*?)\2/gi,
    (match, space, quote, value) => {
      const entities = [];
      const shielded = String(value).replace(/&(?:[a-zA-Z]+|#\d+);/g, (entity) => {
        entities.push(entity);
        return `\u0000${entities.length - 1}\u0000`;
      });
      // 按声明逐条判断，而不是整段丢弃 style，避免误伤 width、text-align 等
      const kept = shielded
        .split(';')
        .filter((declaration) => {
          const name = declaration.split(':')[0].trim().toLowerCase();
          return name && !PASTED_STYLE_PROPS.includes(name);
        })
        .join(';')
        .replace(/\u0000(\d+)\u0000/g, (placeholder, index) => entities[Number(index)]);
      return kept.trim() ? `${space}style=${quote}${kept}${quote}` : '';
    },
  );
}

/**
 * 富文本首个块级标签的上边距会和卡片内边距叠加成双倍留白，
 * 这里给它补一条 margin-top:0。
 * mp-html 中节点自带的行内样式优先级高于 tag-style，所以能覆盖掉。
 *
 * @param {string} html 后台富文本
 * @return {string} 处理后的富文本
 */
export function flushFirstBlock(html) {
  return String(html || '').replace(
    /^\s*<(h[1-6]|p|ul|ol|blockquote|div)\b([^>]*)>/i,
    (match, tag, attrs) =>
      /\bstyle\s*=/i.test(attrs)
        ? match.replace(
            /\bstyle\s*=\s*(["'])(.*?)\1/i,
            (m, quote, value) => `style=${quote}${value};margin-top:0${quote}`,
          )
        : `<${tag}${attrs} style="margin-top:0">`,
  );
}

/**
 * 富文本交给 mp-html 渲染前的统一处理：先摘掉粘贴带进来的行内排版样式，
 * 让 RICH_TAG_STYLE 的字体、字号、颜色、行高生效，再修掉首块多出来的上边距。
 * 页面里请统一调这个，不要只调 flushFirstBlock。
 *
 * @param {string} html 后台富文本
 * @return {string} 处理后的富文本
 */
export function normalizeRichText(html) {
  return flushFirstBlock(stripPastedTypography(html));
}

/**
 * 报名条件：后台存的是「1. xxx；\n2. yyy」这类纯文本，
 * 逐行拆开、去掉序号与行尾标点，交给列表逐条排版。
 * 未编号的整段文字会原样作为一条返回。
 *
 * @param {string} text 报名条件文本
 * @return {string[]} 条件条目
 */
export function parseConditionItems(text) {
  if (!text) {
    return [];
  }
  return String(text)
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)
    .map((line) =>
      line
        .replace(/^\d{1,2}\s*[.、)）]\s*/, '')
        .replace(/^[-•·*]\s*/, '')
        .replace(/[；;。\s]+$/, '')
        .trim(),
    )
    .filter(Boolean);
}

/**
 * 学校完整地址：后台把地址拆成省 / 市 / 区 + 详细地址四段存放，
 * 展示与复制都需要拼成一整串。学校可能只填了其中几段，
 * 空段要跳过，否则会拼出多余的残留空格。
 * 相邻重复段也跳过：直辖市的省与市同名（如「北京市 / 北京市」），
 * 不去重会拼成「北京市北京市…」。
 *
 * @param {object} venue 班期详情里的学校信息，可能整条缺失
 * @return {string} 完整地址，一段都没有时返回空串
 */
export function fullAddress(venue) {
  if (!venue) {
    return '';
  }
  const parts = [venue.province, venue.city, venue.district, venue.address]
    .map((part) => String(part ?? '').trim())
    .filter(Boolean);
  return parts.filter((part, index) => part !== parts[index - 1]).join('');
}

/** 金额展示：整数不带小数，非整数保留两位 */
export function yuan(amount) {
  if (amount === null || amount === undefined || amount === '') {
    return '--';
  }
  const n = Number(amount);
  return Number.isInteger(n) ? String(n) : n.toFixed(2);
}

/** 余位进度：已报名 / 总名额 */
export function seatPercent(session) {
  if (!session || !session.maxStudents) {
    return 0;
  }
  const enrolled = session.enrolledCount || 0;
  return Math.min(100, Math.round((enrolled / session.maxStudents) * 100));
}

/** 拼团优惠金额（成团后从尾款抵扣），无优惠返回 0 */
export function grouponSave(session) {
  if (!session || !session.isGrouponEnabled) {
    return 0;
  }
  const discount = Number(session.grouponDiscountAmount || 0);
  return discount > 0 ? discount : 0;
}

/** 班期状态文案 */
const SESSION_STATUS_TEXT = {
  DRAFT: '草稿',
  OPEN: '招生中',
  FULL: '名额已满',
  CLOSED: '已截止',
  PENDING_START: '待开班',
  IN_PROGRESS: '进行中',
  FINISHED: '已结班',
  COMPLETED: '已结班',
  CANCELLED: '已取消',
};

/** 班期状态 → 中文文案，未知状态原样返回 */
export function sessionStatusText(status) {
  return SESSION_STATUS_TEXT[status] || status || '';
}

/**
 * 拼成整串再插值：<text> 里首字符若是汉字，
 * 模板换行会被折叠成一个前导空格，把这一行顶得比同组其它行靠右
 */
export function deadlineText(session) {
  return session?.enrollmentDeadline ? `报名截止 ${session.enrollmentDeadline}` : '';
}

export function grouponSaveText(session) {
  const save = grouponSave(session);
  return save ? `拼团立减 ¥${yuan(save)}` : '';
}
