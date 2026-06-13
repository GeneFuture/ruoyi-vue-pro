package cn.iocoder.yudao.module.maritime.dal.mysql.announcement;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.announcement.AnnouncementDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo.*;

/**
 * 最新动态 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface AnnouncementMapper extends BaseMapperX<AnnouncementDO> {

    default PageResult<AnnouncementDO> selectPage(AnnouncementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AnnouncementDO>()
                .eqIfPresent(AnnouncementDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AnnouncementDO::getContent, reqVO.getContent())
                .eqIfPresent(AnnouncementDO::getSummary, reqVO.getSummary())
                .eqIfPresent(AnnouncementDO::getCoverImage, reqVO.getCoverImage())
                .eqIfPresent(AnnouncementDO::getType, reqVO.getType())
                .eqIfPresent(AnnouncementDO::getExternalUrl, reqVO.getExternalUrl())
                .betweenIfPresent(AnnouncementDO::getPublishTime, reqVO.getPublishTime())
                .eqIfPresent(AnnouncementDO::getIsTop, reqVO.getIsTop())
                .eqIfPresent(AnnouncementDO::getTopOrder, reqVO.getTopOrder())
                .eqIfPresent(AnnouncementDO::getSortOrder, reqVO.getSortOrder())
                .eqIfPresent(AnnouncementDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AnnouncementDO::getViewCount, reqVO.getViewCount())
                .betweenIfPresent(AnnouncementDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AnnouncementDO::getId));
    }

}