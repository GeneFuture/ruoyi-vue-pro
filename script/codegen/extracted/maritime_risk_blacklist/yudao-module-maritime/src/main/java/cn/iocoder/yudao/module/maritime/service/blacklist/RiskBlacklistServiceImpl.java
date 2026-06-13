package cn.iocoder.yudao.module.maritime.service.blacklist;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.blacklist.RiskBlacklistDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.blacklist.RiskBlacklistMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 风控黑名单 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class RiskBlacklistServiceImpl implements RiskBlacklistService {

    @Resource
    private RiskBlacklistMapper riskBlacklistMapper;

    @Override
    public Long createRiskBlacklist(RiskBlacklistSaveReqVO createReqVO) {
        // 插入
        RiskBlacklistDO riskBlacklist = BeanUtils.toBean(createReqVO, RiskBlacklistDO.class);
        riskBlacklistMapper.insert(riskBlacklist);

        // 返回
        return riskBlacklist.getId();
    }

    @Override
    public void updateRiskBlacklist(RiskBlacklistSaveReqVO updateReqVO) {
        // 校验存在
        validateRiskBlacklistExists(updateReqVO.getId());
        // 更新
        RiskBlacklistDO updateObj = BeanUtils.toBean(updateReqVO, RiskBlacklistDO.class);
        riskBlacklistMapper.updateById(updateObj);
    }

    @Override
    public void deleteRiskBlacklist(Long id) {
        // 校验存在
        validateRiskBlacklistExists(id);
        // 删除
        riskBlacklistMapper.deleteById(id);
    }

    @Override
        public void deleteRiskBlacklistListByIds(List<Long> ids) {
        // 删除
        riskBlacklistMapper.deleteByIds(ids);
        }


    private void validateRiskBlacklistExists(Long id) {
        if (riskBlacklistMapper.selectById(id) == null) {
            throw exception(RISK_BLACKLIST_NOT_EXISTS);
        }
    }

    @Override
    public RiskBlacklistDO getRiskBlacklist(Long id) {
        return riskBlacklistMapper.selectById(id);
    }

    @Override
    public PageResult<RiskBlacklistDO> getRiskBlacklistPage(RiskBlacklistPageReqVO pageReqVO) {
        return riskBlacklistMapper.selectPage(pageReqVO);
    }

}