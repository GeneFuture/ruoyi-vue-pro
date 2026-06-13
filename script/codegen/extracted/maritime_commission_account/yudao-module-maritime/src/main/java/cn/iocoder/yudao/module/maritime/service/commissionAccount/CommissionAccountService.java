package cn.iocoder.yudao.module.maritime.service.commissionAccount;

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

}