package cn.iocoder.yudao.module.maritime.controller.admin.course;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.course.vo.CourseSaveReqVO;
import cn.iocoder.yudao.module.maritime.convert.course.CourseConvert;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.service.course.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 课程管理")
@RestController
@RequestMapping("/maritime/course")
@Validated
public class AdminCourseController {

    @Resource
    private CourseService courseService;

    @PostMapping("/create")
    @Operation(summary = "创建课程")
    @PreAuthorize("@ss.hasPermission('maritime:course:create')")
    public CommonResult<Long> createCourse(@Valid @RequestBody CourseSaveReqVO saveReqVO) {
        return success(courseService.createCourse(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新课程")
    @PreAuthorize("@ss.hasPermission('maritime:course:update')")
    public CommonResult<Boolean> updateCourse(@Valid @RequestBody CourseSaveReqVO saveReqVO) {
        courseService.updateCourse(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除课程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course:delete')")
    public CommonResult<Boolean> deleteCourse(@RequestParam("id") Long id) {
        courseService.deleteCourse(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得课程")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course:query')")
    public CommonResult<CourseRespVO> getCourse(@RequestParam("id") Long id) {
        CourseDO course = courseService.getCourse(id);
        return success(CourseConvert.INSTANCE.convert(course));
    }

    @GetMapping("/page")
    @Operation(summary = "获得课程分页")
    @PreAuthorize("@ss.hasPermission('maritime:course:query')")
    public CommonResult<PageResult<CourseRespVO>> getCoursePage(@Valid CoursePageReqVO pageReqVO) {
        PageResult<CourseDO> pageResult = courseService.getCoursePage(pageReqVO);
        return success(CourseConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改课程上下架状态")
    @Parameter(name = "id", description = "课程ID", required = true)
    @Parameter(name = "status", description = "状态（0=下架，1=上架）", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course:update')")
    public CommonResult<Boolean> updateCourseStatus(@RequestParam("id") Long id,
                                                     @RequestParam("status") Integer status) {
        courseService.updateCourseStatus(id, status);
        return success(true);
    }

}
