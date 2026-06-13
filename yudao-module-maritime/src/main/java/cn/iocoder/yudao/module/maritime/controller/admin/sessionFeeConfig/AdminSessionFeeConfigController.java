package cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig;

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

import cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.service.sessionFeeConfig.SessionFeeConfigService;

@Tag(name = "管理后台 - 班期费用配置")
@RestController
@RequestMapping("/maritime/session-fee-config")
@Validated
public class AdminSessionFeeConfigController {

    @Resource
    private SessionFeeConfigService sessionFeeConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建班期费用配置")
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:create')")
    public CommonResult<Long> createSessionFeeConfig(@Valid @RequestBody SessionFeeConfigSaveReqVO createReqVO) {
        return success(sessionFeeConfigService.createSessionFeeConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班期费用配置")
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:update')")
    public CommonResult<Boolean> updateSessionFeeConfig(@Valid @RequestBody SessionFeeConfigSaveReqVO updateReqVO) {
        sessionFeeConfigService.updateSessionFeeConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班期费用配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:delete')")
    public CommonResult<Boolean> deleteSessionFeeConfig(@RequestParam("id") Long id) {
        sessionFeeConfigService.deleteSessionFeeConfig(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除班期费用配置")
                @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:delete')")
    public CommonResult<Boolean> deleteSessionFeeConfigList(@RequestParam("ids") List<Long> ids) {
        sessionFeeConfigService.deleteSessionFeeConfigListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得班期费用配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:query')")
    public CommonResult<SessionFeeConfigRespVO> getSessionFeeConfig(@RequestParam("id") Long id) {
        SessionFeeConfigDO sessionFeeConfig = sessionFeeConfigService.getSessionFeeConfig(id);
        return success(BeanUtils.toBean(sessionFeeConfig, SessionFeeConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得班期费用配置分页")
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:query')")
    public CommonResult<PageResult<SessionFeeConfigRespVO>> getSessionFeeConfigPage(@Valid SessionFeeConfigPageReqVO pageReqVO) {
        PageResult<SessionFeeConfigDO> pageResult = sessionFeeConfigService.getSessionFeeConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SessionFeeConfigRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班期费用配置 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:session-fee-config:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSessionFeeConfigExcel(@Valid SessionFeeConfigPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SessionFeeConfigDO> list = sessionFeeConfigService.getSessionFeeConfigPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "班期费用配置.xls", "数据", SessionFeeConfigRespVO.class,
                        BeanUtils.toBean(list, SessionFeeConfigRespVO.class));
    }

}