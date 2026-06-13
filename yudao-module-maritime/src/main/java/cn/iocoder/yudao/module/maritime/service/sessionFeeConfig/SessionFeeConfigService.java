package cn.iocoder.yudao.module.maritime.service.sessionFeeConfig;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 班期费用配置 Service 接口
 *
 * @author Gene Ye
 */
public interface SessionFeeConfigService {

    /**
     * 创建班期费用配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSessionFeeConfig(@Valid SessionFeeConfigSaveReqVO createReqVO);

    /**
     * 更新班期费用配置
     *
     * @param updateReqVO 更新信息
     */
    void updateSessionFeeConfig(@Valid SessionFeeConfigSaveReqVO updateReqVO);

    /**
     * 删除班期费用配置
     *
     * @param id 编号
     */
    void deleteSessionFeeConfig(Long id);

    /**
    * 批量删除班期费用配置
    *
    * @param ids 编号
    */
    void deleteSessionFeeConfigListByIds(List<Long> ids);

    /**
     * 获得班期费用配置
     *
     * @param id 编号
     * @return 班期费用配置
     */
    SessionFeeConfigDO getSessionFeeConfig(Long id);

    /**
     * 获得班期费用配置分页
     *
     * @param pageReqVO 分页查询
     * @return 班期费用配置分页
     */
    PageResult<SessionFeeConfigDO> getSessionFeeConfigPage(SessionFeeConfigPageReqVO pageReqVO);

    /**
     * 按 sessionId 保存费用配置（存在则更新，不存在则创建）
     *
     * @param saveReqVO 费用配置信息（sessionId 必传）
     */
    void saveFeeConfig(@Valid SessionFeeConfigSaveReqVO saveReqVO);

    /**
     * 按班期ID查询费用配置
     *
     * @param sessionId 班期编号
     * @return 费用配置，不存在返回 null
     */
    SessionFeeConfigDO getFeeConfigBySessionId(Long sessionId);

}