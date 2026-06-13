package cn.iocoder.yudao.module.maritime.service.enrollmentOrder;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 报名订单 Service 接口
 *
 * @author Gene Ye
 */
public interface EnrollmentOrderService {

    /**
     * 创建报名订单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEnrollmentOrder(@Valid EnrollmentOrderSaveReqVO createReqVO);

    /**
     * 更新报名订单
     *
     * @param updateReqVO 更新信息
     */
    void updateEnrollmentOrder(@Valid EnrollmentOrderSaveReqVO updateReqVO);

    /**
     * 删除报名订单
     *
     * @param id 编号
     */
    void deleteEnrollmentOrder(Long id);

    /**
    * 批量删除报名订单
    *
    * @param ids 编号
    */
    void deleteEnrollmentOrderListByIds(List<Long> ids);

    /**
     * 获得报名订单
     *
     * @param id 编号
     * @return 报名订单
     */
    EnrollmentOrderDO getEnrollmentOrder(Long id);

    /**
     * 获得报名订单分页
     *
     * @param pageReqVO 分页查询
     * @return 报名订单分页
     */
    PageResult<EnrollmentOrderDO> getEnrollmentOrderPage(EnrollmentOrderPageReqVO pageReqVO);

}