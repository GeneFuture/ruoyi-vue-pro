package cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord;

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

import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;

@Tag(name = "管理后台 - 佣金记录")
@RestController
@RequestMapping("/maritime/commission-record")
@Validated
public class CommissionRecordController {

    @Resource
    private CommissionRecordService commissionRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建佣金记录")
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:create')")
    public CommonResult<Long> createCommissionRecord(@Valid @RequestBody CommissionRecordSaveReqVO createReqVO) {
        return success(commissionRecordService.createCommissionRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新佣金记录")
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:update')")
    public CommonResult<Boolean> updateCommissionRecord(@Valid @RequestBody CommissionRecordSaveReqVO updateReqVO) {
        commissionRecordService.updateCommissionRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除佣金记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:delete')")
    public CommonResult<Boolean> deleteCommissionRecord(@RequestParam("id") Long id) {
        commissionRecordService.deleteCommissionRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除佣金记录")
                @PreAuthorize("@ss.hasPermission('maritime:commission-record:delete')")
    public CommonResult<Boolean> deleteCommissionRecordList(@RequestParam("ids") List<Long> ids) {
        commissionRecordService.deleteCommissionRecordListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:query')")
    public CommonResult<CommissionRecordRespVO> getCommissionRecord(@RequestParam("id") Long id) {
        CommissionRecordDO commissionRecord = commissionRecordService.getCommissionRecord(id);
        return success(BeanUtils.toBean(commissionRecord, CommissionRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金记录分页")
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:query')")
    public CommonResult<PageResult<CommissionRecordRespVO>> getCommissionRecordPage(@Valid CommissionRecordPageReqVO pageReqVO) {
        PageResult<CommissionRecordDO> pageResult = commissionRecordService.getCommissionRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CommissionRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出佣金记录 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:commission-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCommissionRecordExcel(@Valid CommissionRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CommissionRecordDO> list = commissionRecordService.getCommissionRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "佣金记录.xls", "数据", CommissionRecordRespVO.class,
                        BeanUtils.toBean(list, CommissionRecordRespVO.class));
    }

}