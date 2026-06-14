package cn.iocoder.yudao.module.maritime.service.referralRelation;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.referral.vo.*;
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

    /**
     * 定金支付成功后，若报名填写了推荐码则建立推荐关系（幂等）
     *
     * @param enrollmentId 报名ID
     */
    void establishRelationIfPresent(Long enrollmentId);

    /**
     * 获得我的推荐中心汇总信息（App 端）
     *
     * @param memberId 会员ID
     * @return 汇总信息
     */
    AppReferralInfoRespVO getMyReferralInfo(Long memberId);

    /**
     * 获得分享参数（App 端，用于小程序分享卡片）
     *
     * @param memberId  会员ID
     * @param sessionId 班期ID（可选，指定班期才能展示课程信息）
     * @return 分享参数
     */
    AppShareParamsRespVO getShareParams(Long memberId, Long sessionId);

    /**
     * 获得我的推荐记录分页（App 端）
     *
     * @param memberId  会员ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<AppReferralRecordRespVO> getMyReferralList(Long memberId, PageParam pageParam);

    /**
     * 获得佣金账户汇总（App 端）
     *
     * @param memberId 会员ID
     * @return 账户汇总
     */
    AppCommissionAccountRespVO getMyCommissionAccount(Long memberId);

}