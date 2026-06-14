package cn.iocoder.yudao.module.maritime.service.commissionRecord;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.CommissionRecordPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.CommissionRecordSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.referral.vo.AppCommissionRecordRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.service.commissionAccount.CommissionAccountService;
import cn.iocoder.yudao.module.maritime.service.tax.TaxService;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 佣金记录 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class CommissionRecordServiceImpl implements CommissionRecordService {

    private static final String PAY_APP_KEY = "maritime";
    private static final DateTimeFormatter TRANSFER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private CommissionRecordMapper commissionRecordMapper;

    @Resource
    private ReferralRelationMapper referralRelationMapper;

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private CommissionAccountService commissionAccountService;

    @Resource
    private TaxService taxService;

    @Resource
    private MemberUserService memberUserService;

    @Resource
    private PayTransferApi payTransferApi;

    @Override
    public Long createCommissionRecord(CommissionRecordSaveReqVO createReqVO) {
        CommissionRecordDO commissionRecord = BeanUtils.toBean(createReqVO, CommissionRecordDO.class);
        commissionRecordMapper.insert(commissionRecord);
        return commissionRecord.getId();
    }

    @Override
    public void updateCommissionRecord(CommissionRecordSaveReqVO updateReqVO) {
        validateCommissionRecordExists(updateReqVO.getId());
        CommissionRecordDO updateObj = BeanUtils.toBean(updateReqVO, CommissionRecordDO.class);
        commissionRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteCommissionRecord(Long id) {
        validateCommissionRecordExists(id);
        commissionRecordMapper.deleteById(id);
    }

    @Override
    public void deleteCommissionRecordListByIds(List<Long> ids) {
        commissionRecordMapper.deleteByIds(ids);
    }

    private void validateCommissionRecordExists(Long id) {
        if (commissionRecordMapper.selectById(id) == null) {
            throw exception(COMMISSION_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public CommissionRecordDO getCommissionRecord(Long id) {
        return commissionRecordMapper.selectById(id);
    }

    @Override
    public PageResult<CommissionRecordDO> getCommissionRecordPage(CommissionRecordPageReqVO pageReqVO) {
        return commissionRecordMapper.selectPage(pageReqVO);
    }

    // ========== T06: 佣金触发 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerCommissionIfEligible(Long enrollmentId) {
        // 幂等：已有佣金记录则跳过
        if (commissionRecordMapper.existsByReferredEnrollmentId(enrollmentId)) {
            log.info("[triggerCommission] 幂等跳过, enrollmentId={}", enrollmentId);
            return;
        }

        // 查找 ACTIVE 推荐关系
        ReferralRelationDO relation = referralRelationMapper.selectByReferredEnrollmentId(enrollmentId);
        if (relation == null || !"ACTIVE".equals(relation.getRelationStatus())) {
            return;
        }

        EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            return;
        }

        // 查费用配置，获取佣金金额
        SessionFeeConfigDO feeConfig = sessionFeeConfigMapper.selectBySessionId(enrollment.getSessionId());
        if (feeConfig == null || !Boolean.TRUE.equals(feeConfig.getIsReferralCommissionEnabled())
                || feeConfig.getReferralCommissionAmount() == null) {
            log.info("[triggerCommission] 班期未开启佣金, sessionId={}", enrollment.getSessionId());
            return;
        }

        // 计算预计结算日期：开班日期 + 7天
        LocalDate expectedSettleDate = null;
        CourseSessionDO session = courseSessionMapper.selectById(enrollment.getSessionId());
        if (session != null && session.getStartDate() != null) {
            expectedSettleDate = session.getStartDate().plusDays(7);
        }

        CommissionRecordDO record = CommissionRecordDO.builder()
                .referrerMemberId(relation.getReferrerMemberId())
                .referredEnrollmentId(enrollmentId)
                .sessionId(enrollment.getSessionId())
                .commissionAmount(feeConfig.getReferralCommissionAmount())
                .commissionStatus("WAITING_FOR_CLASS")
                .expectedSettleDate(expectedSettleDate)
                .isRiskFlagged(false)
                .triggerTime(LocalDateTime.now())
                .retryCount(0)
                .build();
        commissionRecordMapper.insert(record);

        log.info("[triggerCommission] 佣金已创建, enrollmentId={}, referrerId={}, amount={}",
                enrollmentId, relation.getReferrerMemberId(), feeConfig.getReferralCommissionAmount());
    }

    // ========== T06: 管理端审核 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveCommission(Long id, Long approverId, String remark) {
        CommissionRecordDO record = commissionRecordMapper.selectById(id);
        if (record == null) {
            throw exception(COMMISSION_RECORD_NOT_EXISTS);
        }

        int updated = commissionRecordMapper.approveCommissionCas(id, "PENDING_REVIEW", "PENDING_PAYOUT");
        if (updated == 0) {
            throw exception(COMMISSION_APPROVE_NOT_ALLOWED);
        }

        CommissionRecordDO updateObj = new CommissionRecordDO();
        updateObj.setId(id);
        updateObj.setApproverId(approverId);
        updateObj.setApproveRemark(remark);
        updateObj.setApproveTime(LocalDateTime.now());
        commissionRecordMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectCommission(Long id, Long approverId, String reason, String remark) {
        CommissionRecordDO record = commissionRecordMapper.selectById(id);
        if (record == null) {
            throw exception(COMMISSION_RECORD_NOT_EXISTS);
        }

        int updated = commissionRecordMapper.approveCommissionCas(id, "PENDING_REVIEW", "REJECTED");
        if (updated == 0) {
            throw exception(COMMISSION_APPROVE_NOT_ALLOWED);
        }

        CommissionRecordDO updateObj = new CommissionRecordDO();
        updateObj.setId(id);
        updateObj.setApproverId(approverId);
        updateObj.setApproveRemark(remark != null ? reason + "；" + remark : reason);
        updateObj.setApproveTime(LocalDateTime.now());
        commissionRecordMapper.updateById(updateObj);
    }

    // ========== T06: App 端查询 ==========

    @Override
    public PageResult<AppCommissionRecordRespVO> getMyCommissionRecords(Long memberId, PageParam pageParam) {
        PageResult<CommissionRecordDO> pageResult = commissionRecordMapper.selectPageByReferrerMemberId(memberId, pageParam);

        // 批量查班期名称
        List<Long> sessionIds = pageResult.getList().stream()
                .map(CommissionRecordDO::getSessionId).distinct().collect(Collectors.toList());
        java.util.Map<Long, CourseSessionDO> sessionMap = courseSessionMapper
                .selectList(CourseSessionDO::getId, sessionIds)
                .stream().collect(Collectors.toMap(CourseSessionDO::getId, s -> s));

        List<AppCommissionRecordRespVO> voList = pageResult.getList().stream().map(r -> {
            AppCommissionRecordRespVO vo = new AppCommissionRecordRespVO();
            vo.setId(r.getId());
            vo.setCommissionAmount(r.getCommissionAmount());
            vo.setCommissionStatus(r.getCommissionStatus());
            vo.setSessionId(r.getSessionId());
            vo.setTriggerTime(r.getTriggerTime());
            vo.setExpectedSettleDate(r.getExpectedSettleDate());
            CourseSessionDO session = sessionMap.get(r.getSessionId());
            if (session != null) {
                vo.setSessionName(session.getSessionName());
            }
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal());
    }

    // ========== T08: 结算检查 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processSettleCheck(Long recordId) {
        CommissionRecordDO record = commissionRecordMapper.selectById(recordId);
        if (record == null || !"WAITING_FOR_CLASS".equals(record.getCommissionStatus())) {
            return;
        }

        boolean hasRefund = refundApplyMapper.existsByEnrollmentIdAndCreateTimeBefore(
                record.getReferredEnrollmentId(), record.getExpectedSettleDate());

        if (hasRefund) {
            commissionRecordMapper.updateStatusAndSettleTime(recordId, "FROZEN", LocalDateTime.now());
            log.info("[processSettleCheck] 佣金冻结（退费原因）: recordId={}", recordId);
        } else {
            commissionRecordMapper.updateStatusAndSettleTime(recordId, "PENDING_REVIEW", LocalDateTime.now());
            commissionAccountService.addPendingAmount(record.getReferrerMemberId(), record.getCommissionAmount());
            log.info("[processSettleCheck] 佣金进入审核: recordId={}", recordId);
        }
    }

    // ========== T08: 佣金发放 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payoutCommission(Long recordId) {
        CommissionRecordDO record = commissionRecordMapper.selectById(recordId);
        if (record == null) {
            return;
        }

        int claimed = commissionRecordMapper.updateStatusIfPendingPayout(recordId);
        if (claimed == 0) {
            log.warn("[payoutCommission] 非 PENDING_PAYOUT 状态，跳过: recordId={}", recordId);
            return;
        }

        MemberUserDO referrer = memberUserService.getUser(record.getReferrerMemberId());
        if (referrer == null || StrUtil.isBlank(referrer.getOpenId())) {
            String reason = "推荐人 openid 为空，无法发起企业付款";
            commissionRecordMapper.updateFailed(recordId, reason);
            log.error("[payoutCommission] {}: recordId={}", reason, recordId);
            return;
        }

        BigDecimal taxAmount = taxService.calculateAndRecordTax(record.getReferrerMemberId(), record.getCommissionAmount());
        BigDecimal netAmount = record.getCommissionAmount().subtract(taxAmount);

        String transferNo = "TR" + LocalDateTime.now().format(TRANSFER_NO_FMT)
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        try {
            PayTransferCreateReqDTO transferReq = new PayTransferCreateReqDTO();
            transferReq.setAppKey(PAY_APP_KEY);
            transferReq.setUserIp("127.0.0.1");
            transferReq.setUserId(record.getReferrerMemberId());
            transferReq.setUserType(UserTypeEnum.MEMBER.getValue());
            transferReq.setMerchantTransferId(transferNo);
            transferReq.setSubject("海员培训推荐佣金");
            transferReq.setUserAccount(referrer.getOpenId());
            transferReq.setUserName(referrer.getNickname());
            transferReq.setChannelCode("wx_lite");
            transferReq.setPrice(netAmount.multiply(BigDecimal.valueOf(100)).intValue());

            PayTransferCreateRespDTO resp = payTransferApi.createTransfer(transferReq);

            commissionRecordMapper.updatePaid(recordId, LocalDateTime.now(), resp.getId());
            commissionAccountService.confirmPayout(record.getReferrerMemberId(), record.getCommissionAmount());
            log.info("[payoutCommission] 佣金发放成功: recordId={}, transferId={}, netAmount={}", recordId, resp.getId(), netAmount);
        } catch (Exception e) {
            commissionRecordMapper.updateFailed(recordId, e.getMessage());
            log.error("[payoutCommission] 企业付款失败: recordId={}", recordId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualPayout(Long recordId) {
        CommissionRecordDO record = commissionRecordMapper.selectById(recordId);
        if (record == null) {
            throw exception(COMMISSION_RECORD_NOT_EXISTS);
        }
        if (!"FAILED".equals(record.getCommissionStatus())) {
            throw exception(COMMISSION_STATUS_ERROR);
        }
        // 重置为 PENDING_PAYOUT，再调用发放
        commissionRecordMapper.updateStatusById(recordId, "PENDING_PAYOUT");
        payoutCommission(recordId);
    }

}
