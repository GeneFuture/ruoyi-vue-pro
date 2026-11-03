package cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder;

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

import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;
import cn.iocoder.yudao.module.maritime.service.enrollment.EnrollmentService;
import cn.iocoder.yudao.module.maritime.service.enrollmentOrder.AdminEnrollmentOrderQueryService;
import cn.iocoder.yudao.module.maritime.service.enrollmentOrder.EnrollmentOrderService;

@Tag(name = "管理后台 - 支付订单")
@RestController
@RequestMapping("/maritime/enrollment-order")
@Validated
public class AdminEnrollmentOrderController {

    @Resource
    private EnrollmentOrderService enrollmentOrderService;

    @Resource
    private EnrollmentService enrollmentService;

    @Resource
    private AdminEnrollmentOrderQueryService adminEnrollmentOrderQueryService;

    @PostMapping("/create")
    @Operation(summary = "创建支付订单")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:create')")
    public CommonResult<Long> createEnrollmentOrder(@Valid @RequestBody EnrollmentOrderSaveReqVO createReqVO) {
        return success(enrollmentOrderService.createEnrollmentOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新支付订单")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:update')")
    public CommonResult<Boolean> updateEnrollmentOrder(@Valid @RequestBody EnrollmentOrderSaveReqVO updateReqVO) {
        enrollmentOrderService.updateEnrollmentOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除支付订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:delete')")
    public CommonResult<Boolean> deleteEnrollmentOrder(@RequestParam("id") Long id) {
        enrollmentOrderService.deleteEnrollmentOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除支付订单")
                @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:delete')")
    public CommonResult<Boolean> deleteEnrollmentOrderList(@RequestParam("ids") List<Long> ids) {
        enrollmentOrderService.deleteEnrollmentOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得支付订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:query')")
    public CommonResult<EnrollmentOrderRespVO> getEnrollmentOrder(@RequestParam("id") Long id) {
        return success(adminEnrollmentOrderQueryService.getEnrollmentOrder(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付订单分页")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:query')")
    public CommonResult<PageResult<EnrollmentOrderRespVO>> getEnrollmentOrderPage(@Valid EnrollmentOrderPageReqVO pageReqVO) {
        return success(adminEnrollmentOrderQueryService.getEnrollmentOrderPage(pageReqVO));
    }

    @PutMapping("/manual-confirm")
    @Operation(summary = "手动确认定金支付（用于回调异常但实际已付款的情况）")
    @Parameter(name = "orderId", description = "定金订单ID", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:update')")
    public CommonResult<Boolean> manualConfirmDeposit(@RequestParam("orderId") Long orderId) {
        enrollmentService.manualConfirmDeposit(orderId);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出支付订单 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEnrollmentOrderExcel(@Valid EnrollmentOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EnrollmentOrderRespVO> list = adminEnrollmentOrderQueryService
                .getEnrollmentOrderPage(pageReqVO).getList();
        ExcelUtils.write(response, "支付订单.xls", "数据", EnrollmentOrderRespVO.class, list);
    }

}