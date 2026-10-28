// ========== COMMON - 公共模块 ==========

/**
 * 与后端Terminal枚举一一对应
 */
export const TerminalEnum = {
  UNKNOWN: 0, // 未知, 目的：在无法解析到 terminal 时，使用它
  WECHAT_MINI_PROGRAM: 10, //微信小程序
  WECHAT_WAP: 11, // 微信公众号
  H5: 20, // H5 网页
  APP: 31, // 手机 App
};

/**
 * 将 uni-app 提供的平台转换为后端所需的 terminal值
 *
 * @return 终端
 */
export const getTerminal = () => {
  const platformType = uni.getAppBaseInfo().uniPlatform;
  // 与后端terminal枚举一一对应
  switch (platformType) {
    case 'app':
      return TerminalEnum.APP;
    case 'web':
      return TerminalEnum.H5;
    case 'mp-weixin':
      return TerminalEnum.WECHAT_MINI_PROGRAM;
    default:
      return TerminalEnum.UNKNOWN;
  }
};
