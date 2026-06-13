package cn.iocoder.yudao.module.maritime.service.announcement;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 最新动态 Service 接口
 *
 * @author Gene Ye
 */
public interface AnnouncementService {

    /**
     * 创建最新动态
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAnnouncement(@Valid AnnouncementSaveReqVO createReqVO);

    /**
     * 更新最新动态
     *
     * @param updateReqVO 更新信息
     */
    void updateAnnouncement(@Valid AnnouncementSaveReqVO updateReqVO);

    /**
     * 删除最新动态
     *
     * @param id 编号
     */
    void deleteAnnouncement(Long id);

    /**
    * 批量删除最新动态
    *
    * @param ids 编号
    */
    void deleteAnnouncementListByIds(List<Long> ids);

    /**
     * 获得最新动态
     *
     * @param id 编号
     * @return 最新动态
     */
    AnnouncementDO getAnnouncement(Long id);

    /**
     * 获得最新动态分页
     *
     * @param pageReqVO 分页查询
     * @return 最新动态分页
     */
    PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO);

}