package cn.iocoder.yudao.module.maritime.service.enrollment;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayBalanceReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderStatusRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 报名管理 Service 接口
 *
 * @author Gene Ye
 */
public interface EnrollmentService {

    /**
     * 创建报名管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEnrollment(@Valid EnrollmentSaveReqVO createReqVO);

    /**
     * 更新报名管理
     *
     * @param updateReqVO 更新信息
     */
    void updateEnrollment(@Valid EnrollmentSaveReqVO updateReqVO);

    /**
     * 删除报名管理
     *
     * @param id 编号
     */
    void deleteEnrollment(Long id);

    /**
    * 批量删除报名管理
    *
    * @param ids 编号
    */
    void deleteEnrollmentListByIds(List<Long> ids);

    /**
     * 获得报名管理
     *
     * @param id 编号
     * @return 报名管理
     */
    EnrollmentDO getEnrollment(Long id);

    /**
     * 获得报名管理分页
     *
     * @param pageReqVO 分页查询
     * @return 报名管理分页
     */
    PageResult<EnrollmentDO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO);

    // ========== 小程序端 T03 ==========

    /**
     * 小程序端：创建报名（含定金订单、CAS 占座）
     *
     * @param createReqVO 报名信息
     * @param memberId    当前登录学员ID
     * @return 报名结果（enrollmentId、orderId、金额、过期时间）
     */
    AppEnrollmentCreateRespVO createEnrollmentForApp(@Valid AppEnrollmentCreateReqVO createReqVO, Long memberId);

    /**
     * 小程序端：获取我的报名列表
     *
     * @param memberId 当前登录学员ID
     * @return 报名列表（含班期摘要信息）
     */
    List<AppEnrollmentListRespVO> getMyEnrollments(Long memberId);

    /**
     * 小程序端：获取报名详情（仅限本人）
     *
     * @param id       报名ID
     * @param memberId 当前登录学员ID
     * @return 报名详情（含脱敏个人信息 + 订单列表）
     */
    AppEnrollmentDetailRespVO getEnrollmentForApp(Long id, Long memberId);

    /**
     * 小程序端：取消报名（仅 PENDING_DEPOSIT 状态可取消）
     *
     * @param id       报名ID
     * @param memberId 当前登录学员ID
     */
    void cancelEnrollmentForApp(Long id, Long memberId);

    // ========== 后台管理端 T03 ==========

    /**
     * 管理端：人工确认报名（处理异常情况）
     *
     * @param id 报名ID
     */
    void confirmEnrollmentByAdmin(Long id);

    /**
     * 管理端：取消报名（需填写原因）
     *
     * @param id           报名ID
     * @param cancelReason 取消原因
     */
    void cancelEnrollmentByAdmin(Long id, String cancelReason);

    // ========== 小程序端 T04 ==========

    /**
     * 小程序端：发起定金支付（创建 pay 模块订单，返回 payOrderId）
     *
     * @param orderId  定金订单ID（EnrollmentOrder.id）
     * @param memberId 当前登录学员ID
     * @return payOrderId 及应付金额
     */
    AppPayOrderRespVO payDeposit(Long orderId, Long memberId);

    /**
     * 定金支付成功回调（由 pay 模块调用）
     *
     * @param merchantOrderId 商户订单号（即 EnrollmentOrder.orderNo）
     * @param payOrderId      pay 模块支付单ID
     */
    void handleDepositPaid(String merchantOrderId, Long payOrderId);

    /**
     * 关闭过期的定金订单（CAS 幂等，供定时任务调用）
     *
     * @param orderId 定金订单ID
     */
    void closeExpiredOrder(Long orderId);

    /**
     * 小程序端：查询定金支付结果（前端轮询，处理微信回调延迟）
     *
     * @param payOrderId pay 模块支付单ID
     * @param memberId   当前登录学员ID
     * @return 支付状态及报名ID
     */
    AppPayOrderStatusRespVO getPayOrderStatus(Long payOrderId, Long memberId);

    // ========== 后台管理端 T04 ==========

    /**
     * 管理端：手动确认定金支付（用于微信回调异常但实际已付款的情况）
     *
     * @param orderId 定金订单ID（EnrollmentOrder.id）
     */
    void manualConfirmDeposit(Long orderId);

    // ========== 小程序端 T07 ==========

    /**
     * 小程序端：发起尾款支付
     *
     * @param reqVO    请求参数（enrollmentId）
     * @param memberId 当前登录学员ID
     * @return pay 模块订单ID 及应付金额
     */
    AppPayOrderRespVO payBalance(@Valid AppPayBalanceReqVO reqVO, Long memberId);

    /**
     * 尾款支付成功回调（由 pay 模块调用）
     *
     * @param merchantOrderId 商户订单号（即 EnrollmentOrder.orderNo）
     * @param payOrderId      pay 模块支付单ID
     */
    void handleBalancePaid(String merchantOrderId, Long payOrderId);

    /**
     * 小程序端：申请退费
     *
     * @param reqVO    退费申请参数
     * @param memberId 当前登录学员ID
     */
    void applyRefund(@Valid AppRefundApplyReqVO reqVO, Long memberId);

    /**
     * 小程序端：申请转课
     *
     * @param reqVO    转课申请参数
     * @param memberId 当前登录学员ID
     */
    void applyTransfer(@Valid AppTransferApplyReqVO reqVO, Long memberId);

    /**
     * 小程序端：查询退费状态
     *
     * @param enrollmentId 报名ID
     * @param memberId     当前登录学员ID
     * @return 退费申请状态（无申请时返回 null）
     */
    AppRefundStatusRespVO getRefundStatus(Long enrollmentId, Long memberId);

}