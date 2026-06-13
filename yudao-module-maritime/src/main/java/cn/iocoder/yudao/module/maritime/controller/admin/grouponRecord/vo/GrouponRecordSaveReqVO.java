package cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 拼团记录新增/修改 Request VO")
@Data
public class GrouponRecordSaveReqVO {

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12326")
    private Long id;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13340")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "拼团邀请码（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "拼团邀请码（唯一）不能为空")
    private String inviteCode;

    @Schema(description = "发起人报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5696")
    @NotNull(message = "发起人报名ID不能为空")
    private Long initiatorEnrollmentId;

    @Schema(description = "需要人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "32675")
    @NotNull(message = "需要人数不能为空")
    private Integer requiredCount;

    @Schema(description = "当前人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "17641")
    @NotNull(message = "当前人数不能为空")
    private Integer currentCount;

    @Schema(description = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）不能为空")
    private String grouponStatus;

    @Schema(description = "拼团截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拼团截止时间不能为空")
    private LocalDateTime expireTime;

    @Schema(description = "拼团成功时间")
    private LocalDateTime successTime;

}