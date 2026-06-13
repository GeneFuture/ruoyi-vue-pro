package cn.iocoder.yudao.module.maritime.service.userMessage;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.userMessage.UserMessageDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 站内消息 Service 接口
 *
 * @author Gene Ye
 */
public interface UserMessageService {

    /**
     * 创建站内消息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserMessage(@Valid UserMessageSaveReqVO createReqVO);

    /**
     * 更新站内消息
     *
     * @param updateReqVO 更新信息
     */
    void updateUserMessage(@Valid UserMessageSaveReqVO updateReqVO);

    /**
     * 删除站内消息
     *
     * @param id 编号
     */
    void deleteUserMessage(Long id);

    /**
    * 批量删除站内消息
    *
    * @param ids 编号
    */
    void deleteUserMessageListByIds(List<Long> ids);

    /**
     * 获得站内消息
     *
     * @param id 编号
     * @return 站内消息
     */
    UserMessageDO getUserMessage(Long id);

    /**
     * 获得站内消息分页
     *
     * @param pageReqVO 分页查询
     * @return 站内消息分页
     */
    PageResult<UserMessageDO> getUserMessagePage(UserMessagePageReqVO pageReqVO);

}