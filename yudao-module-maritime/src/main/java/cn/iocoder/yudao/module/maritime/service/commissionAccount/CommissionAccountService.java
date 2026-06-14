package cn.iocoder.yudao.module.maritime.service.commissionAccount;

import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 佣金账户 Service 接口
 *
 * @author Gene Ye
 */
public interface CommissionAccountService {

    /**
     * 创建佣金账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCommissionAccount(@Valid CommissionAccountSaveReqVO createReqVO);

    /**
     * 更新佣金账户
     *
     * @param updateReqVO 更新信息
     */
    void updateCommissionAccount(@Valid CommissionAccountSaveReqVO updateReqVO);

    /**
     * 删除佣金账户
     *
     * @param id 编号
     */
    void deleteCommissionAccount(Long id);

    /**
    * 批量删除佣金账户
    *
    * @param ids 编号
    */
    void deleteCommissionAccountListByIds(List<Long> ids);

    /**
     * 获得佣金账户
     *
     * @param id 编号
     * @return 佣金账户
     */
    CommissionAccountDO getCommissionAccount(Long id);

    /**
     * 获得佣金账户分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金账户分页
     */
    PageResult<CommissionAccountDO> getCommissionAccountPage(CommissionAccountPageReqVO pageReqVO);

    /**
     * 为会员初始化佣金账户（若已存在则幂等返回）
     *
     * @param memberId 会员ID
     */
    void initAccountIfAbsent(Long memberId);

    /**
     * 获取账户（不存在则抛异常）
     */
    CommissionAccountDO getAccountByMemberId(Long memberId);

    /**
     * 佣金进入审核时累加 pendingAmount（乐观锁重试）
     */
    void addPendingAmount(Long memberId, BigDecimal amount);

    /**
     * 申请提现时冻结 pendingAmount（乐观锁重试）
     */
    void freezeAmount(Long memberId, BigDecimal amount);

    /**
     * 提现/发放成功，将 frozenAmount 转入 paidAmount（乐观锁重试）
     */
    void confirmPayout(Long memberId, BigDecimal amount);

    /**
     * 提现被拒绝，将 frozenAmount 归还 pendingAmount（乐观锁重试）
     */
    void unfreezeAmount(Long memberId, BigDecimal amount);

}