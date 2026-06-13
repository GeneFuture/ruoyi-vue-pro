package cn.iocoder.yudao.module.maritime.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "小程序 - 进行中的拼团信息 VO")
@Data
public class AppActiveGrouponVO {

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "拼团邀请码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inviteCode;

    @Schema(description = "当前人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer currentCount;

    @Schema(description = "所需人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requiredCount;

    @Schema(description = "剩余人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer remainingCount;

    @Schema(description = "截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expireTime;

}
