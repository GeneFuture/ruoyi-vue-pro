package cn.iocoder.yudao.module.maritime.controller.admin.grouponMember;

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
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.*;
import cn.iocoder.yudao.module.maritime.service.grouponMember.AdminGrouponMemberQueryService;
import cn.iocoder.yudao.module.maritime.service.grouponMember.GrouponMemberService;

@Tag(name = "管理后台 - 拼团成员")
@RestController
@RequestMapping("/maritime/groupon-member")
@Validated
public class AdminGrouponMemberController {

    @Resource
    private GrouponMemberService grouponMemberService;

    @Resource
    private AdminGrouponMemberQueryService adminGrouponMemberQueryService;

    @PostMapping("/create")
    @Operation(summary = "创建拼团成员")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:create')")
    public CommonResult<Long> createGrouponMember(@Valid @RequestBody GrouponMemberSaveReqVO createReqVO) {
        return success(grouponMemberService.createGrouponMember(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新拼团成员")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:update')")
    public CommonResult<Boolean> updateGrouponMember(@Valid @RequestBody GrouponMemberSaveReqVO updateReqVO) {
        grouponMemberService.updateGrouponMember(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拼团成员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:delete')")
    public CommonResult<Boolean> deleteGrouponMember(@RequestParam("id") Long id) {
        grouponMemberService.deleteGrouponMember(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除拼团成员")
                @PreAuthorize("@ss.hasPermission('maritime:groupon-member:delete')")
    public CommonResult<Boolean> deleteGrouponMemberList(@RequestParam("ids") List<Long> ids) {
        grouponMemberService.deleteGrouponMemberListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得拼团成员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:query')")
    public CommonResult<GrouponMemberRespVO> getGrouponMember(@RequestParam("id") Long id) {
        return success(adminGrouponMemberQueryService.getGrouponMember(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团成员分页")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:query')")
    public CommonResult<PageResult<GrouponMemberRespVO>> getGrouponMemberPage(@Valid GrouponMemberPageReqVO pageReqVO) {
        return success(adminGrouponMemberQueryService.getGrouponMemberPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出拼团成员 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:groupon-member:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGrouponMemberExcel(@Valid GrouponMemberPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GrouponMemberRespVO> list = adminGrouponMemberQueryService
                .getGrouponMemberPage(pageReqVO).getList();
        ExcelUtils.write(response, "拼团成员.xls", "数据", GrouponMemberRespVO.class, list);
    }

}