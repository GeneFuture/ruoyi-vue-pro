package cn.iocoder.yudao.module.maritime.service.risk;

/**
 * 风控检查 Service 接口
 *
 * @author Gene Ye
 */
public interface RiskCheckService {

    /**
     * 执行推荐风控检查
     * Rule 1: 自推检测（同 memberId / openId / 手机号）
     * Rule 2: 被推荐人手机号黑名单
     * Rule 3: 批量推荐检测（Phase 2 实现，本期预留）
     *
     * @param referrerId   推荐人 memberId
     * @param enrollmentId 被推荐人报名ID
     * @return 检查结果
     */
    RiskCheckResult check(Long referrerId, Long enrollmentId);
}
