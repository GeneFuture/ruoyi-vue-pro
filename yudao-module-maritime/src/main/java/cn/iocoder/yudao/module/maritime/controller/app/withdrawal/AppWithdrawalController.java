package cn.iocoder.yudao.module.maritime.controller.app.withdrawal;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo.*;
import cn.iocoder.yudao.module.maritime.service.withdrawal.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 佣金提现")
@RestController
@RequestMapping("/maritime/withdrawal")
@Validated
public class AppWithdrawalController {

    @Resource
    private WithdrawalService withdrawalService;

    @PostMapping("/apply")
    @Operation(summary = "申请提现")
    public CommonResult<AppWithdrawalApplyRespVO> applyWithdrawal(@Valid @RequestBody AppWithdrawalApplyReqVO reqVO) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(withdrawalService.applyWithdrawal(memberId, reqVO));
    }

    @GetMapping("/my-list")
    @Operation(summary = "我的提现记录")
    public CommonResult<PageResult<AppWithdrawalRecordRespVO>> getMyWithdrawals(@Valid PageParam pageParam) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(withdrawalService.getMyWithdrawals(memberId, pageParam));
    }

    @GetMapping("/tax-preview")
    @Operation(summary = "提现税额预览")
    @Parameter(name = "amount", description = "提现金额（元）", required = true)
    public CommonResult<AppTaxPreviewRespVO> previewTax(
            @RequestParam("amount") @NotNull @DecimalMin("0.01") BigDecimal amount) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(withdrawalService.previewTax(memberId, amount));
    }

}
