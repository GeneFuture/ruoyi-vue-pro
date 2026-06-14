package cn.iocoder.yudao.module.maritime.dal.mysql.withdrawalApply;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalPageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.withdrawalApply.WithdrawalApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金提现申请 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface WithdrawalApplyMapper extends BaseMapperX<WithdrawalApplyDO> {

    default PageResult<WithdrawalApplyDO> selectPageByMemberId(Long memberId, PageParam pageParam) {
        return selectPage(pageParam, new LambdaQueryWrapperX<WithdrawalApplyDO>()
                .eq(WithdrawalApplyDO::getMemberId, memberId)
                .orderByDesc(WithdrawalApplyDO::getId));
    }

    /** 本月提现次数（所有终态，包含 SUCCESS/FAILED/REJECTED） */
    @Select("SELECT COUNT(*) FROM maritime_withdrawal_apply " +
            "WHERE member_id = #{memberId} AND deleted = 0 " +
            "AND DATE_FORMAT(create_time, '%Y-%m') = #{yearMonth}")
    int countByMemberAndMonth(@Param("memberId") Long memberId,
                              @Param("yearMonth") String yearMonth);

    /** 24 小时内累计成功提现金额 */
    @Select("SELECT COALESCE(SUM(apply_amount), 0) FROM maritime_withdrawal_apply " +
            "WHERE member_id = #{memberId} AND deleted = 0 " +
            "AND apply_status = 'SUCCESS' AND create_time >= #{since}")
    BigDecimal sumSuccessAmountSince(@Param("memberId") Long memberId,
                                    @Param("since") LocalDateTime since);

    /** 是否存在进行中的提现申请 */
    default boolean existsPendingByMember(Long memberId) {
        return selectCount(new LambdaQueryWrapperX<WithdrawalApplyDO>()
                .eq(WithdrawalApplyDO::getMemberId, memberId)
                .in(WithdrawalApplyDO::getApplyStatus, "PENDING", "PROCESSING")) > 0;
    }

    /** 管理端分页查询（支持 memberId / status / channel / createTime 过滤） */
    default PageResult<WithdrawalApplyDO> selectPage(AdminWithdrawalPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WithdrawalApplyDO>()
                .eqIfPresent(WithdrawalApplyDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(WithdrawalApplyDO::getApplyStatus, reqVO.getApplyStatus())
                .eqIfPresent(WithdrawalApplyDO::getChannel, reqVO.getChannel())
                .betweenIfPresent(WithdrawalApplyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WithdrawalApplyDO::getId));
    }

    /** CAS 拒绝提现（只允许从 PENDING 状态拒绝） */
    @Update("UPDATE maritime_withdrawal_apply SET apply_status = 'REJECTED', reject_reason = #{reason} " +
            "WHERE id = #{id} AND apply_status = 'PENDING' AND deleted = 0")
    int rejectIfPending(@Param("id") Long id, @Param("reason") String reason);

    /** CAS 退款：将冻结金额归还 pending_amount（由 service 调用 commissionAccountService） */
    @Update("UPDATE maritime_withdrawal_apply SET apply_status = 'REJECTED', reject_reason = #{reason} " +
            "WHERE id = #{id} AND deleted = 0")
    int updateRejected(@Param("id") Long id, @Param("reason") String reason);

}
