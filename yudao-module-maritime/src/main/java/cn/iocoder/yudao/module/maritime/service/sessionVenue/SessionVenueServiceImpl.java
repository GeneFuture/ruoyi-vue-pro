package cn.iocoder.yudao.module.maritime.service.sessionVenue;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue.vo.SessionVenueSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue.SessionVenueMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 班期场地信息 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
@Slf4j
public class SessionVenueServiceImpl implements SessionVenueService {

    @Resource
    private SessionVenueMapper sessionVenueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSessionVenue(SessionVenueSaveReqVO saveReqVO) {
        SessionVenueDO existing = sessionVenueMapper.selectBySessionId(saveReqVO.getSessionId());
        if (existing == null) {
            SessionVenueDO venue = BeanUtils.toBean(saveReqVO, SessionVenueDO.class);
            venue.setId(null);
            sessionVenueMapper.insert(venue);
        } else {
            SessionVenueDO updateObj = BeanUtils.toBean(saveReqVO, SessionVenueDO.class);
            updateObj.setId(existing.getId());
            sessionVenueMapper.updateById(updateObj);
        }
    }

    @Override
    public SessionVenueDO getSessionVenueBySessionId(Long sessionId) {
        return sessionVenueMapper.selectBySessionId(sessionId);
    }

}
