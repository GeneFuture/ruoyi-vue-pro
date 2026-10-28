import $platform from '@/sheep/platform';
import $url from '@/sheep/url';

// #ifdef H5
import $wxsdk from '@/sheep/libs/sdk-h5-weixin';
// #endif

/**
 * 默认分享信息：海员培训首页
 */
const getShareInfo = (scene = {}) => ({
  title: scene.title || '海员培训',
  image: $url.cdn(scene.image || ''),
  forward: {
    path: 'pages/maritime/index',
  },
});

/**
 * 更新公众号分享 SDK 信息
 */
const updateShareInfo = (shareInfo) => {
  // #ifdef H5
  if ($platform.name === 'WechatOfficialAccount') {
    $wxsdk.updateShareInfo(shareInfo);
  }
  // #endif
};

export default {
  getShareInfo,
  updateShareInfo,
};
