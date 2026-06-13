package cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 拼团记录分页 Request VO")
@Data
public class GrouponRecordPageReqVO extends PageParam {

    @Schema(description = "班期ID", example = "13340")
    private Long sessionId;

    @Schema(description = "拼团邀请码（唯一）")
    private String inviteCode;

    @Schema(description = "发起人报名ID", example = "5696")
    private Long initiatorEnrollmentId;

    @Schema(description = "需要人数", example = "32675")
    private Integer requiredCount;

    @Schema(description = "当前人数", example = "17641")
    private Integer currentCount;

    @Schema(description = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）", example = "2")
    private String grouponStatus;

    @Schema(description = "拼团截止时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireTime;

    @Schema(description = "拼团成功时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] successTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}