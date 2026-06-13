package cn.iocoder.yudao.module.maritime.service.grouponRecord;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 拼团记录 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class GrouponRecordServiceImpl implements GrouponRecordService {

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Override
    public Long createGrouponRecord(GrouponRecordSaveReqVO createReqVO) {
        // 插入
        GrouponRecordDO grouponRecord = BeanUtils.toBean(createReqVO, GrouponRecordDO.class);
        grouponRecordMapper.insert(grouponRecord);

        // 返回
        return grouponRecord.getId();
    }

    @Override
    public void updateGrouponRecord(GrouponRecordSaveReqVO updateReqVO) {
        // 校验存在
        validateGrouponRecordExists(updateReqVO.getId());
        // 更新
        GrouponRecordDO updateObj = BeanUtils.toBean(updateReqVO, GrouponRecordDO.class);
        grouponRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteGrouponRecord(Long id) {
        // 校验存在
        validateGrouponRecordExists(id);
        // 删除
        grouponRecordMapper.deleteById(id);
    }

    @Override
        public void deleteGrouponRecordListByIds(List<Long> ids) {
        // 删除
        grouponRecordMapper.deleteByIds(ids);
        }


    private void validateGrouponRecordExists(Long id) {
        if (grouponRecordMapper.selectById(id) == null) {
            throw exception(GROUPON_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public GrouponRecordDO getGrouponRecord(Long id) {
        return grouponRecordMapper.selectById(id);
    }

    @Override
    public PageResult<GrouponRecordDO> getGrouponRecordPage(GrouponRecordPageReqVO pageReqVO) {
        return grouponRecordMapper.selectPage(pageReqVO);
    }

}