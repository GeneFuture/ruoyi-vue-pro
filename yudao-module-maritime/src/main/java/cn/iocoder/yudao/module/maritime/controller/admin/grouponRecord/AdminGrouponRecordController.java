package cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord;

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

import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.service.grouponRecord.GrouponRecordService;

@Tag(name = "管理后台 - 拼团记录")
@RestController
@RequestMapping("/maritime/groupon-record")
@Validated
public class AdminGrouponRecordController {

    @Resource
    private GrouponRecordService grouponRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建拼团记录")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:create')")
    public CommonResult<Long> createGrouponRecord(@Valid @RequestBody GrouponRecordSaveReqVO createReqVO) {
        return success(grouponRecordService.createGrouponRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新拼团记录")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:update')")
    public CommonResult<Boolean> updateGrouponRecord(@Valid @RequestBody GrouponRecordSaveReqVO updateReqVO) {
        grouponRecordService.updateGrouponRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拼团记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:delete')")
    public CommonResult<Boolean> deleteGrouponRecord(@RequestParam("id") Long id) {
        grouponRecordService.deleteGrouponRecord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除拼团记录")
                @PreAuthorize("@ss.hasPermission('maritime:groupon-record:delete')")
    public CommonResult<Boolean> deleteGrouponRecordList(@RequestParam("ids") List<Long> ids) {
        grouponRecordService.deleteGrouponRecordListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得拼团记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:query')")
    public CommonResult<GrouponRecordRespVO> getGrouponRecord(@RequestParam("id") Long id) {
        GrouponRecordDO grouponRecord = grouponRecordService.getGrouponRecord(id);
        return success(BeanUtils.toBean(grouponRecord, GrouponRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团记录分页")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:query')")
    public CommonResult<PageResult<GrouponRecordRespVO>> getGrouponRecordPage(@Valid GrouponRecordPageReqVO pageReqVO) {
        PageResult<GrouponRecordDO> pageResult = grouponRecordService.getGrouponRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, GrouponRecordRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出拼团记录 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGrouponRecordExcel(@Valid GrouponRecordPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GrouponRecordDO> list = grouponRecordService.getGrouponRecordPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "拼团记录.xls", "数据", GrouponRecordRespVO.class,
                        BeanUtils.toBean(list, GrouponRecordRespVO.class));
    }

}