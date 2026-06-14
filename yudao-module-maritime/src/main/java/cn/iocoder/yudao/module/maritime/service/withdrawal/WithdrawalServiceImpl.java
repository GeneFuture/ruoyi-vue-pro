package cn.iocoder.yudao.module.maritime.service.withdrawal;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.taxRecord.TaxRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.withdrawalApply.WithdrawalApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.taxRecord.TaxRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.withdrawalApply.WithdrawalApplyMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 佣金提现 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("100");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000");
    private static final int MONTHLY_MAX_TIMES = 5;
    private static final String PAY_APP_KEY = "maritime";
    private static final DateTimeFormatter TRANSFER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private WithdrawalApplyMapper withdrawalApplyMapper;
    @Resource
    private CommissionAccountService commissionAccountService;
    @Resource
    private TaxService taxService;
    @Resource
    private TaxRecordMapper taxRecordMapper;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private MemberUserService memberUserService;
    @Resource
    private PayTransferApi payTransferApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppWithdrawalApplyRespVO applyWithdrawal(Long memberId, AppWithdrawalApplyReqVO reqVO) {
        BigDecimal amount = reqVO.getAmount();

        // 金额校验
        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw exception(WITHDRAWAL_AMOUNT_TOO_SMALL);
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw exception(WITHDRAWAL_AMOUNT_TOO_LARGE);
        }

        // 账户余额校验
        CommissionAccountDO account = commissionAccountService.getAccountByMemberId(memberId);
        if (account.getPendingAmount().compareTo(amount) < 0) {
            throw exception(WITHDRAWAL_INSUFFICIENT_BALANCE);
        }

        // 账户状态校验
        MemberUserDO member = memberUserService.getUser(memberId);
        if (member == null || member.getStatus() == null || member.getStatus() != 0) {
            throw exception(WITHDRAWAL_ACCOUNT_FROZEN);
        }

        // 冻结期校验（首次定金支付后 7 天内不可提现）
        LocalDateTime firstDepositTime = enrollmentMapper.getFirstDepositTime(memberId);
        if (firstDepositTime != null && firstDepositTime.plusDays(7).isAfter(LocalDateTime.now())) {
            throw exception(WITHDRAWAL_IN_FREEZE_PERIOD);
        }

        // 本月提现次数校验
        String yearMonth = YearMonth.now().toString();
        int monthCount = withdrawalApplyMapper.countByMemberAndMonth(memberId, yearMonth);
        if (monthCount >= MONTHLY_MAX_TIMES) {
            throw exception(WITHDRAWAL_MONTHLY_LIMIT_EXCEEDED);
        }

        // 24h 累计金额校验
        BigDecimal last24hTotal = withdrawalApplyMapper.sumSuccessAmountSince(
                memberId, LocalDateTime.now().minusHours(24));
        if (last24hTotal == null) {
            last24hTotal = BigDecimal.ZERO;
        }
        if (last24hTotal.add(amount).compareTo(DAILY_LIMIT) > 0) {
            throw exception(WITHDRAWAL_DAILY_LIMIT_EXCEEDED);
        }

        // 不能有进行中的申请
        if (withdrawalApplyMapper.existsPendingByMember(memberId)) {
            throw exception(WITHDRAWAL_PENDING_EXISTS);
        }

        // 计算税额
        BigDecimal taxAmount = taxService.calculateTaxPreview(memberId, amount);
        BigDecimal netAmount = amount.subtract(taxAmount);

        WithdrawalApplyDO apply = WithdrawalApplyDO.builder()
                .memberId(memberId)
                .applyAmount(amount)
                .taxAmount(taxAmount)
                .netAmount(netAmount)
                .channel(reqVO.getChannel())
                .applyStatus("PENDING")
                .build();
        withdrawalApplyMapper.insert(apply);

        // 冻结账户余额
        commissionAccountService.freezeAmount(memberId, amount);

        // 自动审批并发起转账
        autoApproveAndPay(apply, member, taxAmount, netAmount);

        AppWithdrawalApplyRespVO respVO = new AppWithdrawalApplyRespVO();
        respVO.setApplyId(apply.getId());
        respVO.setApplyAmount(amount);
        respVO.setTaxAmount(taxAmount);
        respVO.setNetAmount(netAmount);
        respVO.setEstimatedArrivalDesc("WX_BALANCE".equals(reqVO.getChannel()) ? "预计 T+1 到账" : "预计 2-3 工作日到账");
        return respVO;
    }

    private void autoApproveAndPay(WithdrawalApplyDO apply, MemberUserDO member,
                                   BigDecimal taxAmount, BigDecimal netAmount) {
        if (StrUtil.isBlank(member.getOpenId())) {
            log.warn("[autoApproveAndPay] 用户 openid 为空，转账失败: memberId={}", member.getId());
            updateWithdrawalFailed(apply.getId(), "用户 openid 为空，无法发起企业付款");
            return;
        }

        String transferNo = "WD" + LocalDateTime.now().format(TRANSFER_NO_FMT)
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        try {
            PayTransferCreateReqDTO transferReq = new PayTransferCreateReqDTO();
            transferReq.setAppKey(PAY_APP_KEY);
            transferReq.setUserIp("127.0.0.1");
            transferReq.setUserId(member.getId());
            transferReq.setUserType(UserTypeEnum.MEMBER.getValue());
            transferReq.setMerchantTransferId(transferNo);
            transferReq.setSubject("佣金提现");
            transferReq.setUserAccount(member.getOpenId());
            transferReq.setUserName(member.getNickname());
            transferReq.setChannelCode("wx_lite");
            transferReq.setPrice(netAmount.multiply(BigDecimal.valueOf(100)).intValue());

            PayTransferCreateRespDTO resp = payTransferApi.createTransfer(transferReq);

            // 记录实际税额
            taxService.calculateAndRecordTax(apply.getMemberId(), apply.getApplyAmount());

            WithdrawalApplyDO update = new WithdrawalApplyDO();
            update.setId(apply.getId());
            update.setApplyStatus("SUCCESS");
            update.setPayTransferId(resp.getId());
            update.setSuccessTime(LocalDateTime.now());
            withdrawalApplyMapper.updateById(update);

            // 发放成功转账
            commissionAccountService.confirmPayout(apply.getMemberId(), apply.getApplyAmount());
            log.info("[autoApproveAndPay] 提现成功: applyId={}, transferId={}", apply.getId(), resp.getId());
        } catch (Exception e) {
            log.error("[autoApproveAndPay] 提现转账失败: applyId={}", apply.getId(), e);
            updateWithdrawalFailed(apply.getId(), e.getMessage());
        }
    }

    private void updateWithdrawalFailed(Long applyId, String reason) {
        WithdrawalApplyDO update = new WithdrawalApplyDO();
        update.setId(applyId);
        update.setApplyStatus("FAILED");
        update.setFailReason(reason);
        withdrawalApplyMapper.updateById(update);
    }

    @Override
    public PageResult<AppWithdrawalRecordRespVO> getMyWithdrawals(Long memberId, PageParam pageParam) {
        PageResult<WithdrawalApplyDO> page = withdrawalApplyMapper.selectPageByMemberId(memberId, pageParam);
        List<AppWithdrawalRecordRespVO> voList = page.getList().stream().map(apply -> {
            AppWithdrawalRecordRespVO vo = new AppWithdrawalRecordRespVO();
            vo.setId(apply.getId());
            vo.setApplyAmount(apply.getApplyAmount());
            vo.setTaxAmount(apply.getTaxAmount());
            vo.setNetAmount(apply.getNetAmount());
            vo.setChannel(apply.getChannel());
            vo.setApplyStatus(apply.getApplyStatus());
            vo.setSuccessTime(apply.getSuccessTime());
            vo.setCreateTime(apply.getCreateTime());
            vo.setFailReason(apply.getFailReason() != null ? apply.getFailReason() : apply.getRejectReason());
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal());
    }

    @Override
    public PageResult<AdminWithdrawalRespVO> getWithdrawalPage(AdminWithdrawalPageReqVO reqVO) {
        PageResult<WithdrawalApplyDO> page = withdrawalApplyMapper.selectPage(reqVO);
        return BeanUtils.toBean(page, AdminWithdrawalRespVO.class);
    }

    @Override
    public void rejectWithdrawal(Long id, String reason) {
        WithdrawalApplyDO apply = withdrawalApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(WITHDRAWAL_NOT_EXISTS);
        }
        int updated = withdrawalApplyMapper.rejectIfPending(id, reason);
        if (updated == 0) {
            throw exception(COMMISSION_STATUS_ERROR);
        }
        // 解冻余额：PENDING 状态时已做 freezeAmount，拒绝后需归还
        commissionAccountService.unfreezeAmount(apply.getMemberId(), apply.getApplyAmount());
    }

    @Override
    public AppTaxPreviewRespVO previewTax(Long memberId, BigDecimal amount) {
        String taxMonth = YearMonth.now().toString();
        TaxRecordDO record = taxRecordMapper.selectByMemberAndMonth(memberId, taxMonth);
        BigDecimal monthIncome = record != null ? record.getMonthIncome() : BigDecimal.ZERO;
        BigDecimal taxAmount = taxService.calculateTaxPreview(memberId, amount);
        BigDecimal netAmount = amount.subtract(taxAmount);

        AppTaxPreviewRespVO vo = new AppTaxPreviewRespVO();
        vo.setApplyAmount(amount);
        vo.setTaxAmount(taxAmount);
        vo.setNetAmount(netAmount);
        vo.setMonthIncome(monthIncome);
        vo.setMonthIncomeAfter(monthIncome.add(amount));
        if (taxAmount.compareTo(BigDecimal.ZERO) == 0) {
            vo.setTaxDesc("本月累计未超过800元，暂无代扣个税");
        } else {
            vo.setTaxDesc("本月累计超过800元，代扣20%个税 " + taxAmount + " 元");
        }
        return vo;
    }

}
