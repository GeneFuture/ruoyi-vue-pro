package cn.iocoder.yudao.module.maritime.service.groupon;

import cn.iocoder.yudao.module.maritime.controller.app.groupon.vo.AppGrouponStatusRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;

import java.util.List;

/**
 * 拼团业务 Service 接口
 *
 * @author Gene Ye
 */
public interface GrouponService {

    /**
     * 加入或创建拼团（由报名流程调用）
     *
     * @param enrollmentId 报名ID（已创建）
     * @param memberId     学员ID
     * @param inviteCode   邀请码（null=新建拼团；非null=加入已有拼团）
     * @param sessionId    班期ID
     * @param feeConfig    班期费用配置
     * @return 拼团处理结果（grouponRecordId + inviteCode）
     */
    GrouponProcessResult joinOrCreateGroupon(Long enrollmentId, Long memberId, String inviteCode,
                                             Long sessionId, SessionFeeConfigDO feeConfig);

    /**
     * 定金支付成功后处理拼团（由支付回调调用）
     *
     * @param enrollmentId 报名ID
     */
    void handleMemberPaid(Long enrollmentId);

    /**
     * 拼团成功（CAS 幂等）
     *
     * @param grouponRecordId 拼团记录ID
     */
    void succeedGroupon(Long grouponRecordId);

    /**
     * 拼团降级（超时未成团，CAS 幂等）
     *
     * @param grouponRecordId 拼团记录ID
     */
    void degradeGroupon(Long grouponRecordId);

    /**
     * 小程序端：获取班期当前进行中的拼团列表
     *
     * @param sessionId 班期ID
     * @return 活跃拼团列表
     */
    List<AppActiveGrouponVO> getActiveGroupons(Long sessionId);

    /**
     * 小程序端：查询我的拼团状态
     *
     * @param enrollmentId 报名ID
     * @param memberId     当前登录学员ID
     * @return 拼团状态
     */
    AppGrouponStatusRespVO getMyGrouponStatus(Long enrollmentId, Long memberId);

    /**
     * 管理端：强制成团（人工特批）
     *
     * @param grouponRecordId 拼团记录ID
     */
    void forceSucceedGroupon(Long grouponRecordId);

    /**
     * 管理端：关闭拼团（降级处理）
     *
     * @param grouponRecordId 拼团记录ID
     */
    void closeGroupon(Long grouponRecordId);

}
