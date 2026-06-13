package cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 拼团记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GrouponRecordRespVO {

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12326")
    @ExcelProperty("拼团记录ID")
    private Long id;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13340")
    @ExcelProperty("班期ID")
    private Long sessionId;

    @Schema(description = "拼团邀请码（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团邀请码（唯一）")
    private String inviteCode;

    @Schema(description = "发起人报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5696")
    @ExcelProperty("发起人报名ID")
    private Long initiatorEnrollmentId;

    @Schema(description = "需要人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "32675")
    @ExcelProperty("需要人数")
    private Integer requiredCount;

    @Schema(description = "当前人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "17641")
    @ExcelProperty("当前人数")
    private Integer currentCount;

    @Schema(description = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）")
    private String grouponStatus;

    @Schema(description = "拼团截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团截止时间")
    private LocalDateTime expireTime;

    @Schema(description = "拼团成功时间")
    @ExcelProperty("拼团成功时间")
    private LocalDateTime successTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}