package cn.iocoder.yudao.module.maritime.service.referralRelation;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 推荐关系 Service 接口
 *
 * @author Gene Ye
 */
public interface ReferralRelationService {

    /**
     * 创建推荐关系
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReferralRelation(@Valid ReferralRelationSaveReqVO createReqVO);

    /**
     * 更新推荐关系
     *
     * @param updateReqVO 更新信息
     */
    void updateReferralRelation(@Valid ReferralRelationSaveReqVO updateReqVO);

    /**
     * 删除推荐关系
     *
     * @param id 编号
     */
    void deleteReferralRelation(Long id);

    /**
    * 批量删除推荐关系
    *
    * @param ids 编号
    */
    void deleteReferralRelationListByIds(List<Long> ids);

    /**
     * 获得推荐关系
     *
     * @param id 编号
     * @return 推荐关系
     */
    ReferralRelationDO getReferralRelation(Long id);

    /**
     * 获得推荐关系分页
     *
     * @param pageReqVO 分页查询
     * @return 推荐关系分页
     */
    PageResult<ReferralRelationDO> getReferralRelationPage(ReferralRelationPageReqVO pageReqVO);

}