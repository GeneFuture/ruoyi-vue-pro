package cn.iocoder.yudao.module.maritime.dal.mysql.commissionRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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
                .likeIfPresent(CommissionRecordDO::getApproveRemark, reqVO.getApproveRemark())
                .betweenIfPresent(CommissionRecordDO::getPayoutTime, reqVO.getPayoutTime())
                .eqIfPresent(CommissionRecordDO::getPayTransferId, reqVO.getPayTransferId())
                .eqIfPresent(CommissionRecordDO::getFailReason, reqVO.getFailReason())
                .eqIfPresent(CommissionRecordDO::getRetryCount, reqVO.getRetryCount())
                .betweenIfPresent(CommissionRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CommissionRecordDO::getId));
    }

    /** 幂等检查：同一报名是否已有佣金记录 */
    default boolean existsByReferredEnrollmentId(Long enrollmentId) {
        return selectCount(CommissionRecordDO::getReferredEnrollmentId, enrollmentId) > 0;
    }

    /** 查推荐人的佣金记录分页（App 端） */
    default PageResult<CommissionRecordDO> selectPageByReferrerMemberId(Long memberId, cn.iocoder.yudao.framework.common.pojo.PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CommissionRecordDO>()
                .eq(CommissionRecordDO::getReferrerMemberId, memberId)
                .orderByDesc(CommissionRecordDO::getId));
    }

    /** CAS 审核通过：PENDING_REVIEW → PENDING_PAYOUT */
    default int approveCommissionCas(Long id, String expectedStatus, String newStatus) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CommissionRecordDO>()
                .set(CommissionRecordDO::getCommissionStatus, newStatus)
                .eq(CommissionRecordDO::getId, id)
                .eq(CommissionRecordDO::getCommissionStatus, expectedStatus)
                .eq(CommissionRecordDO::getDeleted, false));
    }

    /** 按被推荐报名ID查佣金记录（退款冻结用） */
    default CommissionRecordDO selectByReferredEnrollmentId(Long enrollmentId) {
        return selectOne(CommissionRecordDO::getReferredEnrollmentId, enrollmentId);
    }

    /** 直接更新佣金状态（退款冻结用，不需要 CAS） */
    default int updateStatusById(Long id, String newStatus) {
        return update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CommissionRecordDO>()
                .set(CommissionRecordDO::getCommissionStatus, newStatus)
                .eq(CommissionRecordDO::getId, id)
                .eq(CommissionRecordDO::getDeleted, false));
    }

    /** 分页查询 WAITING_FOR_CLASS 且预计结算日 ≤ today 的记录（供结算检查 Job 分批处理） */
    @Select("SELECT * FROM maritime_commission_record WHERE commission_status = 'WAITING_FOR_CLASS' " +
            "AND expected_settle_date <= #{today} AND deleted = 0 ORDER BY id ASC LIMIT #{offset}, #{limit}")
    List<CommissionRecordDO> selectWaitingForClassAndDue(@Param("today") LocalDate today,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    /** 分页查询 PENDING_PAYOUT 且审核通过时间 ≤ threshold（T+1 发放用） */
    @Select("SELECT * FROM maritime_commission_record WHERE commission_status = 'PENDING_PAYOUT' " +
            "AND approve_time <= #{threshold} AND deleted = 0 ORDER BY id ASC LIMIT #{offset}, #{limit}")
    List<CommissionRecordDO> selectPendingPayoutBefore(@Param("threshold") LocalDateTime threshold,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);

    /** 进入审核：更新状态 + 记录 settleCheckTime（CAS WAITING_FOR_CLASS → newStatus） */
    @Update("UPDATE maritime_commission_record SET commission_status = #{newStatus}, " +
            "settle_check_time = #{now} WHERE id = #{id} AND commission_status = 'WAITING_FOR_CLASS' AND deleted = 0")
    int updateStatusAndSettleTime(@Param("id") Long id,
                                  @Param("newStatus") String newStatus,
                                  @Param("now") LocalDateTime now);

    /** CAS 发放前占位：PENDING_PAYOUT → PAYING（防并发重复发放） */
    @Update("UPDATE maritime_commission_record SET commission_status = 'PAYING' " +
            "WHERE id = #{id} AND commission_status = 'PENDING_PAYOUT' AND deleted = 0")
    int updateStatusIfPendingPayout(@Param("id") Long id);

    /** 发放成功：更新 PAID + payoutTime + payTransferId */
    @Update("UPDATE maritime_commission_record SET commission_status = 'PAID', " +
            "payout_time = #{payoutTime}, pay_transfer_id = #{payTransferId} " +
            "WHERE id = #{id} AND deleted = 0")
    int updatePaid(@Param("id") Long id,
                   @Param("payoutTime") LocalDateTime payoutTime,
                   @Param("payTransferId") Long payTransferId);

    /** 发放失败：更新 FAILED + failReason */
    @Update("UPDATE maritime_commission_record SET commission_status = 'FAILED', " +
            "fail_reason = #{failReason} WHERE id = #{id} AND deleted = 0")
    int updateFailed(@Param("id") Long id, @Param("failReason") String failReason);

    /** 统计指定状态的佣金数量（Dashboard 待办用） */
    @Select("SELECT COUNT(*) FROM maritime_commission_record WHERE commission_status = #{status} AND deleted = 0")
    int countByStatus(@Param("status") String status);

    /** 统计指定时间段内已发放佣金总额（Dashboard/财务汇总用） */
    @Select("SELECT COALESCE(SUM(commission_amount), 0) FROM maritime_commission_record " +
            "WHERE commission_status = 'PAID' AND payout_time >= #{start} AND payout_time <= #{end} AND deleted = 0")
    BigDecimal sumPaidByPayoutTimeBetween(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /** 统计指定时间段内已发放佣金笔数（财务汇总用） */
    @Select("SELECT COUNT(*) FROM maritime_commission_record " +
            "WHERE commission_status = 'PAID' AND payout_time >= #{start} AND payout_time <= #{end} AND deleted = 0")
    int countPaidByPayoutTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 预查佣金导出行数（行数限制前置检查用） */
    @Select("<script>SELECT COUNT(*) FROM maritime_commission_record WHERE deleted = 0 " +
            "<if test='status != null and status != \"\"'>AND commission_status = #{status} </if>" +
            "<if test='start != null'>AND trigger_time &gt;= #{start} </if>" +
            "<if test='end != null'>AND trigger_time &lt;= #{end} </if></script>")
    int countForExport(@Param("status") String status,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);

    /** 查询佣金记录列表（Excel导出用），支持时间范围和状态过滤，限制最大行数防止全表扫描 */
    @Select("<script>SELECT * FROM maritime_commission_record WHERE deleted = 0 " +
            "<if test='status != null and status != \"\"'>AND commission_status = #{status} </if>" +
            "<if test='start != null'>AND trigger_time &gt;= #{start} </if>" +
            "<if test='end != null'>AND trigger_time &lt;= #{end} </if>" +
            "ORDER BY id ASC LIMIT #{maxRows}</script>")
    List<CommissionRecordDO> selectListForExport(@Param("status") String status,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("maxRows") int maxRows);

    /** 推荐人排行榜：统计各推荐人在指定时间段内的 ACTIVE 推荐数，取前N名 */
    @Select("SELECT referrer_member_id, COUNT(*) AS cnt FROM maritime_commission_record " +
            "WHERE commission_status IN ('PAID', 'PENDING_PAYOUT', 'PAYING', 'FROZEN', 'PENDING_REVIEW', 'REVIEWING') " +
            "AND trigger_time >= #{since} AND deleted = 0 " +
            "GROUP BY referrer_member_id ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> selectLeaderboard(@Param("since") LocalDateTime since, @Param("limit") int limit);

}