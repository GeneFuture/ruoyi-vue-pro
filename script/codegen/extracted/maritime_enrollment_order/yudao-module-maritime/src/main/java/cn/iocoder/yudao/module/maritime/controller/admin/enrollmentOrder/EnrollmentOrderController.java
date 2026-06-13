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
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.service.enrollmentOrder.EnrollmentOrderService;

@Tag(name = "管理后台 - 报名订单")
@RestController
@RequestMapping("/maritime/enrollment-order")
@Validated
public class EnrollmentOrderController {

    @Resource
    private EnrollmentOrderService enrollmentOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建报名订单")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:create')")
    public CommonResult<Long> createEnrollmentOrder(@Valid @RequestBody EnrollmentOrderSaveReqVO createReqVO) {
        return success(enrollmentOrderService.createEnrollmentOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报名订单")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:update')")
    public CommonResult<Boolean> updateEnrollmentOrder(@Valid @RequestBody EnrollmentOrderSaveReqVO updateReqVO) {
        enrollmentOrderService.updateEnrollmentOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报名订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:delete')")
    public CommonResult<Boolean> deleteEnrollmentOrder(@RequestParam("id") Long id) {
        enrollmentOrderService.deleteEnrollmentOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除报名订单")
                @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:delete')")
    public CommonResult<Boolean> deleteEnrollmentOrderList(@RequestParam("ids") List<Long> ids) {
        enrollmentOrderService.deleteEnrollmentOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报名订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:query')")
    public CommonResult<EnrollmentOrderRespVO> getEnrollmentOrder(@RequestParam("id") Long id) {
        EnrollmentOrderDO enrollmentOrder = enrollmentOrderService.getEnrollmentOrder(id);
        return success(BeanUtils.toBean(enrollmentOrder, EnrollmentOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报名订单分页")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:query')")
    public CommonResult<PageResult<EnrollmentOrderRespVO>> getEnrollmentOrderPage(@Valid EnrollmentOrderPageReqVO pageReqVO) {
        PageResult<EnrollmentOrderDO> pageResult = enrollmentOrderService.getEnrollmentOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, EnrollmentOrderRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出报名订单 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEnrollmentOrderExcel(@Valid EnrollmentOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EnrollmentOrderDO> list = enrollmentOrderService.getEnrollmentOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "报名订单.xls", "数据", EnrollmentOrderRespVO.class,
                        BeanUtils.toBean(list, EnrollmentOrderRespVO.class));
    }

}