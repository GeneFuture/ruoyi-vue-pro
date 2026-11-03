package cn.iocoder.yudao.module.maritime.service.home;

import cn.iocoder.yudao.module.maritime.controller.app.home.vo.AppHomeRespVO;

/**
 * 小程序首页聚合 Service 接口
 *
 * @author Gene Ye
 */
public interface AppHomeService {

    /**
     * 获取首页聚合数据（金刚区 + Banner + 平台统计 + 公告 + 近期开班）
     *
     * 带 Redis 缓存，缓存周期内重复访问不查库。
     *
     * @return 首页聚合数据
     */
    AppHomeRespVO getHome();

}
