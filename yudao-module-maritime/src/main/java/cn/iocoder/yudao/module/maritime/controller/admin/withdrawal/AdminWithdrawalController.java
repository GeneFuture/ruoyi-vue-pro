package cn.iocoder.yudao.module.maritime.controller.admin.withdrawal;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalRejectReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalRespVO;
import cn.iocoder.yudao.module.maritime.service.withdrawal.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 佣金提现管理")
@RestController
@RequestMapping("/maritime/withdrawal")
@Validated
public class AdminWithdrawalController {

    @Resource
    private WithdrawalService withdrawalService;

    @GetMapping("/page")
    @Operation(summary = "获得提现申请分页")
    @PreAuthorize("@ss.hasPermission('maritime:withdrawal:query')")
    public CommonResult<PageResult<AdminWithdrawalRespVO>> getWithdrawalPage(
            @Valid AdminWithdrawalPageReqVO pageReqVO) {
        return success(withdrawalService.getWithdrawalPage(pageReqVO));
    }

    @PutMapping("/reject")
    @Operation(summary = "拒绝提现申请")
    @PreAuthorize("@ss.hasPermission('maritime:withdrawal:update')")
    public CommonResult<Boolean> rejectWithdrawal(@Valid @RequestBody AdminWithdrawalRejectReqVO reqVO) {
        withdrawalService.rejectWithdrawal(reqVO.getId(), reqVO.getReason());
        return success(true);
    }

}
