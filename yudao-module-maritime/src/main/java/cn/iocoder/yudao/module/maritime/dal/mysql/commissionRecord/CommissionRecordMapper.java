package cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.*;

/**
 * 佣金记录 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface CommissionRecordMapper extends BaseMapperX<CommissionRecordDO> {

    default PageResult<CommissionRecordDO> selectPage(CommissionRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CommissionRecordDO>()
                .eqIfPresent(CommissionRecordDO::getReferrerMemberId, reqVO.getReferrerMemberId())
                .eqIfPresent(CommissionRecordDO::getReferredEnrollmentId, reqVO.getReferredEnrollmentId())
                .eqIfPresent(CommissionRecordDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(CommissionRecordDO::getCommissionAmount, reqVO.getCommissionAmount())
                .eqIfPresent(CommissionRecordDO::getCommissionStatus, reqVO.getCommissionStatus())
                .betweenIfPresent(CommissionRecordDO::getExpectedSettleDate, reqVO.getExpectedSettleDate())
                .eqIfPresent(CommissionRecordDO::getIsRiskFlagged, reqVO.getIsRiskFlagged())
                .eqIfPresent(CommissionRecordDO::getRiskCheckResult, reqVO.getRiskCheckResult())
                .betweenIfPresent(CommissionRecordDO::getTriggerTime, reqVO.getTriggerTime())
                .betweenIfPresent(CommissionRecordDO::getSettleCheckTime, reqVO.getSettleCheckTime())
                .betweenIfPresent(CommissionRecordDO::getApproveTime, reqVO.getApproveTime())
                .eqIfPresent(CommissionRecordDO::getApproverId, reqVO.getApproverId())
                .eqIfPresent(CommissionRecordDO::getApproveRemark, reqVO.getApproveRemark())
                .betweenIfPresent(CommissionRecordDO::getPayoutTime, reqVO.getPayoutTime())
                .eqIfPresent(CommissionRecordDO::getPayTransferId, reqVO.getPayTransferId())
                .eqIfPresent(CommissionRecordDO::getFailReason, reqVO.getFailReason())
                .eqIfPresent(CommissionRecordDO::getRetryCount, reqVO.getRetryCount())
                .betweenIfPresent(CommissionRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CommissionRecordDO::getId));
    }

}