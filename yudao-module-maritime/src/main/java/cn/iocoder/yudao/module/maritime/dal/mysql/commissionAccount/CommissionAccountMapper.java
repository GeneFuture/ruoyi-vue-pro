package cn.iocoder.yudao.module.maritime.dal.mysql.commissionAccount;

import java.math.BigDecimal;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;

/**
 * 佣金账户 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface CommissionAccountMapper extends BaseMapperX<CommissionAccountDO> {

    default PageResult<CommissionAccountDO> selectPage(CommissionAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CommissionAccountDO>()
                .eqIfPresent(CommissionAccountDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(CommissionAccountDO::getTotalAmount, reqVO.getTotalAmount())
                .eqIfPresent(CommissionAccountDO::getPaidAmount, reqVO.getPaidAmount())
                .eqIfPresent(CommissionAccountDO::getPendingAmount, reqVO.getPendingAmount())
                .eqIfPresent(CommissionAccountDO::getFrozenAmount, reqVO.getFrozenAmount())
                .eqIfPresent(CommissionAccountDO::getRejectedAmount, reqVO.getRejectedAmount())
                .eqIfPresent(CommissionAccountDO::getTotalReferralCount, reqVO.getTotalReferralCount())
                .eqIfPresent(CommissionAccountDO::getVersion, reqVO.getVersion())
                .betweenIfPresent(CommissionAccountDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CommissionAccountDO::getId));
    }

    default CommissionAccountDO selectByMemberId(Long memberId) {
        return selectOne(CommissionAccountDO::getMemberId, memberId);
    }

    /** 进入审核时累加 pending_amount（乐观锁） */
    @Update("UPDATE maritime_commission_account " +
            "SET pending_amount = pending_amount + #{amount}, total_amount = total_amount + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND deleted = 0")
    int addPendingAmount(@Param("id") Long id,
                        @Param("amount") BigDecimal amount,
                        @Param("version") Integer version);

    /** 提现时冻结 pending_amount（乐观锁） */
    @Update("UPDATE maritime_commission_account " +
            "SET pending_amount = pending_amount - #{amount}, frozen_amount = frozen_amount + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND pending_amount >= #{amount} AND deleted = 0")
    int freezeAmount(@Param("id") Long id,
                     @Param("amount") BigDecimal amount,
                     @Param("version") Integer version);

    /** 发放成功：frozen_amount → paid_amount（乐观锁） */
    @Update("UPDATE maritime_commission_account " +
            "SET frozen_amount = frozen_amount - #{amount}, paid_amount = paid_amount + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND frozen_amount >= #{amount} AND deleted = 0")
    int confirmPayout(@Param("id") Long id,
                      @Param("amount") BigDecimal amount,
                      @Param("version") Integer version);

    /** 提现被拒绝：frozen_amount 归还 pending_amount（乐观锁） */
    @Update("UPDATE maritime_commission_account " +
            "SET frozen_amount = frozen_amount - #{amount}, pending_amount = pending_amount + #{amount}, " +
            "version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND frozen_amount >= #{amount} AND deleted = 0")
    int unfreezeAmount(@Param("id") Long id,
                       @Param("amount") BigDecimal amount,
                       @Param("version") Integer version);

}