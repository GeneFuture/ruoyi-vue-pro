package cn.iocoder.yudao.module.maritime.dal.mysql.enrollment;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;

/**
 * 报名管理 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface EnrollmentMapper extends BaseMapperX<EnrollmentDO> {

    default PageResult<EnrollmentDO> selectPage(EnrollmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnrollmentDO>()
                .eqIfPresent(EnrollmentDO::getEnrollmentNo, reqVO.getEnrollmentNo())
                .eqIfPresent(EnrollmentDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(EnrollmentDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(EnrollmentDO::getGrouponRecordId, reqVO.getGrouponRecordId())
                .eqIfPresent(EnrollmentDO::getReferredByMemberId, reqVO.getReferredByMemberId())
                .eqIfPresent(EnrollmentDO::getReferralCodeUsed, reqVO.getReferralCodeUsed())
                .likeIfPresent(EnrollmentDO::getRealName, reqVO.getRealName())
                .eqIfPresent(EnrollmentDO::getIdCard, reqVO.getIdCard())
                .eqIfPresent(EnrollmentDO::getPhone, reqVO.getPhone())
                .eqIfPresent(EnrollmentDO::getTotalAmount, reqVO.getTotalAmount())
                .eqIfPresent(EnrollmentDO::getDepositAmountSnapshot, reqVO.getDepositAmountSnapshot())
                .eqIfPresent(EnrollmentDO::getBalanceAmount, reqVO.getBalanceAmount())
                .eqIfPresent(EnrollmentDO::getPaidAmount, reqVO.getPaidAmount())
                .betweenIfPresent(EnrollmentDO::getBalanceDueDate, reqVO.getBalanceDueDate())
                .eqIfPresent(EnrollmentDO::getEnrollmentStatus, reqVO.getEnrollmentStatus())
                .eqIfPresent(EnrollmentDO::getReferralRightGranted, reqVO.getReferralRightGranted())
                .eqIfPresent(EnrollmentDO::getCancelReason, reqVO.getCancelReason())
                .betweenIfPresent(EnrollmentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnrollmentDO::getId));
    }

}