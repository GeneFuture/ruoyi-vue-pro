package cn.iocoder.yudao.module.maritime.service.announcement;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.announcement.AnnouncementMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 最新动态 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class AnnouncementServiceImpl implements AnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    @Override
    public Long createAnnouncement(AnnouncementSaveReqVO createReqVO) {
        // 插入
        AnnouncementDO announcement = BeanUtils.toBean(createReqVO, AnnouncementDO.class);
        announcementMapper.insert(announcement);

        // 返回
        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(AnnouncementSaveReqVO updateReqVO) {
        // 校验存在
        validateAnnouncementExists(updateReqVO.getId());
        // 更新
        AnnouncementDO updateObj = BeanUtils.toBean(updateReqVO, AnnouncementDO.class);
        announcementMapper.updateById(updateObj);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        // 校验存在
        validateAnnouncementExists(id);
        // 删除
        announcementMapper.deleteById(id);
    }

    @Override
    public void deleteAnnouncementListByIds(List<Long> ids) {
        // 删除
        announcementMapper.deleteByIds(ids);
    }


    private void validateAnnouncementExists(Long id) {
        if (announcementMapper.selectById(id) == null) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
    }

    @Override
    public AnnouncementDO getAnnouncement(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO) {
        return announcementMapper.selectPage(pageReqVO);
    }

}