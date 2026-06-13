package cn.iocoder.yudao.module.maritime.dal.mysql.userMessage;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.userMessage.UserMessageDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo.*;

/**
 * 站内消息 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface UserMessageMapper extends BaseMapperX<UserMessageDO> {

    default PageResult<UserMessageDO> selectPage(UserMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserMessageDO>()
                .eqIfPresent(UserMessageDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(UserMessageDO::getType, reqVO.getType())
                .eqIfPresent(UserMessageDO::getTitle, reqVO.getTitle())
                .eqIfPresent(UserMessageDO::getContent, reqVO.getContent())
                .eqIfPresent(UserMessageDO::getRelatedId, reqVO.getRelatedId())
                .eqIfPresent(UserMessageDO::getRelatedType, reqVO.getRelatedType())
                .eqIfPresent(UserMessageDO::getIsRead, reqVO.getIsRead())
                .betweenIfPresent(UserMessageDO::getReadTime, reqVO.getReadTime())
                .betweenIfPresent(UserMessageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserMessageDO::getId));
    }

}