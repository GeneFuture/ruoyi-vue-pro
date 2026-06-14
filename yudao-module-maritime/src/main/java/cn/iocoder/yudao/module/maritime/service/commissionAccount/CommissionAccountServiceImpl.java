package cn.iocoder.yudao.module.maritime.service.commissionAccount;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.commissionAccount.CommissionAccountMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 佣金账户 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class CommissionAccountServiceImpl implements CommissionAccountService {

    @Resource
    private CommissionAccountMapper commissionAccountMapper;

    @Override
    public Long createCommissionAccount(CommissionAccountSaveReqVO createReqVO) {
        // 插入
        CommissionAccountDO commissionAccount = BeanUtils.toBean(createReqVO, CommissionAccountDO.class);
        commissionAccountMapper.insert(commissionAccount);

        // 返回
        return commissionAccount.getId();
    }

    @Override
    public void updateCommissionAccount(CommissionAccountSaveReqVO updateReqVO) {
        // 校验存在
        validateCommissionAccountExists(updateReqVO.getId());
        // 更新
        CommissionAccountDO updateObj = BeanUtils.toBean(updateReqVO, CommissionAccountDO.class);
        commissionAccountMapper.updateById(updateObj);
    }

    @Override
    public void deleteCommissionAccount(Long id) {
        // 校验存在
        validateCommissionAccountExists(id);
        // 删除
        commissionAccountMapper.deleteById(id);
    }

    @Override
    public void deleteCommissionAccountListByIds(List<Long> ids) {
        // 删除
        commissionAccountMapper.deleteByIds(ids);
    }


    private void validateCommissionAccountExists(Long id) {
        if (commissionAccountMapper.selectById(id) == null) {
            throw exception(COMMISSION_ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public CommissionAccountDO getCommissionAccount(Long id) {
        return commissionAccountMapper.selectById(id);
    }

    @Override
    public PageResult<CommissionAccountDO> getCommissionAccountPage(CommissionAccountPageReqVO pageReqVO) {
        return commissionAccountMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initAccountIfAbsent(Long memberId) {
        if (commissionAccountMapper.selectByMemberId(memberId) != null) {
            return;
        }
        CommissionAccountDO account = new CommissionAccountDO();
        account.setMemberId(memberId);
        account.setTotalAmount(BigDecimal.ZERO);
        account.setPaidAmount(BigDecimal.ZERO);
        account.setPendingAmount(BigDecimal.ZERO);
        account.setFrozenAmount(BigDecimal.ZERO);
        account.setRejectedAmount(BigDecimal.ZERO);
        account.setTotalReferralCount(0);
        account.setVersion(0);
        commissionAccountMapper.insert(account);
    }

    @Override
    public CommissionAccountDO getAccountByMemberId(Long memberId) {
        CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
        if (account == null) {
            throw exception(COMMISSION_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPendingAmount(Long memberId, BigDecimal amount) {
        for (int i = 0; i < 3; i++) {
            CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
            if (account == null) {
                initAccountIfAbsent(memberId);
                account = commissionAccountMapper.selectByMemberId(memberId);
            }
            int updated = commissionAccountMapper.addPendingAmount(account.getId(), amount, account.getVersion());
            if (updated > 0) {
                return;
            }
        }
        log.warn("[addPendingAmount] 乐观锁重试3次失败, memberId={}, amount={}", memberId, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freezeAmount(Long memberId, BigDecimal amount) {
        for (int i = 0; i < 3; i++) {
            CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
            if (account == null) {
                throw exception(COMMISSION_ACCOUNT_NOT_EXISTS);
            }
            if (account.getPendingAmount().compareTo(amount) < 0) {
                throw exception(WITHDRAWAL_INSUFFICIENT_BALANCE);
            }
            int updated = commissionAccountMapper.freezeAmount(account.getId(), amount, account.getVersion());
            if (updated > 0) {
                return;
            }
        }
        log.warn("[freezeAmount] 乐观锁重试3次失败, memberId={}, amount={}", memberId, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPayout(Long memberId, BigDecimal amount) {
        for (int i = 0; i < 3; i++) {
            CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
            if (account == null) {
                return;
            }
            int updated = commissionAccountMapper.confirmPayout(account.getId(), amount, account.getVersion());
            if (updated > 0) {
                return;
            }
        }
        log.warn("[confirmPayout] 乐观锁重试3次失败, memberId={}, amount={}", memberId, amount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreezeAmount(Long memberId, BigDecimal amount) {
        for (int i = 0; i < 3; i++) {
            CommissionAccountDO account = commissionAccountMapper.selectByMemberId(memberId);
            if (account == null) {
                return;
            }
            int updated = commissionAccountMapper.unfreezeAmount(account.getId(), amount, account.getVersion());
            if (updated > 0) {
                return;
            }
        }
        log.warn("[unfreezeAmount] 乐观锁重试3次失败, memberId={}, amount={}", memberId, amount);
    }

}