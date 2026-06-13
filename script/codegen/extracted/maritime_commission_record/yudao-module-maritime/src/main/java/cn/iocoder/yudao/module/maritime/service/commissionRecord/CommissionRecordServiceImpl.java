package cn.iocoder.yudao.module.maritime.service.commissionRecord;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord.CommissionRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 佣金记录 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class CommissionRecordServiceImpl implements CommissionRecordService {

    @Resource
    private CommissionRecordMapper commissionRecordMapper;

    @Override
    public Long createCommissionRecord(CommissionRecordSaveReqVO createReqVO) {
        // 插入
        CommissionRecordDO commissionRecord = BeanUtils.toBean(createReqVO, CommissionRecordDO.class);
        commissionRecordMapper.insert(commissionRecord);

        // 返回
        return commissionRecord.getId();
    }

    @Override
    public void updateCommissionRecord(CommissionRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateCommissionRecordExists(updateReqVO.getId());
        // 更新
        CommissionRecordDO updateObj = BeanUtils.toBean(updateReqVO, CommissionRecordDO.class);
        commissionRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteCommissionRecord(Long id) {
        // 校验存在
        validateCommissionRecordExists(id);
        // 删除
        commissionRecordMapper.deleteById(id);
    }

    @Override
        public void deleteCommissionRecordListByIds(List<Long> ids) {
        // 删除
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

}