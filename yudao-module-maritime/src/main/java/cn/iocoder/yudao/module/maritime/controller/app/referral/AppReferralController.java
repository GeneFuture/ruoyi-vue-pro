package cn.iocoder.yudao.module.maritime.controller.app.referral;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.referral.vo.*;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;
import cn.iocoder.yudao.module.maritime.service.referralRelation.ReferralRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 推荐中心")
@RestController
@RequestMapping("/maritime/referral")
@Validated
public class AppReferralController {

    @Resource
    private ReferralRelationService referralRelationService;

    @Resource
    private CommissionRecordService commissionRecordService;

    @GetMapping("/my-info")
    @Operation(summary = "获得我的推荐信息（推荐码、佣金汇总）")
    public CommonResult<AppReferralInfoRespVO> getMyReferralInfo() {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralRelationService.getMyReferralInfo(memberId));
    }

    @GetMapping("/share-params")
    @Operation(summary = "获得分享参数")
    @Parameter(name = "sessionId", description = "班期ID（可选，分享特定班期时传入）")
    public CommonResult<AppShareParamsRespVO> getShareParams(
            @RequestParam(required = false) Long sessionId) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralRelationService.getShareParams(memberId, sessionId));
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得我的推荐记录（分页）")
    public CommonResult<PageResult<AppReferralRecordRespVO>> getMyReferralList(@Valid PageParam pageParam) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralRelationService.getMyReferralList(memberId, pageParam));
    }

    @GetMapping("/my-commission-records")
    @Operation(summary = "获得我的佣金记录（分页）")
    public CommonResult<PageResult<AppCommissionRecordRespVO>> getMyCommissionRecords(@Valid PageParam pageParam) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(commissionRecordService.getMyCommissionRecords(memberId, pageParam));
    }

    @GetMapping("/my-account")
    @Operation(summary = "获得我的佣金账户汇总")
    public CommonResult<AppCommissionAccountRespVO> getMyCommissionAccount() {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(referralRelationService.getMyCommissionAccount(memberId));
    }

}
