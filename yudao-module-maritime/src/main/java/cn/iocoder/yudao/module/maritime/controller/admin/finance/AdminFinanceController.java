package cn.iocoder.yudao.module.maritime.controller.admin.finance;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.maritime.controller.admin.finance.vo.FinanceDailyFlowRespVO;
import cn.iocoder.yudao.module.maritime.service.finance.AdminFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 资金流水")
@RestController
@RequestMapping("/maritime/finance")
@Validated
public class AdminFinanceController {

    @Resource
    private AdminFinanceService adminFinanceService;

    @GetMapping("/daily-flow")
    @Operation(summary = "获得每日资金流与日期范围汇总")
    @Parameters({
            @Parameter(name = "startDate", description = "开始日期（含），为空默认近 30 天", example = "2026-10-01"),
            @Parameter(name = "endDate", description = "结束日期（含），为空默认今天", example = "2026-10-29")
    })
    @PreAuthorize("@ss.hasPermission('maritime:finance:view')")
    public CommonResult<FinanceDailyFlowRespVO> getDailyFlow(
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return success(adminFinanceService.getDailyFlow(startDate, endDate));
    }

}
