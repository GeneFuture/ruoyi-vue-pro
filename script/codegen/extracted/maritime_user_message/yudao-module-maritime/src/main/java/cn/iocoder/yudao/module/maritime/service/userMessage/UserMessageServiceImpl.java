package cn.iocoder.yudao.module.maritime.service.userMessage;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.userMessage.UserMessageDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.userMessage.UserMessageMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 站内消息 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageMapper userMessageMapper;

    @Override
    public Long createUserMessage(UserMessageSaveReqVO createReqVO) {
        // 插入
        UserMessageDO userMessage = BeanUtils.toBean(createReqVO, UserMessageDO.class);
        userMessageMapper.insert(userMessage);

        // 返回
        return userMessage.getId();
    }

    @Override
    public void updateUserMessage(UserMessageSaveReqVO updateReqVO) {
        // 校验存在
        validateUserMessageExists(updateReqVO.getId());
        // 更新
        UserMessageDO updateObj = BeanUtils.toBean(updateReqVO, UserMessageDO.class);
        userMessageMapper.updateById(updateObj);
    }

    @Override
    public void deleteUserMessage(Long id) {
        // 校验存在
        validateUserMessageExists(id);
        // 删除
        userMessageMapper.deleteById(id);
    }

    @Override
        public void deleteUserMessageListByIds(List<Long> ids) {
        // 删除
        userMessageMapper.deleteByIds(ids);
        }


    private void validateUserMessageExists(Long id) {
        if (userMessageMapper.selectById(id) == null) {
            throw exception(USER_MESSAGE_NOT_EXISTS);
        }
    }

    @Override
    public UserMessageDO getUserMessage(Long id) {
        return userMessageMapper.selectById(id);
    }

    @Override
    public PageResult<UserMessageDO> getUserMessagePage(UserMessagePageReqVO pageReqVO) {
        return userMessageMapper.selectPage(pageReqVO);
    }

}