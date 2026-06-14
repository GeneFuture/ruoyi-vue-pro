package cn.iocoder.yudao.module.maritime.controller.app.groupon;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.groupon.vo.AppGrouponStatusRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 拼团")
@RestController
@RequestMapping("/maritime/groupon")
@Validated
public class AppGrouponController {

    @Resource
    private GrouponService grouponService;

    @GetMapping("/active/{sessionId}")
    @Operation(summary = "获取班期进行中的拼团列表")
    @Parameter(name = "sessionId", description = "班期ID", required = true)
    @PermitAll
    public CommonResult<List<AppActiveGrouponVO>> getActiveGroupons(@PathVariable Long sessionId) {
        return success(grouponService.getActiveGroupons(sessionId));
    }

    @GetMapping("/my-status/{enrollmentId}")
    @Operation(summary = "查询我的拼团状态")
    @Parameter(name = "enrollmentId", description = "报名ID", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment:query')")
    public CommonResult<AppGrouponStatusRespVO> getMyGrouponStatus(@PathVariable Long enrollmentId) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(grouponService.getMyGrouponStatus(enrollmentId, memberId));
    }

}
