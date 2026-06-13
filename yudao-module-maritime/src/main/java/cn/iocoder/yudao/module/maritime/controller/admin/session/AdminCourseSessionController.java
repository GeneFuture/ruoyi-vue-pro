package cn.iocoder.yudao.module.maritime.controller.admin.session;

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

import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.service.session.CourseSessionService;

@Tag(name = "管理后台 - 班期管理")
@RestController
@RequestMapping("/maritime/course-session")
@Validated
public class AdminCourseSessionController {

    @Resource
    private CourseSessionService courseSessionService;

    @PostMapping("/create")
    @Operation(summary = "创建班期管理")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:create')")
    public CommonResult<Long> createCourseSession(@Valid @RequestBody CourseSessionSaveReqVO createReqVO) {
        return success(courseSessionService.createCourseSession(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新班期管理")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:update')")
    public CommonResult<Boolean> updateCourseSession(@Valid @RequestBody CourseSessionSaveReqVO updateReqVO) {
        courseSessionService.updateCourseSession(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除班期管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course-session:delete')")
    public CommonResult<Boolean> deleteCourseSession(@RequestParam("id") Long id) {
        courseSessionService.deleteCourseSession(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除班期管理")
                @PreAuthorize("@ss.hasPermission('maritime:course-session:delete')")
    public CommonResult<Boolean> deleteCourseSessionList(@RequestParam("ids") List<Long> ids) {
        courseSessionService.deleteCourseSessionListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得班期管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:query')")
    public CommonResult<CourseSessionRespVO> getCourseSession(@RequestParam("id") Long id) {
        CourseSessionDO courseSession = courseSessionService.getCourseSession(id);
        return success(BeanUtils.toBean(courseSession, CourseSessionRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得班期管理分页")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:query')")
    public CommonResult<PageResult<CourseSessionRespVO>> getCourseSessionPage(@Valid CourseSessionPageReqVO pageReqVO) {
        PageResult<CourseSessionDO> pageResult = courseSessionService.getCourseSessionPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CourseSessionRespVO.class));
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改班期状态")
    @Parameter(name = "id", description = "班期ID", required = true)
    @Parameter(name = "sessionStatus", description = "目标状态（DRAFT/OPEN/PENDING_START/IN_PROGRESS/FINISHED/CANCELLED）", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course-session:update')")
    public CommonResult<Boolean> updateCourseSessionStatus(@RequestParam("id") Long id,
                                                            @RequestParam("sessionStatus") String sessionStatus) {
        courseSessionService.updateCourseSessionStatus(id, sessionStatus);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出班期管理 Excel")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCourseSessionExcel(@Valid CourseSessionPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CourseSessionDO> list = courseSessionService.getCourseSessionPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "班期管理.xls", "数据", CourseSessionRespVO.class,
                        BeanUtils.toBean(list, CourseSessionRespVO.class));
    }

}