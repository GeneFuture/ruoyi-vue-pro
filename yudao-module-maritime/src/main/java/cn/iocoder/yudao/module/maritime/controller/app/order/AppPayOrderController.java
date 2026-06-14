package cn.iocoder.yudao.module.maritime.controller.app.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayBalanceReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayDepositReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderStatusRespVO;
import io.swagger.v3.oas.annotations.Parameter;
import cn.iocoder.yudao.module.maritime.service.enrollment.EnrollmentService;
import cn.iocoder.yudao.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 报名支付")
@RestController
@RequestMapping("/maritime/order")
@Validated
@Slf4j
public class AppPayOrderController {

    @Resource
    private EnrollmentService enrollmentService;

    @PostMapping("/pay-deposit")
    @Operation(summary = "发起定金支付（创建 pay 模块订单，返回 payOrderId 供前端调用微信支付）")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:create')")
    public CommonResult<AppPayOrderRespVO> payDeposit(@Valid @RequestBody AppPayDepositReqVO reqVO) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        AppPayOrderRespVO respVO = enrollmentService.payDeposit(reqVO.getOrderId(), memberId);
        return success(respVO);
    }

    @GetMapping("/status")
    @Operation(summary = "查询定金支付结果（前端轮询，处理微信回调延迟）")
    @Parameter(name = "payOrderId", description = "pay 模块支付单ID", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:query')")
    public CommonResult<AppPayOrderStatusRespVO> getPayOrderStatus(@RequestParam("payOrderId") Long payOrderId) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(enrollmentService.getPayOrderStatus(payOrderId, memberId));
    }

    @PostMapping("/pay-balance")
    @Operation(summary = "发起尾款支付（创建 pay 模块订单，返回 payOrderId 供前端调用微信支付）")
    @PreAuthorize("@ss.hasPermission('maritime:enrollment-order:create')")
    public CommonResult<AppPayOrderRespVO> payBalance(@Valid @RequestBody AppPayBalanceReqVO reqVO) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(enrollmentService.payBalance(reqVO, memberId));
    }

    @PostMapping("/notify")
    @Operation(summary = "定金支付成功回调（由 pay 模块服务端调用，无需登录）")
    @PermitAll
    public CommonResult<Boolean> handleDepositPaidNotify(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        log.info("[handleDepositPaidNotify] merchantOrderId={}, payOrderId={}",
                notifyReqDTO.getMerchantOrderId(), notifyReqDTO.getPayOrderId());
        enrollmentService.handleDepositPaid(notifyReqDTO.getMerchantOrderId(), notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @PostMapping("/notify-balance")
    @Operation(summary = "尾款支付成功回调（由 pay 模块服务端调用，无需登录）")
    @PermitAll
    public CommonResult<Boolean> handleBalancePaidNotify(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        log.info("[handleBalancePaidNotify] merchantOrderId={}, payOrderId={}",
                notifyReqDTO.getMerchantOrderId(), notifyReqDTO.getPayOrderId());
        enrollmentService.handleBalancePaid(notifyReqDTO.getMerchantOrderId(), notifyReqDTO.getPayOrderId());
        return success(true);
    }

}
