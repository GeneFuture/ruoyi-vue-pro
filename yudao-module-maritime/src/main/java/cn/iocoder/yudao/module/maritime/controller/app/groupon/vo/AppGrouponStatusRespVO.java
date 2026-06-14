package cn.iocoder.yudao.module.maritime.controller.app.groupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 APP - 我的拼团状态 Response VO")
@Data
public class AppGrouponStatusRespVO {

    @Schema(description = "拼团记录ID", example = "1024")
    private Long grouponRecordId;

    @Schema(description = "邀请码（可分享给他人）", example = "GRP-ABCD1234")
    private String inviteCode;

    @Schema(description = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）", example = "IN_PROGRESS")
    private String grouponStatus;

    @Schema(description = "当前人数", example = "1")
    private Integer currentCount;

    @Schema(description = "目标人数", example = "3")
    private Integer requiredCount;

    @Schema(description = "还差几人", example = "2")
    private Integer remainingCount;

    @Schema(description = "截止时间", example = "2024-01-01T12:00:00")
    private LocalDateTime expireTime;

    @Schema(description = "成团时间（仅 SUCCESS 状态有值）")
    private LocalDateTime successTime;

}
