package cn.iocoder.yudao.module.maritime.controller.admin.blacklist;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.blacklist.RiskBlacklistDO;
import cn.iocoder.yudao.module.maritime.service.blacklist.RiskBlacklistService;

@Tag(name = "管理后台 - 风控黑名单")
@RestController
@RequestMapping("/maritime/risk-blacklist")
@Validated
public class AdminRiskBlacklistController {

    @Resource
    private RiskBlacklistService riskBlacklistService;

    @PostMapping("/create")
    @Operation(summary = "创建风控黑名单")
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:create')")
    public CommonResult<Long> createRiskBlacklist(@Valid @RequestBody RiskBlacklistSaveReqVO createReqVO) {
        return success(riskBlacklistService.createRiskBlacklist(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新风控黑名单")
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:update')")
    public CommonResult<Boolean> updateRiskBlacklist(@Valid @RequestBody RiskBlacklistSaveReqVO updateReqVO) {
        riskBlacklistService.updateRiskBlacklist(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除风控黑名单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:delete')")
    public CommonResult<Boolean> deleteRiskBlacklist(@RequestParam("id") Long id) {
        riskBlacklistService.deleteRiskBlacklist(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除风控黑名单")
                @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:delete')")
    public CommonResult<Boolean> deleteRiskBlacklistList(@RequestParam("ids") List<Long> ids) {
        riskBlacklistService.deleteRiskBlacklistListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得风控黑名单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:query')")
    public CommonResult<RiskBlacklistRespVO> getRiskBlacklist(@RequestParam("id") Long id) {
        RiskBlacklistDO riskBlacklist = riskBlacklistService.getRiskBlacklist(id);
        return success(BeanUtils.toBean(riskBlacklist, RiskBlacklistRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得风控黑名单分页")
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:query')")
    public CommonResult<PageResult<RiskBlacklistRespVO>> getRiskBlacklistPage(@Valid RiskBlacklistPageReqVO pageReqVO) {
        PageResult<RiskBlacklistDO> pageResult = riskBlacklistService.getRiskBlacklistPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RiskBlacklistRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出风控黑名单 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:risk-blacklist:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRiskBlacklistExcel(@Valid RiskBlacklistPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RiskBlacklistDO> list = riskBlacklistService.getRiskBlacklistPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "风控黑名单.xls", "数据", RiskBlacklistRespVO.class,
                        BeanUtils.toBean(list, RiskBlacklistRespVO.class));
    }

}