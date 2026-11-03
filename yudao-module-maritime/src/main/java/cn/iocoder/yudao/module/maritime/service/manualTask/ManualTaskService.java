package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskResolveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;

import java.util.List;

/**
 * 人工处理单 Service 接口
 *
 * @author Gene Ye
 */
public interface ManualTaskService {

    /**
     * 创建人工处理单；同一报名同一类型已有未关闭工单时直接复用
     *
     * @return 人工处理单ID
     */
    Long createTaskIfAbsent(Long enrollmentId, Long memberId, Long orderId,
                            String sourceType, Long sourceId, String taskType, String reason);

    /**
     * 查询人工处理单
     */
    ManualTaskDO getTask(Long id);

    /**
     * 查询报名下优先级最高的未关闭人工单
     */
    ManualTaskDO getOpenTask(Long enrollmentId);

    /**
     * 查询报名下所有未关闭人工单
     */
    List<ManualTaskDO> getOpenTasks(Long enrollmentId);

    /**
     * 分页查询人工处理单
     */
    PageResult<ManualTaskDO> getPage(ManualTaskPageReqVO pageReqVO);

    /**
     * 处理人工单：关闭工单，并按目标状态推进报名
     *
     * @param reqVO     处理参数
     * @param handlerId 处理人
     */
    void resolveTask(ManualTaskResolveReqVO reqVO, Long handlerId);

    /**
     * 关闭报名下指定类型的未关闭人工单（支付补缴、退费审核等场景使用）
     */
    void closeOpenTask(Long enrollmentId, String taskType, String handleResult, Long handlerId);

    /**
     * 关闭报名下所有未关闭的人工单（退费/取消等终态场景使用）
     */
    void closeAllOpenTasks(Long enrollmentId, String handleResult, Long handlerId);

}
