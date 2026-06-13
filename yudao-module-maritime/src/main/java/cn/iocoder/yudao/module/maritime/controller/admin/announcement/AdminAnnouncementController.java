package cn.iocoder.yudao.module.maritime.controller.admin.announcement;

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

import cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.announcement.AnnouncementDO;
import cn.iocoder.yudao.module.maritime.service.announcement.AnnouncementService;

@Tag(name = "管理后台 - 最新动态")
@RestController
@RequestMapping("/maritime/announcement")
@Validated
public class AdminAnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @PostMapping("/create")
    @Operation(summary = "创建最新动态")
    @PreAuthorize("@ss.hasPermission('maritime:announcement:create')")
    public CommonResult<Long> createAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO createReqVO) {
        return success(announcementService.createAnnouncement(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新最新动态")
    @PreAuthorize("@ss.hasPermission('maritime:announcement:update')")
    public CommonResult<Boolean> updateAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO updateReqVO) {
        announcementService.updateAnnouncement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除最新动态")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:announcement:delete')")
    public CommonResult<Boolean> deleteAnnouncement(@RequestParam("id") Long id) {
        announcementService.deleteAnnouncement(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除最新动态")
                @PreAuthorize("@ss.hasPermission('maritime:announcement:delete')")
    public CommonResult<Boolean> deleteAnnouncementList(@RequestParam("ids") List<Long> ids) {
        announcementService.deleteAnnouncementListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得最新动态")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:announcement:query')")
    public CommonResult<AnnouncementRespVO> getAnnouncement(@RequestParam("id") Long id) {
        AnnouncementDO announcement = announcementService.getAnnouncement(id);
        return success(BeanUtils.toBean(announcement, AnnouncementRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得最新动态分页")
    @PreAuthorize("@ss.hasPermission('maritime:announcement:query')")
    public CommonResult<PageResult<AnnouncementRespVO>> getAnnouncementPage(@Valid AnnouncementPageReqVO pageReqVO) {
        PageResult<AnnouncementDO> pageResult = announcementService.getAnnouncementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AnnouncementRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出最新动态 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:announcement:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAnnouncementExcel(@Valid AnnouncementPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AnnouncementDO> list = announcementService.getAnnouncementPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "最新动态.xls", "数据", AnnouncementRespVO.class,
                        BeanUtils.toBean(list, AnnouncementRespVO.class));
    }

}