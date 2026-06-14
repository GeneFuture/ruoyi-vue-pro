package cn.iocoder.yudao.module.maritime.controller.app.enrollment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentListRespVO;
import cn.iocoder.yudao.module.maritime.service.enrollment.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 报名管理")
@RestController
@RequestMapping("/maritime/enrollment")
@Validated
public class AppEnrollmentController {

    @Resource
    private EnrollmentService enrollmentService;

    @PostMapping("/create")
    @Operation(summary = "创建报名记录")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:create')")
    public CommonResult<AppEnrollmentCreateRespVO> createEnrollment(
            @Valid @RequestBody AppEnrollmentCreateReqVO createReqVO) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(enrollmentService.createEnrollmentForApp(createReqVO, memberId));
    }

    @GetMapping("/my-list")
    @Operation(summary = "我的报名列表")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:query')")
    public CommonResult<List<AppEnrollmentListRespVO>> getMyEnrollments() {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(enrollmentService.getMyEnrollments(memberId));
    }

    @GetMapping("/get")
    @Operation(summary = "报名详情")
    @Parameter(name = "id", description = "报名ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:query')")
    public CommonResult<AppEnrollmentDetailRespVO> getEnrollment(@RequestParam("id") Long id) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(enrollmentService.getEnrollmentForApp(id, memberId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消报名（仅限待支付定金状态）")
    @Parameter(name = "id", description = "报名ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:update')")
    public CommonResult<Boolean> cancelEnrollment(@RequestParam("id") Long id) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        enrollmentService.cancelEnrollmentForApp(id, memberId);
        return success(true);
    }

}
