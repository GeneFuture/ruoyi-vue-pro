package cn.iocoder.yudao.module.maritime.service.enrollment;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.*;
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

}