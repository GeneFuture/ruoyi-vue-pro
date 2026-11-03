package cn.iocoder.yudao.module.maritime.controller.app.home;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.maritime.controller.app.home.vo.AppHomeRespVO;
import cn.iocoder.yudao.module.maritime.service.home.AppHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 首页")
@RestController
@RequestMapping("/maritime/home")
@Validated
public class AppHomeController {

    @Resource
    private AppHomeService appHomeService;

    @GetMapping("/get")
    @Operation(summary = "获取首页聚合数据（金刚区+Banner+平台统计+公告+近期开班）")
    @PermitAll
    public CommonResult<AppHomeRespVO> getHome() {
        return success(appHomeService.getHome());
    }

}
