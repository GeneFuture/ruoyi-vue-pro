package cn.iocoder.yudao.module.maritime.dal.mysql.manualTask;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskStatusEnum;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人工处理单 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface ManualTaskMapper extends BaseMapperX<ManualTaskDO> {

    default PageResult<ManualTaskDO> selectPage(ManualTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ManualTaskDO>()
                .eqIfPresent(ManualTaskDO::getEnrollmentId, reqVO.getEnrollmentId())
                .inIfPresent(ManualTaskDO::getEnrollmentId, reqVO.getEnrollmentIds())
                .eqIfPresent(ManualTaskDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(ManualTaskDO::getTaskType, reqVO.getTaskType())
                .eqIfPresent(ManualTaskDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ManualTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ManualTaskDO::getPriority)
                .orderByDesc(ManualTaskDO::getId));
    }

    /**
     * 查询报名下未关闭的人工单（一条报名可以有多个未关闭任务）
     */
    default List<ManualTaskDO> selectOpenByEnrollmentId(Long enrollmentId) {
        return selectList(new LambdaQueryWrapperX<ManualTaskDO>()
                .eq(ManualTaskDO::getEnrollmentId, enrollmentId)
                .in(ManualTaskDO::getStatus,
                        ManualTaskStatusEnum.PENDING.getStatus(),
                        ManualTaskStatusEnum.PROCESSING.getStatus())
                .orderByDesc(ManualTaskDO::getPriority)
                .orderByDesc(ManualTaskDO::getId));
    }

    default ManualTaskDO selectOpenByEnrollmentIdAndType(Long enrollmentId, String taskType) {
        return selectOne(new LambdaQueryWrapperX<ManualTaskDO>()
                .eq(ManualTaskDO::getEnrollmentId, enrollmentId)
                .eq(ManualTaskDO::getTaskType, taskType)
                .in(ManualTaskDO::getStatus,
                        ManualTaskStatusEnum.PENDING.getStatus(),
                        ManualTaskStatusEnum.PROCESSING.getStatus())
                .orderByDesc(ManualTaskDO::getId)
                .last("LIMIT 1"));
    }

    /**
     * 关闭报名下某一类型的未关闭任务（支付补缴、退费审核等场景使用）
     */
    default int closeOpenByEnrollmentIdAndType(Long enrollmentId, String taskType,
                                               String handleResult, Long handlerId) {
        return update(null, new LambdaUpdateWrapper<ManualTaskDO>()
                .set(ManualTaskDO::getStatus, ManualTaskStatusEnum.DONE.getStatus())
                .set(ManualTaskDO::getHandleResult, handleResult)
                .set(ManualTaskDO::getHandlerId, handlerId)
                .set(ManualTaskDO::getHandleTime, LocalDateTime.now())
                .eq(ManualTaskDO::getEnrollmentId, enrollmentId)
                .eq(ManualTaskDO::getTaskType, taskType)
                .in(ManualTaskDO::getStatus,
                        ManualTaskStatusEnum.PENDING.getStatus(),
                        ManualTaskStatusEnum.PROCESSING.getStatus()));
    }

    /**
     * 关闭报名下所有未关闭的人工单（退费/取消等终态场景使用）
     */
    default int closeAllOpenByEnrollmentId(Long enrollmentId, String handleResult, Long handlerId) {
        return update(null, new LambdaUpdateWrapper<ManualTaskDO>()
                .set(ManualTaskDO::getStatus, ManualTaskStatusEnum.DONE.getStatus())
                .set(ManualTaskDO::getHandleResult, handleResult)
                .set(ManualTaskDO::getHandlerId, handlerId)
                .set(ManualTaskDO::getHandleTime, LocalDateTime.now())
                .eq(ManualTaskDO::getEnrollmentId, enrollmentId)
                .in(ManualTaskDO::getStatus,
                        ManualTaskStatusEnum.PENDING.getStatus(),
                        ManualTaskStatusEnum.PROCESSING.getStatus()));
    }

}
