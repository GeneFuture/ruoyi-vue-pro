package cn.iocoder.yudao.module.maritime.controller.admin.userMessage;

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

import cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.userMessage.UserMessageDO;
import cn.iocoder.yudao.module.maritime.service.userMessage.UserMessageService;

@Tag(name = "管理后台 - 站内消息")
@RestController
@RequestMapping("/maritime/user-message")
@Validated
public class AdminUserMessageController {

    @Resource
    private UserMessageService userMessageService;

    @PostMapping("/create")
    @Operation(summary = "创建站内消息")
    @PreAuthorize("@ss.hasPermission('maritime:user-message:create')")
    public CommonResult<Long> createUserMessage(@Valid @RequestBody UserMessageSaveReqVO createReqVO) {
        return success(userMessageService.createUserMessage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新站内消息")
    @PreAuthorize("@ss.hasPermission('maritime:user-message:update')")
    public CommonResult<Boolean> updateUserMessage(@Valid @RequestBody UserMessageSaveReqVO updateReqVO) {
        userMessageService.updateUserMessage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除站内消息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:user-message:delete')")
    public CommonResult<Boolean> deleteUserMessage(@RequestParam("id") Long id) {
        userMessageService.deleteUserMessage(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除站内消息")
                @PreAuthorize("@ss.hasPermission('maritime:user-message:delete')")
    public CommonResult<Boolean> deleteUserMessageList(@RequestParam("ids") List<Long> ids) {
        userMessageService.deleteUserMessageListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得站内消息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:user-message:query')")
    public CommonResult<UserMessageRespVO> getUserMessage(@RequestParam("id") Long id) {
        UserMessageDO userMessage = userMessageService.getUserMessage(id);
        return success(BeanUtils.toBean(userMessage, UserMessageRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得站内消息分页")
    @PreAuthorize("@ss.hasPermission('maritime:user-message:query')")
    public CommonResult<PageResult<UserMessageRespVO>> getUserMessagePage(@Valid UserMessagePageReqVO pageReqVO) {
        PageResult<UserMessageDO> pageResult = userMessageService.getUserMessagePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UserMessageRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出站内消息 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:user-message:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportUserMessageExcel(@Valid UserMessagePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<UserMessageDO> list = userMessageService.getUserMessagePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "站内消息.xls", "数据", UserMessageRespVO.class,
                        BeanUtils.toBean(list, UserMessageRespVO.class));
    }

}