package cn.iocoder.yudao.module.maritime.service.blacklist;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.blacklist.RiskBlacklistDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 风控黑名单 Service 接口
 *
 * @author Gene Ye
 */
public interface RiskBlacklistService {

    /**
     * 创建风控黑名单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRiskBlacklist(@Valid RiskBlacklistSaveReqVO createReqVO);

    /**
     * 更新风控黑名单
     *
     * @param updateReqVO 更新信息
     */
    void updateRiskBlacklist(@Valid RiskBlacklistSaveReqVO updateReqVO);

    /**
     * 删除风控黑名单
     *
     * @param id 编号
     */
    void deleteRiskBlacklist(Long id);

    /**
    * 批量删除风控黑名单
    *
    * @param ids 编号
    */
    void deleteRiskBlacklistListByIds(List<Long> ids);

    /**
     * 获得风控黑名单
     *
     * @param id 编号
     * @return 风控黑名单
     */
    RiskBlacklistDO getRiskBlacklist(Long id);

    /**
     * 获得风控黑名单分页
     *
     * @param pageReqVO 分页查询
     * @return 风控黑名单分页
     */
    PageResult<RiskBlacklistDO> getRiskBlacklistPage(RiskBlacklistPageReqVO pageReqVO);

}