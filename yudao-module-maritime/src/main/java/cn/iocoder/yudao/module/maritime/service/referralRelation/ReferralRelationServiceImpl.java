package cn.iocoder.yudao.module.maritime.service.referralRelation;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.ReferralRelationPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.ReferralRelationSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.referral.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.riskEvent.RiskEventDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionAccount.CommissionAccountMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.riskEvent.RiskEventMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.service.risk.RiskCheckResult;
import cn.iocoder.yudao.module.maritime.service.risk.RiskCheckService;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 推荐关系 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class ReferralRelationServiceImpl implements ReferralRelationService {

    @Resource
    private ReferralRelationMapper referralRelationMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CommissionRecordMapper commissionRecordMapper;

    @Resource
    private CommissionAccountMapper commissionAccountMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private MemberUserService memberUserService;

    @Resource
    private RiskCheckService riskCheckService;

    @Resource
    private RiskEventMapper riskEventMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    // ========== Admin CRUD ==========

    @Override
    public Long createReferralRelation(ReferralRelationSaveReqVO createReqVO) {
        ReferralRelationDO referralRelation = BeanUtils.toBean(createReqVO, ReferralRelationDO.class);
        referralRelationMapper.insert(referralRelation);
        return referralRelation.getId();
    }

    @Override
    public void updateReferralRelation(ReferralRelationSaveReqVO updateReqVO) {
        validateReferralRelationExists(updateReqVO.getId());
        ReferralRelationDO updateObj = BeanUtils.toBean(updateReqVO, ReferralRelationDO.class);
        referralRelationMapper.updateById(updateObj);
    }

    @Override
    public void deleteReferralRelation(Long id) {
        validateReferralRelationExists(id);
        referralRelationMapper.deleteById(id);
    }

    @Override
    public void deleteReferralRelationListByIds(List<Long> ids) {
        referralRelationMapper.deleteByIds(ids);
    }

    private void validateReferralRelationExists(Long id) {
        if (referralRelationMapper.selectById(id) == null) {
            throw exception(REFERRAL_RELATION_NOT_EXISTS);
        }
    }

    @Override
    public ReferralRelationDO getReferralRelation(Long id) {
        return referralRelationMapper.selectById(id);
    }

    @Override
    public PageResult<ReferralRelationDO> getReferralRelationPage(ReferralRelationPageReqVO pageReqVO) {
        return referralRelationMapper.selectPage(pageReqVO);
    }

    // ========== T06: 推荐关系建立 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void establishRelationIfPresent(Long enrollmentId) {
        try {
            EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
            if (enrollment == null || StrUtil.isBlank(enrollment.getReferralCodeUsed())) {
                return;
            }
            if (referralRelationMapper.selectByReferredEnrollmentId(enrollmentId) != null) {
                return; // 幂等
            }

            // 查推荐人
            MemberUserDO referrer = memberUserService.getUserByReferralCode(enrollment.getReferralCodeUsed());
            if (referrer == null) {
                log.warn("[establishRelation] 推荐码不存在: code={}, enrollmentId={}",
                        enrollment.getReferralCodeUsed(), enrollmentId);
                return;
            }

            // Rule 1 + Rule 2 风控检查
            RiskCheckResult checkResult = riskCheckService.check(referrer.getId(), enrollmentId);

            // Rule 3: 批量推荐检测
            if (checkResult.passed() && riskCheckService.checkBatchReferralLimit(referrer.getId())) {
                log.warn("[risk][Rule3] 批量推荐告警: referrerId={}", referrer.getId());
                // 临时禁推
                memberUserMapper.update(null, new LambdaUpdateWrapper<MemberUserDO>()
                        .set(MemberUserDO::getIsReferralBanned, true)
                        .eq(MemberUserDO::getId, referrer.getId()));
                // 记录风控事件
                riskEventMapper.insert(RiskEventDO.builder()
                        .eventType("BATCH_REFERRAL")
                        .memberId(referrer.getId())
                        .detail("24h内推荐数超过阈值，自动禁推")
                        .isReviewed(false)
                        .build());
                checkResult = RiskCheckResult.fail("RULE_3", "推荐频率超过限制，已暂停推荐权限");
            }

            ReferralRelationDO relation = ReferralRelationDO.builder()
                    .referrerMemberId(referrer.getId())
                    .referredEnrollmentId(enrollmentId)
                    .referredMemberId(enrollment.getMemberId())
                    .sessionId(enrollment.getSessionId())
                    .relationStatus(checkResult.passed() ? "ACTIVE" : "INVALID")
                    .invalidReason(checkResult.passed() ? null :
                            "[" + checkResult.ruleCode() + "] " + checkResult.reason())
                    .referralLockedAt(LocalDateTime.now())
                    .build();
            referralRelationMapper.insert(relation);

            log.info("[establishRelation] enrollmentId={}, referrerId={}, status={}, reason={}",
                    enrollmentId, referrer.getId(), relation.getRelationStatus(), relation.getInvalidReason());
        } catch (Exception e) {
            log.error("[establishRelation] 推荐关系建立失败，不阻塞主流程, enrollmentId={}", enrollmentId, e);
        }
    }

    // ========== T06: App 端推荐中心 ==========

    @Override
    public AppReferralInfoRespVO getMyReferralInfo(Long memberId) {
        MemberUserDO user = memberUserService.getUser(memberId);
        boolean hasReferralRight = enrollmentMapper.hasReferralRight(memberId);

        long totalCount = referralRelationMapper.countByReferrerMemberId(memberId);

        // 成功推荐数：有 PENDING_PAYOUT/PAID 状态佣金记录的数量
        PageParam allPage = new PageParam();
        allPage.setPageSize(PageParam.PAGE_SIZE_NONE);
        long successCount = commissionRecordMapper.selectPageByReferrerMemberId(memberId, allPage).getList().stream()
                .filter(r -> "PENDING_PAYOUT".equals(r.getCommissionStatus())
                        || "PAID".equals(r.getCommissionStatus()))
                .count();

        CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
        BigDecimal pendingAmount = account != null ? account.getPendingAmount() : BigDecimal.ZERO;
        BigDecimal paidAmount = account != null ? account.getPaidAmount() : BigDecimal.ZERO;

        AppReferralInfoRespVO vo = new AppReferralInfoRespVO();
        vo.setReferralCode(user != null ? user.getReferralCode() : null);
        vo.setHasReferralRight(hasReferralRight);
        vo.setSharePath("/pages/maritime/course-list?ref=" + (user != null ? user.getReferralCode() : ""));
        vo.setTotalReferralCount(totalCount);
        vo.setSuccessCount(successCount);
        vo.setPendingCommissionAmount(pendingAmount != null ? pendingAmount : BigDecimal.ZERO);
        vo.setPaidCommissionAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        return vo;
    }

    @Override
    public AppShareParamsRespVO getShareParams(Long memberId, Long sessionId) {
        MemberUserDO user = memberUserService.getUser(memberId);
        String referralCode = user != null ? user.getReferralCode() : null;

        String title = "海员培训课程推荐";
        String path = "/pages/maritime/course-list?ref=" + referralCode;
        if (sessionId != null) {
            CourseSessionDO session = courseSessionMapper.selectById(sessionId);
            if (session != null) {
                title = "推荐你参加：" + session.getSessionName();
                path = "/pages/maritime/course-detail?sessionId=" + sessionId + "&ref=" + referralCode;
            }
        }

        AppShareParamsRespVO vo = new AppShareParamsRespVO();
        vo.setPath(path);
        vo.setTitle(title);
        vo.setReferralCode(referralCode);
        return vo;
    }

    @Override
    public PageResult<AppReferralRecordRespVO> getMyReferralList(Long memberId, PageParam pageParam) {
        PageResult<ReferralRelationDO> pageResult = referralRelationMapper.selectPageByReferrerMemberId(memberId, pageParam);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }

        // 批量查班期名称
        List<Long> sessionIds = pageResult.getList().stream()
                .map(ReferralRelationDO::getSessionId).distinct().collect(Collectors.toList());
        Map<Long, CourseSessionDO> sessionMap = courseSessionMapper
                .selectList(CourseSessionDO::getId, sessionIds)
                .stream().collect(Collectors.toMap(CourseSessionDO::getId, s -> s));

        // 批量查佣金记录（按 referredEnrollmentId）
        List<Long> enrollmentIds = pageResult.getList().stream()
                .map(ReferralRelationDO::getReferredEnrollmentId).distinct().collect(Collectors.toList());
        // 查被推荐人姓名（脱敏）
        Map<Long, EnrollmentDO> enrollmentMap = enrollmentMapper
                .selectList(EnrollmentDO::getId, enrollmentIds)
                .stream().collect(Collectors.toMap(EnrollmentDO::getId, e -> e));

        List<AppReferralRecordRespVO> voList = pageResult.getList().stream().map(r -> {
            AppReferralRecordRespVO vo = new AppReferralRecordRespVO();
            vo.setId(r.getId());
            vo.setRelationStatus(r.getRelationStatus());
            vo.setCreateTime(r.getCreateTime());

            // 被推荐人姓名脱敏
            EnrollmentDO enrollment = enrollmentMap.get(r.getReferredEnrollmentId());
            if (enrollment != null && StrUtil.isNotBlank(enrollment.getRealName())) {
                vo.setReferredName(maskName(enrollment.getRealName()));
            }

            // 班期名称
            CourseSessionDO session = sessionMap.get(r.getSessionId());
            if (session != null) {
                vo.setSessionName(session.getSessionName());
            }

            // 对应佣金
            CommissionRecordDO commission = commissionRecordMapper.selectOne(
                    CommissionRecordDO::getReferredEnrollmentId, r.getReferredEnrollmentId());
            if (commission != null) {
                vo.setCommissionAmount(commission.getCommissionAmount());
                vo.setCommissionStatus(commission.getCommissionStatus());
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    @Override
    public AppCommissionAccountRespVO getMyCommissionAccount(Long memberId) {
        CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
        boolean hasReferralRight = enrollmentMapper.hasReferralRight(memberId);
        long totalReferralCount = referralRelationMapper.countByReferrerMemberId(memberId);

        AppCommissionAccountRespVO vo = new AppCommissionAccountRespVO();
        vo.setTotalAmount(account != null && account.getTotalAmount() != null ? account.getTotalAmount() : BigDecimal.ZERO);
        vo.setPaidAmount(account != null && account.getPaidAmount() != null ? account.getPaidAmount() : BigDecimal.ZERO);
        vo.setPendingAmount(account != null && account.getPendingAmount() != null ? account.getPendingAmount() : BigDecimal.ZERO);
        vo.setFrozenAmount(account != null && account.getFrozenAmount() != null ? account.getFrozenAmount() : BigDecimal.ZERO);
        vo.setTotalReferralCount((int) totalReferralCount);
        vo.setHasReferralRight(hasReferralRight);
        return vo;
    }

    private static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

}
