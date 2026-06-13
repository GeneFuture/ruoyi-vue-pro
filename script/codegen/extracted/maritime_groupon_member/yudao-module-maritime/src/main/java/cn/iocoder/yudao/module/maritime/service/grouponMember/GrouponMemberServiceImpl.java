package cn.iocoder.yudao.module.maritime.service.grouponMember;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember.GrouponMemberMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 拼团成员 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class GrouponMemberServiceImpl implements GrouponMemberService {

    @Resource
    private GrouponMemberMapper grouponMemberMapper;

    @Override
    public Long createGrouponMember(GrouponMemberSaveReqVO createReqVO) {
        // 插入
        GrouponMemberDO grouponMember = BeanUtils.toBean(createReqVO, GrouponMemberDO.class);
        grouponMemberMapper.insert(grouponMember);

        // 返回
        return grouponMember.getId();
    }

    @Override
    public void updateGrouponMember(GrouponMemberSaveReqVO updateReqVO) {
        // 校验存在
        validateGrouponMemberExists(updateReqVO.getId());
        // 更新
        GrouponMemberDO updateObj = BeanUtils.toBean(updateReqVO, GrouponMemberDO.class);
        grouponMemberMapper.updateById(updateObj);
    }

    @Override
    public void deleteGrouponMember(Long id) {
        // 校验存在
        validateGrouponMemberExists(id);
        // 删除
        grouponMemberMapper.deleteById(id);
    }

    @Override
        public void deleteGrouponMemberListByIds(List<Long> ids) {
        // 删除
        grouponMemberMapper.deleteByIds(ids);
        }


    private void validateGrouponMemberExists(Long id) {
        if (grouponMemberMapper.selectById(id) == null) {
            throw exception(GROUPON_MEMBER_NOT_EXISTS);
        }
    }

    @Override
    public GrouponMemberDO getGrouponMember(Long id) {
        return grouponMemberMapper.selectById(id);
    }

    @Override
    public PageResult<GrouponMemberDO> getGrouponMemberPage(GrouponMemberPageReqVO pageReqVO) {
        return grouponMemberMapper.selectPage(pageReqVO);
    }

}