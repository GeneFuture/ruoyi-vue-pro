package cn.iocoder.yudao.module.maritime.service.commissionAccount;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.commissionAccount.CommissionAccountMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 佣金账户 Service 实现类
 *
 * @author Gene Ye
 */
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

}