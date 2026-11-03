package cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.*;

/**
 * 拼团记录 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface GrouponRecordMapper extends BaseMapperX<GrouponRecordDO> {

    default PageResult<GrouponRecordDO> selectPage(GrouponRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GrouponRecordDO>()
                .eqIfPresent(GrouponRecordDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(GrouponRecordDO::getInviteCode, reqVO.getInviteCode())
                .eqIfPresent(GrouponRecordDO::getInitiatorEnrollmentId, reqVO.getInitiatorEnrollmentId())
                .eqIfPresent(GrouponRecordDO::getRequiredCount, reqVO.getRequiredCount())
                .eqIfPresent(GrouponRecordDO::getCurrentCount, reqVO.getCurrentCount())
                .eqIfPresent(GrouponRecordDO::getGrouponStatus, reqVO.getGrouponStatus())
                .betweenIfPresent(GrouponRecordDO::getExpireTime, reqVO.getExpireTime())
                .betweenIfPresent(GrouponRecordDO::getSuccessTime, reqVO.getSuccessTime())
                .betweenIfPresent(GrouponRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GrouponRecordDO::getId));
    }

    /** 查询某班期当前进行中的拼团（未过期），按创建时间升序 */
    default List<GrouponRecordDO> selectActiveBySessionId(Long sessionId) {
        return selectList(new LambdaQueryWrapperX<GrouponRecordDO>()
                .eq(GrouponRecordDO::getSessionId, sessionId)
                .eq(GrouponRecordDO::getGrouponStatus, "IN_PROGRESS")
                .gt(GrouponRecordDO::getExpireTime, LocalDateTime.now())
                .orderByAsc(GrouponRecordDO::getCreateTime));
    }

    /** 按邀请码查（加入拼团时用） */
    default GrouponRecordDO selectByInviteCode(String inviteCode) {
        return selectOne(GrouponRecordDO::getInviteCode, inviteCode);
    }

    /** 查已过期的 IN_PROGRESS 拼团（定时任务降级用） */
    default List<GrouponRecordDO> selectExpiredInProgress(LocalDateTime now) {
        return selectList(new LambdaQueryWrapperX<GrouponRecordDO>()
                .eq(GrouponRecordDO::getGrouponStatus, "IN_PROGRESS")
                .lt(GrouponRecordDO::getExpireTime, now));
    }

    /** 悲观锁查询（handleMemberPaid 内部序列化并发支付回调用） */
    @Select("SELECT * FROM maritime_groupon_record WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    GrouponRecordDO selectByIdForUpdate(@Param("id") Long id);

    /**
     * CAS 原子递增当前人数：同时校验进行中、未过期、未满员，
     * 避免预检通过后拼团刚好成团/超时/关闭仍被加入。
     */
    @Update("UPDATE maritime_groupon_record SET current_count = current_count + 1 " +
            "WHERE id = #{id} AND current_count < required_count " +
            "AND groupon_status = 'IN_PROGRESS' AND expire_time > NOW() AND deleted = 0")
    int incrementCount(@Param("id") Long id);

    /** 成员退出时原子递减人数（至少保留 1 人，进行中的拼团才允许操作） */
    @Update("UPDATE maritime_groupon_record SET current_count = current_count - 1 " +
            "WHERE id = #{id} AND current_count > 1 AND groupon_status = 'IN_PROGRESS' AND deleted = 0")
    int decrementCountForRelease(@Param("id") Long id);

    /** CAS IN_PROGRESS → DEGRADED（幂等，定时任务降级 / 管理员关闭 / 提前付清尾款用） */
    @Update("UPDATE maritime_groupon_record SET groupon_status = #{newStatus}, degrade_reason = #{reason} " +
            "WHERE id = #{id} AND groupon_status = 'IN_PROGRESS' AND deleted = 0")
    int updateStatusIfInProgress(@Param("id") Long id, @Param("newStatus") String newStatus,
                                 @Param("reason") String reason);

    /** CAS IN_PROGRESS → SUCCESS，同时写入 success_time（原子，避免两步更新间的崩溃窗口） */
    @Update("UPDATE maritime_groupon_record SET groupon_status = 'SUCCESS', success_time = #{successTime} WHERE id = #{id} AND groupon_status = 'IN_PROGRESS' AND deleted = 0")
    int updateStatusAndSuccessTime(@Param("id") Long id, @Param("successTime") LocalDateTime successTime);

    /** 统计指定时间段内成团数（Dashboard 本月成团用） */
    @Select("SELECT COUNT(*) FROM maritime_groupon_record " +
            "WHERE groupon_status = 'SUCCESS' AND success_time >= #{start} AND success_time <= #{end} AND deleted = 0")
    int countSucceededByTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}