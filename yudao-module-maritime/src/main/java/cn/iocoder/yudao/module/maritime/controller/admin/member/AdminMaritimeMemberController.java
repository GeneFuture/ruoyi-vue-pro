package cn.iocoder.yudao.module.maritime.controller.admin.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.maritime.controller.admin.member.vo.AdminMemberBanReqVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.RISK_MEMBER_NOT_EXISTS;

@Tag(name = "管理后台 - 海员会员管理")
@RestController
@RequestMapping("/maritime/member")
@Validated
@Slf4j
public class AdminMaritimeMemberController {

    @Resource
    private MemberUserMapper memberUserMapper;

    @PutMapping("/ban")
    @Operation(summary = "封禁/禁推会员")
    @PreAuthorize("@ss.hasPermission('maritime:member:ban')")
    public CommonResult<Boolean> banMember(@Valid @RequestBody AdminMemberBanReqVO reqVO) {
        MemberUserDO user = memberUserMapper.selectById(reqVO.getMemberId());
        if (user == null) {
            throw exception(RISK_MEMBER_NOT_EXISTS);
        }

        LambdaUpdateWrapper<MemberUserDO> wrapper = new LambdaUpdateWrapper<MemberUserDO>()
                .eq(MemberUserDO::getId, reqVO.getMemberId());

        if (Boolean.TRUE.equals(reqVO.getReferralBanned())) {
            wrapper.set(MemberUserDO::getIsReferralBanned, true);
            log.info("[member] 管理员禁推: memberId={}, reason={}", reqVO.getMemberId(), reqVO.getReason());
        }

        memberUserMapper.update(null, wrapper);
        return success(true);
    }

    @PutMapping("/unban")
    @Operation(summary = "解封/解除禁推会员")
    @PreAuthorize("@ss.hasPermission('maritime:member:ban')")
    public CommonResult<Boolean> unbanMember(@Valid @RequestBody AdminMemberBanReqVO reqVO) {
        MemberUserDO user = memberUserMapper.selectById(reqVO.getMemberId());
        if (user == null) {
            throw exception(RISK_MEMBER_NOT_EXISTS);
        }

        memberUserMapper.update(null, new LambdaUpdateWrapper<MemberUserDO>()
                .set(MemberUserDO::getIsReferralBanned, false)
                .eq(MemberUserDO::getId, reqVO.getMemberId()));

        log.info("[member] 管理员解除禁推: memberId={}", reqVO.getMemberId());
        return success(true);
    }

}
