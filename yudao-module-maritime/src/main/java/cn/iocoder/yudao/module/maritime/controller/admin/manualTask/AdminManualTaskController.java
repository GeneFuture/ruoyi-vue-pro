package cn.iocoder.yudao.module.maritime.controller.admin.manualTask;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundResolveReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskResolveReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRespVO;
import cn.iocoder.yudao.module.maritime.service.manualTask.AdminManualTaskQueryService;
import cn.iocoder.yudao.module.maritime.service.manualTask.ManualTaskRefundService;
import cn.iocoder.yudao.module.maritime.service.manualTask.ManualTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 人工处理单")
@RestController
@RequestMapping("/maritime/manual-task")
@Validated
public class AdminManualTaskController {

    @Resource
    private ManualTaskService manualTaskService;

    @Resource
    private AdminManualTaskQueryService adminManualTaskQueryService;

    @Resource
    private ManualTaskRefundService manualTaskRefundService;

    @GetMapping("/page")
    @Operation(summary = "获得人工处理单分页")
    @PreAuthorize("@ss.hasPermission('maritime:manual-task:query')")
    public CommonResult<PageResult<ManualTaskRespVO>> getManualTaskPage(@Valid ManualTaskPageReqVO pageReqVO) {
        return success(adminManualTaskQueryService.getManualTaskPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得人工处理单")
    @Parameter(name = "id", description = "人工处理单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('maritime:manual-task:query')")
    public CommonResult<ManualTaskRespVO> getManualTask(@RequestParam("id") Long id) {
        return success(adminManualTaskQueryService.getManualTask(id));
    }

    @PutMapping("/resolve")
    @Operation(summary = "处理人工单")
    @PreAuthorize("@ss.hasPermission('maritime:manual-task:update')")
    public CommonResult<Boolean> resolveManualTask(@Valid @RequestBody ManualTaskResolveReqVO reqVO) {
        manualTaskService.resolveTask(reqVO, SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }

    @GetMapping("/refund-detail")
    @Operation(summary = "获得退费类人工单的审核详情")
    @Parameter(name = "taskId", description = "人工处理单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('maritime:manual-task:query')")
    public CommonResult<ManualTaskRefundDetailRespVO> getRefundDetail(@RequestParam("taskId") Long taskId) {
        return success(manualTaskRefundService.getRefundDetail(taskId));
    }

    @PutMapping("/resolve-refund")
    @Operation(summary = "审核退费类人工单")
    @PreAuthorize("@ss.hasPermission('maritime:manual-task:update')")
    public CommonResult<Boolean> resolveRefundTask(@Valid @RequestBody ManualTaskRefundResolveReqVO reqVO) {
        manualTaskRefundService.resolveRefundTask(reqVO, SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }

}
