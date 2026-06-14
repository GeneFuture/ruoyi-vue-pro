package cn.iocoder.yudao.module.maritime.controller.admin.refundApply;

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
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService;

@Tag(name = "管理后台 - 退费申请")
@RestController
@RequestMapping("/maritime/refund-apply")
@Validated
public class AdminRefundApplyController {

    @Resource
    private RefundApplyService refundApplyService;

    @PostMapping("/create")
    @Operation(summary = "创建退费申请")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:create')")
    public CommonResult<Long> createRefundApply(@Valid @RequestBody RefundApplySaveReqVO createReqVO) {
        return success(refundApplyService.createRefundApply(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新退费申请")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:update')")
    public CommonResult<Boolean> updateRefundApply(@Valid @RequestBody RefundApplySaveReqVO updateReqVO) {
        refundApplyService.updateRefundApply(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除退费申请")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:delete')")
    public CommonResult<Boolean> deleteRefundApply(@RequestParam("id") Long id) {
        refundApplyService.deleteRefundApply(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除退费申请")
                @PreAuthorize("@ss.hasPermission('maritime:refund-apply:delete')")
    public CommonResult<Boolean> deleteRefundApplyList(@RequestParam("ids") List<Long> ids) {
        refundApplyService.deleteRefundApplyListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得退费申请")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:query')")
    public CommonResult<RefundApplyRespVO> getRefundApply(@RequestParam("id") Long id) {
        RefundApplyDO refundApply = refundApplyService.getRefundApply(id);
        return success(BeanUtils.toBean(refundApply, RefundApplyRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得退费申请分页")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:query')")
    public CommonResult<PageResult<RefundApplyRespVO>> getRefundApplyPage(@Valid RefundApplyPageReqVO pageReqVO) {
        PageResult<RefundApplyDO> pageResult = refundApplyService.getRefundApplyPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RefundApplyRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出退费申请 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportRefundApplyExcel(@Valid RefundApplyPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<RefundApplyDO> list = refundApplyService.getRefundApplyPage(pageReqVO).getList();
        ExcelUtils.write(response, "退费申请.xls", "数据", RefundApplyRespVO.class,
                        BeanUtils.toBean(list, RefundApplyRespVO.class));
    }

    @PutMapping("/approve")
    @Operation(summary = "审核通过退费申请")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:update')")
    public CommonResult<Boolean> approveRefund(@Valid @RequestBody AdminRefundApproveReqVO reqVO) {
        Long approverId = SecurityFrameworkUtils.getLoginUserId();
        refundApplyService.approveRefund(reqVO.getId(), reqVO.getRefundAmount(), approverId, reqVO.getAdminRemark());
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "拒绝退费申请")
    @PreAuthorize("@ss.hasPermission('maritime:refund-apply:update')")
    public CommonResult<Boolean> rejectRefund(@Valid @RequestBody AdminRefundRejectReqVO reqVO) {
        Long approverId = SecurityFrameworkUtils.getLoginUserId();
        refundApplyService.rejectRefund(reqVO.getId(), approverId, reqVO.getRejectReason());
        return success(true);
    }

}