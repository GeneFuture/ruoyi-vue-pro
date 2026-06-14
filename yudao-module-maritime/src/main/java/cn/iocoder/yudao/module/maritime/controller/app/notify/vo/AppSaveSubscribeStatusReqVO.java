package cn.iocoder.yudao.module.maritime.controller.app.notify.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 APP - 保存微信订阅状态 Request VO")
@Data
public class AppSaveSubscribeStatusReqVO {

    @Schema(description = "微信订阅消息模板标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "报名成功通知")
    @NotBlank(message = "模板标题不能为空")
    private String templateTitle;

    @Schema(description = "是否已授权订阅", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "订阅状态不能为空")
    private Boolean subscribed;

}
