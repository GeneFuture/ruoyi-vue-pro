package cn.iocoder.yudao.module.maritime.controller.app.course;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCoursePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.course.vo.AppCourseRespVO;
import cn.iocoder.yudao.module.maritime.service.course.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 课程管理")
@RestController
@RequestMapping("/maritime/course")
@Validated
public class AppCourseController {

    @Resource
    private CourseService courseService;

    @GetMapping("/list")
    @Operation(summary = "获取课程列表")
    @PermitAll
    public CommonResult<PageResult<AppCourseRespVO>> getCourseList(@Valid AppCoursePageReqVO pageReqVO) {
        return success(courseService.getCoursePageForApp(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获取课程详情")
    @Parameter(name = "id", description = "课程ID", required = true)
    @PermitAll
    public CommonResult<AppCourseDetailRespVO> getCourseDetail(@RequestParam("id") Long id) {
        return success(courseService.getCourseDetailForApp(id));
    }

}
