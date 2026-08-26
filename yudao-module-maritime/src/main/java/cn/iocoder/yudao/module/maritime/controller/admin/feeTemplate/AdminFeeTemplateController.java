package cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import java.util.List;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import cn.iocoder.yudao.module.maritime.service.feeTemplate.FeeTemplateService;

@Tag(name = "管理后台 - 费用模板")
@RestController
@RequestMapping("/maritime/fee-template")
@Validated
public class AdminFeeTemplateController {

    @Resource
    private FeeTemplateService feeTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建费用模板")
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:create')")
    public CommonResult<Long> createFeeTemplate(@Valid @RequestBody FeeTemplateSaveReqVO createReqVO) {
        return success(feeTemplateService.createFeeTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新费用模板")
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:update')")
    public CommonResult<Boolean> updateFeeTemplate(@Valid @RequestBody FeeTemplateSaveReqVO updateReqVO) {
        feeTemplateService.updateFeeTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除费用模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:delete')")
    public CommonResult<Boolean> deleteFeeTemplate(@RequestParam("id") Long id) {
        feeTemplateService.deleteFeeTemplate(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除费用模板")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:delete')")
    public CommonResult<Boolean> deleteFeeTemplateList(@RequestParam("ids") List<Long> ids) {
        feeTemplateService.deleteFeeTemplateListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得费用模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:query')")
    public CommonResult<FeeTemplateRespVO> getFeeTemplate(@RequestParam("id") Long id) {
        FeeTemplateDO feeTemplate = feeTemplateService.getFeeTemplate(id);
        return success(BeanUtils.toBean(feeTemplate, FeeTemplateRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得费用模板分页")
    @PreAuthorize("@ss.hasPermission('maritime:fee-template:query')")
    public CommonResult<PageResult<FeeTemplateRespVO>> getFeeTemplatePage(@Valid FeeTemplatePageReqVO pageReqVO) {
        PageResult<FeeTemplateDO> pageResult = feeTemplateService.getFeeTemplatePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, FeeTemplateRespVO.class));
    }

}
