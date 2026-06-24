package cn.iocoder.yudao.module.maritime.service.risk;

import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.blacklist.RiskBlacklistMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 风控检查 Service 实现类
 *
 * Rule 1a: 被推荐人与推荐人是同一 memberId（自推荐）
 * Rule 1b: 被推荐人与推荐人使用同一 openId（换号规避）
 * Rule 1c: 被推荐人与推荐人手机号相同
 * Rule 2:  被推荐人手机号在风控黑名单中
 * Rule 3:  24h 内同一推荐人推荐超 N 人批量检测
 */
@Slf4j
@Service
public class RiskCheckServiceImpl implements RiskCheckService {

    private static final int DEFAULT_BATCH_REFERRAL_LIMIT = 10;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private MemberUserService memberUserService;

    @Resource
    private RiskBlacklistMapper riskBlacklistMapper;

    @Resource
    private ReferralRelationMapper referralRelationMapper;

    @Resource
    private ConfigApi configApi;

    @Override
    public RiskCheckResult check(Long referrerId, Long enrollmentId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            return RiskCheckResult.fail("RULE_1A", "报名记录不存在");
        }

        Long referredMemberId = enrollment.getMemberId();
        String referredPhone = enrollment.getPhone();

        // Rule 1a: 自推荐（同 memberId）
        if (referrerId.equals(referredMemberId)) {
            return RiskCheckResult.fail("RULE_1A", "不允许自推荐");
        }

        // Rule 1b: 同 openId（同微信账号换号规避）
        MemberUserDO referrer = memberUserService.getUser(referrerId);
        MemberUserDO referred = memberUserService.getUser(referredMemberId);
        if (referrer != null && referred != null
                && referrer.getOpenId() != null
                && referrer.getOpenId().equals(referred.getOpenId())) {
            return RiskCheckResult.fail("RULE_1B", "推荐人与被推荐人使用同一微信账号");
        }

        // Rule 1c: 同手机号
        if (referrer != null && referred != null) {
            String referrerPhone = referrer.getMobile();
            if (referrerPhone != null && referrerPhone.equals(referredPhone)) {
                return RiskCheckResult.fail("RULE_1C", "推荐人与被推荐人手机号相同");
            }
        }

        // Rule 2: 黑名单检查（被推荐人手机号）
        if (referredPhone != null && riskBlacklistMapper.existsByPhone(referredPhone)) {
            return RiskCheckResult.fail("RULE_2", "被推荐人手机号在风控黑名单中");
        }

        return RiskCheckResult.pass();
    }

    @Override
    public boolean checkBatchReferralLimit(Long referrerId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        long count = referralRelationMapper.countActiveByReferrerSince(referrerId, since);

        int limit = DEFAULT_BATCH_REFERRAL_LIMIT;
        try {
            String configValue = configApi.getConfigValueByKey("maritime.risk.batch_referral_limit");
            if (configValue != null) {
                limit = Integer.parseInt(configValue);
            }
        } catch (Exception e) {
            log.warn("[checkBatchReferralLimit] 读取系统参数失败，使用默认值: {}", DEFAULT_BATCH_REFERRAL_LIMIT);
        }

        return count >= limit;
    }
}
