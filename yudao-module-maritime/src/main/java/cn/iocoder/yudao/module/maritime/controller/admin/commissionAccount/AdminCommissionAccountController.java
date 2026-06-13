package cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount;

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

import cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionAccount.CommissionAccountDO;
import cn.iocoder.yudao.module.maritime.service.commissionAccount.CommissionAccountService;

@Tag(name = "管理后台 - 佣金账户")
@RestController
@RequestMapping("/maritime/commission-account")
@Validated
public class AdminCommissionAccountController {

    @Resource
    private CommissionAccountService commissionAccountService;

    @PostMapping("/create")
    @Operation(summary = "创建佣金账户")
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:create')")
    public CommonResult<Long> createCommissionAccount(@Valid @RequestBody CommissionAccountSaveReqVO createReqVO) {
        return success(commissionAccountService.createCommissionAccount(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新佣金账户")
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:update')")
    public CommonResult<Boolean> updateCommissionAccount(@Valid @RequestBody CommissionAccountSaveReqVO updateReqVO) {
        commissionAccountService.updateCommissionAccount(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除佣金账户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:delete')")
    public CommonResult<Boolean> deleteCommissionAccount(@RequestParam("id") Long id) {
        commissionAccountService.deleteCommissionAccount(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除佣金账户")
                @PreAuthorize("@ss.hasPermission('maritime:commission-account:delete')")
    public CommonResult<Boolean> deleteCommissionAccountList(@RequestParam("ids") List<Long> ids) {
        commissionAccountService.deleteCommissionAccountListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金账户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:query')")
    public CommonResult<CommissionAccountRespVO> getCommissionAccount(@RequestParam("id") Long id) {
        CommissionAccountDO commissionAccount = commissionAccountService.getCommissionAccount(id);
        return success(BeanUtils.toBean(commissionAccount, CommissionAccountRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金账户分页")
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:query')")
    public CommonResult<PageResult<CommissionAccountRespVO>> getCommissionAccountPage(@Valid CommissionAccountPageReqVO pageReqVO) {
        PageResult<CommissionAccountDO> pageResult = commissionAccountService.getCommissionAccountPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CommissionAccountRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出佣金账户 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:commission-account:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCommissionAccountExcel(@Valid CommissionAccountPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CommissionAccountDO> list = commissionAccountService.getCommissionAccountPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "佣金账户.xls", "数据", CommissionAccountRespVO.class,
                        BeanUtils.toBean(list, CommissionAccountRespVO.class));
    }

}