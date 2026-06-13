package cn.iocoder.yudao.module.maritime.dal.mysql.refundApply;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;

/**
 * 退费申请 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface RefundApplyMapper extends BaseMapperX<RefundApplyDO> {

    default PageResult<RefundApplyDO> selectPage(RefundApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RefundApplyDO>()
                .eqIfPresent(RefundApplyDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(RefundApplyDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(RefundApplyDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(RefundApplyDO::getApplyReason, reqVO.getApplyReason())
                .eqIfPresent(RefundApplyDO::getRefundAmount, reqVO.getRefundAmount())
                .eqIfPresent(RefundApplyDO::getApplyStatus, reqVO.getApplyStatus())
                .eqIfPresent(RefundApplyDO::getAdminRemark, reqVO.getAdminRemark())
                .betweenIfPresent(RefundApplyDO::getApproveTime, reqVO.getApproveTime())
                .eqIfPresent(RefundApplyDO::getApproverId, reqVO.getApproverId())
                .eqIfPresent(RefundApplyDO::getPayRefundId, reqVO.getPayRefundId())
                .betweenIfPresent(RefundApplyDO::getRefundTime, reqVO.getRefundTime())
                .betweenIfPresent(RefundApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(RefundApplyDO::getId));
    }

}