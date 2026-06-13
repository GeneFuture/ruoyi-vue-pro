package cn.iocoder.yudao.module.maritime.service.refundApply;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 退费申请 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class RefundApplyServiceImpl implements RefundApplyService {

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Override
    public Long createRefundApply(RefundApplySaveReqVO createReqVO) {
        // 插入
        RefundApplyDO refundApply = BeanUtils.toBean(createReqVO, RefundApplyDO.class);
        refundApplyMapper.insert(refundApply);

        // 返回
        return refundApply.getId();
    }

    @Override
    public void updateRefundApply(RefundApplySaveReqVO updateReqVO) {
        // 校验存在
        validateRefundApplyExists(updateReqVO.getId());
        // 更新
        RefundApplyDO updateObj = BeanUtils.toBean(updateReqVO, RefundApplyDO.class);
        refundApplyMapper.updateById(updateObj);
    }

    @Override
    public void deleteRefundApply(Long id) {
        // 校验存在
        validateRefundApplyExists(id);
        // 删除
        refundApplyMapper.deleteById(id);
    }

    @Override
        public void deleteRefundApplyListByIds(List<Long> ids) {
        // 删除
        refundApplyMapper.deleteByIds(ids);
        }


    private void validateRefundApplyExists(Long id) {
        if (refundApplyMapper.selectById(id) == null) {
            throw exception(REFUND_APPLY_NOT_EXISTS);
        }
    }

    @Override
    public RefundApplyDO getRefundApply(Long id) {
        return refundApplyMapper.selectById(id);
    }

    @Override
    public PageResult<RefundApplyDO> getRefundApplyPage(RefundApplyPageReqVO pageReqVO) {
        return refundApplyMapper.selectPage(pageReqVO);
    }

}