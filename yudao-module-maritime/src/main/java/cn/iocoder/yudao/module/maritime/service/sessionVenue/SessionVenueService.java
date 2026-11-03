package cn.iocoder.yudao.module.maritime.service.sessionVenue;

import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;

/**
 * 班期场地信息 Service 接口
 *
 * @author Gene Ye
 */
public interface SessionVenueService {

    /**
     * 按 sessionId 保存场地信息（存在则更新，不存在则创建）
     *
     * @param saveReqVO 场地信息（sessionId 必传）
     */
    void saveSessionVenue(@Valid SessionVenueSaveReqVO saveReqVO);

    /**
     * 按班期ID查询场地信息
     *
     * @param sessionId 班期编号
     * @return 场地信息，不存在返回 null
     */
    SessionVenueDO getSessionVenueBySessionId(Long sessionId);

}
