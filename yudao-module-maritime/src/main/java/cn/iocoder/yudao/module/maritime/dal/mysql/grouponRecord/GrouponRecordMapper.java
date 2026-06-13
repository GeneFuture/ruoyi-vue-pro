package cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord;

import java.time.LocalDateTime;
import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import org.apache.ibatis.annotations.Mapper;
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

}