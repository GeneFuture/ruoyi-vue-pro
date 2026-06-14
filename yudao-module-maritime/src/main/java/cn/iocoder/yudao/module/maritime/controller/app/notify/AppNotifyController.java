package cn.iocoder.yudao.module.maritime.controller.app.notify;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.maritime.controller.app.notify.vo.AppSaveSubscribeStatusReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.memberSubscribe.MemberSubscribeDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.memberSubscribe.MemberSubscribeMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 消息通知")
@RestController
@RequestMapping("/maritime/notify")
@Validated
public class AppNotifyController {

    @Resource
    private MemberSubscribeMapper memberSubscribeMapper;

    @PostMapping("/save-subscribe-status")
    @Operation(summary = "保存微信订阅消息授权状态")
    public CommonResult<Boolean> saveSubscribeStatus(@Valid @RequestBody AppSaveSubscribeStatusReqVO reqVO) {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        MemberSubscribeDO record = MemberSubscribeDO.builder()
                .memberId(memberId)
                .templateTitle(reqVO.getTemplateTitle())
                .subscribed(reqVO.getSubscribed())
                .build();
        memberSubscribeMapper.upsert(record);
        return success(true);
    }

}
