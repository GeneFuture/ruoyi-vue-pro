package cn.iocoder.yudao.module.maritime.controller.app.session;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionDetailRespVO;
import cn.iocoder.yudao.module.maritime.service.session.CourseSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 班期管理")
@RestController
@RequestMapping("/maritime/session")
@Validated
public class AppCourseSessionController {

    @Resource
    private CourseSessionService courseSessionService;

    @GetMapping("/get")
    @Operation(summary = "获取班期详情（含费用配置和进行中拼团）")
    @Parameter(name = "id", description = "班期ID", required = true)
    @PermitAll
    public CommonResult<AppSessionDetailRespVO> getSessionDetail(@RequestParam("id") Long id) {
        return success(courseSessionService.getCourseSessionForApp(id));
    }

}
