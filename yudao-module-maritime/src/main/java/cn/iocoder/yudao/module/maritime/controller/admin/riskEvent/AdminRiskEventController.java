package cn.iocoder.yudao.module.maritime.controller.admin.riskEvent;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo.RiskEventPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo.RiskEventRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo.RiskEventReviewReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.riskEvent.RiskEventDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.riskEvent.RiskEventMapper;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.RISK_EVENT_NOT_EXISTS;

@Tag(name = "管理后台 - 风控事件")
@RestController
@RequestMapping("/maritime/risk-event")
@Validated
@Slf4j
public class AdminRiskEventController {

    @Resource
    private RiskEventMapper riskEventMapper;

    @Resource
    private MemberUserMapper memberUserMapper;

    @GetMapping("/page")
    @Operation(summary = "获取风控事件分页列表")
    @PreAuthorize("@ss.hasPermission('maritime:risk-event:query')")
    public CommonResult<PageResult<RiskEventRespVO>> getRiskEventPage(@Valid RiskEventPageReqVO pageReqVO) {
        PageResult<RiskEventDO> pageResult = riskEventMapper.selectPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RiskEventRespVO.class));
    }

    @PutMapping("/review")
    @Operation(summary = "审核风控事件")
    @PreAuthorize("@ss.hasPermission('maritime:risk-event:review')")
    public CommonResult<Boolean> reviewRiskEvent(@Valid @RequestBody RiskEventReviewReqVO reqVO) {
        RiskEventDO event = riskEventMapper.selectById(reqVO.getId());
        if (event == null) {
            throw exception(RISK_EVENT_NOT_EXISTS);
        }

        // 更新审核结果
        RiskEventDO update = new RiskEventDO();
        update.setId(reqVO.getId());
        update.setIsReviewed(true);
        update.setReviewerId(getLoginUserId());
        update.setReviewResult(reqVO.getReviewResult());
        update.setReviewRemark(reqVO.getReviewRemark());
        riskEventMapper.updateById(update);

        // NORMAL：审核正常，解除禁推
        if ("NORMAL".equals(reqVO.getReviewResult())) {
            memberUserMapper.update(null, new LambdaUpdateWrapper<MemberUserDO>()
                    .set(MemberUserDO::getIsReferralBanned, false)
                    .eq(MemberUserDO::getId, event.getMemberId()));
            log.info("[riskEvent] 审核正常，已解除禁推: memberId={}, eventId={}", event.getMemberId(), event.getId());
        } else {
            log.info("[riskEvent] 审核异常，维持禁推: memberId={}, eventId={}", event.getMemberId(), event.getId());
        }

        return success(true);
    }

}
