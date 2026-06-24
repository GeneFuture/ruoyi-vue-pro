package cn.iocoder.yudao.module.maritime.service.risk;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.dal.dataobject.blacklist.RiskBlacklistDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.blacklist.RiskBlacklistMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link RiskCheckServiceImpl} 的单元测试类
 *
 * 重点覆盖：自推荐/同微信号/同手机号/黑名单 四条风控规则，及批量推荐限额判断
 */
@Import(RiskCheckServiceImpl.class)
public class RiskCheckServiceImplTest extends BaseDbUnitTest {

    @Resource
    private RiskCheckServiceImpl riskCheckService;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private RiskBlacklistMapper riskBlacklistMapper;
    @Resource
    private ReferralRelationMapper referralRelationMapper;

    @MockitoBean
    private MemberUserService memberUserService;
    @MockitoBean
    private ConfigApi configApi;

    private EnrollmentDO insertEnrollment(Long memberId, String phone) {
        EnrollmentDO enrollment = EnrollmentDO.builder()
                .enrollmentNo("EN" + randomLongId())
                .memberId(memberId)
                .sessionId(randomLongId())
                .realName("被推荐人")
                .idCard("110101199004044567")
                .phone(phone)
                .totalAmount(BigDecimal.TEN)
                .depositAmountSnapshot(BigDecimal.ONE)
                .balanceAmount(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .enrollmentStatus("PENDING_DEPOSIT")
                .referralRightGranted(false)
                .build();
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    @Test
    public void testCheck_selfReferral_fails() {
        Long memberId = randomLongId();
        EnrollmentDO enrollment = insertEnrollment(memberId, "13800138000");

        RiskCheckResult result = riskCheckService.check(memberId, enrollment.getId()); // 推荐人 == 被推荐人

        assertFalse(result.passed());
        assertEquals("RULE_1A", result.ruleCode());
    }

    @Test
    public void testCheck_enrollmentNotExists_fails() {
        RiskCheckResult result = riskCheckService.check(randomLongId(), randomLongId());

        assertFalse(result.passed());
        assertEquals("RULE_1A", result.ruleCode());
    }

    @Test
    public void testCheck_sameOpenId_fails() {
        Long referrerId = randomLongId();
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), "13800138001");
        when(memberUserService.getUser(referrerId))
                .thenReturn(MemberUserDO.builder().id(referrerId).openId("wx-shared-openid").mobile("13900139000").build());
        when(memberUserService.getUser(enrollment.getMemberId()))
                .thenReturn(MemberUserDO.builder().id(enrollment.getMemberId()).openId("wx-shared-openid").mobile("13900139001").build());

        RiskCheckResult result = riskCheckService.check(referrerId, enrollment.getId());

        assertFalse(result.passed());
        assertEquals("RULE_1B", result.ruleCode());
    }

    @Test
    public void testCheck_samePhone_fails() {
        Long referrerId = randomLongId();
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), "13800138002");
        when(memberUserService.getUser(referrerId))
                .thenReturn(MemberUserDO.builder().id(referrerId).openId("wx-openid-a").mobile("13800138002").build());
        when(memberUserService.getUser(enrollment.getMemberId()))
                .thenReturn(MemberUserDO.builder().id(enrollment.getMemberId()).openId("wx-openid-b").mobile("13800138002").build());

        RiskCheckResult result = riskCheckService.check(referrerId, enrollment.getId());

        assertFalse(result.passed());
        assertEquals("RULE_1C", result.ruleCode());
    }

    @Test
    public void testCheck_blacklistedPhone_fails() {
        Long referrerId = randomLongId();
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), "13800138003");
        riskBlacklistMapper.insert(RiskBlacklistDO.builder().phone("13800138003").reason("欺诈举报").build());
        when(memberUserService.getUser(referrerId))
                .thenReturn(MemberUserDO.builder().id(referrerId).openId("wx-openid-c").mobile("13900139002").build());
        when(memberUserService.getUser(enrollment.getMemberId()))
                .thenReturn(MemberUserDO.builder().id(enrollment.getMemberId()).openId("wx-openid-d").mobile("13900139003").build());

        RiskCheckResult result = riskCheckService.check(referrerId, enrollment.getId());

        assertFalse(result.passed());
        assertEquals("RULE_2", result.ruleCode());
    }

    @Test
    public void testCheck_allRulesPass() {
        Long referrerId = randomLongId();
        EnrollmentDO enrollment = insertEnrollment(randomLongId(), "13800138004");
        when(memberUserService.getUser(referrerId))
                .thenReturn(MemberUserDO.builder().id(referrerId).openId("wx-openid-e").mobile("13900139004").build());
        when(memberUserService.getUser(enrollment.getMemberId()))
                .thenReturn(MemberUserDO.builder().id(enrollment.getMemberId()).openId("wx-openid-f").mobile("13900139005").build());

        RiskCheckResult result = riskCheckService.check(referrerId, enrollment.getId());

        assertTrue(result.passed());
        assertNull(result.ruleCode());
    }

    @Test
    public void testCheckBatchReferralLimit_underDefaultLimit_returnsFalse() {
        Long referrerId = randomLongId();
        when(configApi.getConfigValueByKey(any())).thenReturn(null); // 走默认值 10

        for (int i = 0; i < 5; i++) {
            ReferralRelationDO relation = ReferralRelationDO.builder()
                    .referrerMemberId(referrerId)
                    .referredEnrollmentId(randomLongId())
                    .referredMemberId(randomLongId())
                    .sessionId(randomLongId())
                    .relationStatus("ACTIVE")
                    .build();
            referralRelationMapper.insert(relation);
        }

        assertFalse(riskCheckService.checkBatchReferralLimit(referrerId));
    }

    @Test
    public void testCheckBatchReferralLimit_overConfiguredLimit_returnsTrue() {
        Long referrerId = randomLongId();
        when(configApi.getConfigValueByKey(any())).thenReturn("3"); // 自定义限额为 3

        for (int i = 0; i < 4; i++) {
            ReferralRelationDO relation = ReferralRelationDO.builder()
                    .referrerMemberId(referrerId)
                    .referredEnrollmentId(randomLongId())
                    .referredMemberId(randomLongId())
                    .sessionId(randomLongId())
                    .relationStatus("ACTIVE")
                    .build();
            referralRelationMapper.insert(relation);
        }

        assertTrue(riskCheckService.checkBatchReferralLimit(referrerId));
    }

    @Test
    public void testCheckBatchReferralLimit_invalidConfig_fallsBackToDefault() {
        Long referrerId = randomLongId();
        when(configApi.getConfigValueByKey(any())).thenReturn("not-a-number"); // 配置异常，应回退默认值 10，不抛异常

        for (int i = 0; i < 5; i++) {
            ReferralRelationDO relation = ReferralRelationDO.builder()
                    .referrerMemberId(referrerId)
                    .referredEnrollmentId(randomLongId())
                    .referredMemberId(randomLongId())
                    .sessionId(randomLongId())
                    .relationStatus("ACTIVE")
                    .build();
            referralRelationMapper.insert(relation);
        }

        assertFalse(riskCheckService.checkBatchReferralLimit(referrerId));
    }

}
