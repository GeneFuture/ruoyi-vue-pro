package cn.iocoder.yudao.module.maritime.controller.admin.enrollment;

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

import io.swagger.v3.oas.annotations.media.Schema;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.service.enrollment.EnrollmentService;

@Tag(name = "管理后台 - 报名管理")
@RestController
@RequestMapping("/maritime/enrollment")
@Validated
public class AdminEnrollmentController {

    @Resource
    private EnrollmentService enrollmentService;

    @PostMapping("/create")
    @Operation(summary = "创建报名管理")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:create')")
    public CommonResult<Long> createEnrollment(@Valid @RequestBody EnrollmentSaveReqVO createReqVO) {
        return success(enrollmentService.createEnrollment(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新报名管理")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:update')")
    public CommonResult<Boolean> updateEnrollment(@Valid @RequestBody EnrollmentSaveReqVO updateReqVO) {
        enrollmentService.updateEnrollment(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除报名管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:delete')")
    public CommonResult<Boolean> deleteEnrollment(@RequestParam("id") Long id) {
        enrollmentService.deleteEnrollment(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除报名管理")
                @PreAuthorize("@ss.hasPermission('maritime:enrollment:delete')")
    public CommonResult<Boolean> deleteEnrollmentList(@RequestParam("ids") List<Long> ids) {
        enrollmentService.deleteEnrollmentListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得报名管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:query')")
    public CommonResult<EnrollmentRespVO> getEnrollment(@RequestParam("id") Long id) {
        EnrollmentDO enrollment = enrollmentService.getEnrollment(id);
        return success(toMaskedRespVO(enrollment));
    }

    @GetMapping("/page")
    @Operation(summary = "获得报名管理分页")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:query')")
    public CommonResult<PageResult<EnrollmentRespVO>> getEnrollmentPage(@Valid EnrollmentPageReqVO pageReqVO) {
        PageResult<EnrollmentDO> pageResult = enrollmentService.getEnrollmentPage(pageReqVO);
        List<EnrollmentRespVO> voList = pageResult.getList().stream()
                .map(this::toMaskedRespVO).collect(java.util.stream.Collectors.toList());
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出报名管理 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEnrollmentExcel(@Valid EnrollmentPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EnrollmentDO> list = enrollmentService.getEnrollmentPage(pageReqVO).getList();
        List<EnrollmentRespVO> voList = list.stream().map(this::toMaskedRespVO).collect(java.util.stream.Collectors.toList());
        ExcelUtils.write(response, "报名管理.xls", "数据", EnrollmentRespVO.class, voList);
    }

    /** 将 DO 转 VO 并对身份证做脱敏（前4后4，中间*） */
    private EnrollmentRespVO toMaskedRespVO(EnrollmentDO enrollment) {
        EnrollmentRespVO vo = BeanUtils.toBean(enrollment, EnrollmentRespVO.class);
        String idCard = enrollment.getIdCard();
        if (StrUtil.isNotBlank(idCard) && idCard.length() >= 8) {
            vo.setIdCardMasked(idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4));
        }
        return vo;
    }

    @PutMapping("/confirm")
    @Operation(summary = "人工确认报名（处理异常情况）")
    @Parameter(name = "id", description = "报名ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:update')")
    public CommonResult<Boolean> confirmEnrollment(@RequestParam("id") Long id) {
        enrollmentService.confirmEnrollmentByAdmin(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "管理员取消报名")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:update')")
    public CommonResult<Boolean> cancelEnrollment(
            @RequestParam("id") Long id,
            @RequestParam("cancelReason") @NotBlank(message = "取消原因不能为空") String cancelReason) {
        enrollmentService.cancelEnrollmentByAdmin(id, cancelReason);
        return success(true);
    }

}