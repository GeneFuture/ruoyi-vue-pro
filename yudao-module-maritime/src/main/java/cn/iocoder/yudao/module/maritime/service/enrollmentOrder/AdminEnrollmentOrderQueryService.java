package cn.iocoder.yudao.module.maritime.service.enrollmentOrder;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderRespVO;

/**
 * 管理后台 - 支付订单查询 Service 接口
 *
 * @author Gene Ye
 */
public interface AdminEnrollmentOrderQueryService {

    /**
     * 获得支付订单详情（含报名、学员、课程、班期信息）
     */
    EnrollmentOrderRespVO getEnrollmentOrder(Long id);

    /**
     * 获得支付订单分页（含报名、学员、课程、班期信息）
     */
    PageResult<EnrollmentOrderRespVO> getEnrollmentOrderPage(EnrollmentOrderPageReqVO pageReqVO);

}
