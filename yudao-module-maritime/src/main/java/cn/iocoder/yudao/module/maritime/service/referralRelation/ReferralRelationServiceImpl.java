package cn.iocoder.yudao.module.maritime.service.referralRelation;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.referralRelation.ReferralRelationMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public Long createReferralRelation(ReferralRelationSaveReqVO createReqVO) {
        // 插入
        ReferralRelationDO referralRelation = BeanUtils.toBean(createReqVO, ReferralRelationDO.class);
        referralRelationMapper.insert(referralRelation);

        // 返回
        return referralRelation.getId();
    }

    @Override
    public void updateReferralRelation(ReferralRelationSaveReqVO updateReqVO) {
        // 校验存在
        validateReferralRelationExists(updateReqVO.getId());
        // 更新
        ReferralRelationDO updateObj = BeanUtils.toBean(updateReqVO, ReferralRelationDO.class);
        referralRelationMapper.updateById(updateObj);
    }

    @Override
    public void deleteReferralRelation(Long id) {
        // 校验存在
        validateReferralRelationExists(id);
        // 删除
        referralRelationMapper.deleteById(id);
    }

    @Override
    public void deleteReferralRelationListByIds(List<Long> ids) {
        // 删除
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

}