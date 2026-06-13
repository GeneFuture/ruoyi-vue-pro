package cn.iocoder.yudao.module.maritime.controller.admin.referralRelation;

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

import cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation.ReferralRelationDO;
import cn.iocoder.yudao.module.maritime.service.referralRelation.ReferralRelationService;

@Tag(name = "管理后台 - 推荐关系")
@RestController
@RequestMapping("/maritime/referral-relation")
@Validated
public class ReferralRelationController {

    @Resource
    private ReferralRelationService referralRelationService;

    @PostMapping("/create")
    @Operation(summary = "创建推荐关系")
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:create')")
    public CommonResult<Long> createReferralRelation(@Valid @RequestBody ReferralRelationSaveReqVO createReqVO) {
        return success(referralRelationService.createReferralRelation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新推荐关系")
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:update')")
    public CommonResult<Boolean> updateReferralRelation(@Valid @RequestBody ReferralRelationSaveReqVO updateReqVO) {
        referralRelationService.updateReferralRelation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除推荐关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:delete')")
    public CommonResult<Boolean> deleteReferralRelation(@RequestParam("id") Long id) {
        referralRelationService.deleteReferralRelation(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除推荐关系")
                @PreAuthorize("@ss.hasPermission('maritime:referral-relation:delete')")
    public CommonResult<Boolean> deleteReferralRelationList(@RequestParam("ids") List<Long> ids) {
        referralRelationService.deleteReferralRelationListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得推荐关系")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:query')")
    public CommonResult<ReferralRelationRespVO> getReferralRelation(@RequestParam("id") Long id) {
        ReferralRelationDO referralRelation = referralRelationService.getReferralRelation(id);
        return success(BeanUtils.toBean(referralRelation, ReferralRelationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得推荐关系分页")
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:query')")
    public CommonResult<PageResult<ReferralRelationRespVO>> getReferralRelationPage(@Valid ReferralRelationPageReqVO pageReqVO) {
        PageResult<ReferralRelationDO> pageResult = referralRelationService.getReferralRelationPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ReferralRelationRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出推荐关系 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:referral-relation:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportReferralRelationExcel(@Valid ReferralRelationPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ReferralRelationDO> list = referralRelationService.getReferralRelationPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "推荐关系.xls", "数据", ReferralRelationRespVO.class,
                        BeanUtils.toBean(list, ReferralRelationRespVO.class));
    }

}