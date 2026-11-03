package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRespVO;

/**
 * 管理后台 - 人工处理单查询 Service 接口
 *
 * @author Gene Ye
 */
public interface AdminManualTaskQueryService {

    /**
     * 获得人工处理单详情（含学员、课程、班期、退费信息）
     */
    ManualTaskRespVO getManualTask(Long id);

    /**
     * 获得人工处理单分页（含学员、课程、班期、退费信息）
     */
    PageResult<ManualTaskRespVO> getManualTaskPage(ManualTaskPageReqVO pageReqVO);

}
