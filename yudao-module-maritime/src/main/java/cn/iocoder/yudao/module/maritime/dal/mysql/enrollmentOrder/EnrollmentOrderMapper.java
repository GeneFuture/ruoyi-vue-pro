package cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;

/**
 * 报名订单 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface EnrollmentOrderMapper extends BaseMapperX<EnrollmentOrderDO> {

    default PageResult<EnrollmentOrderDO> selectPage(EnrollmentOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnrollmentOrderDO>()
                .eqIfPresent(EnrollmentOrderDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(EnrollmentOrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(EnrollmentOrderDO::getOrderType, reqVO.getOrderType())
                .eqIfPresent(EnrollmentOrderDO::getAmount, reqVO.getAmount())
                .eqIfPresent(EnrollmentOrderDO::getPayOrderId, reqVO.getPayOrderId())
                .eqIfPresent(EnrollmentOrderDO::getPayChannel, reqVO.getPayChannel())
                .eqIfPresent(EnrollmentOrderDO::getOrderStatus, reqVO.getOrderStatus())
                .betweenIfPresent(EnrollmentOrderDO::getPayTime, reqVO.getPayTime())
                .betweenIfPresent(EnrollmentOrderDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(EnrollmentOrderDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnrollmentOrderDO::getId));
    }

}