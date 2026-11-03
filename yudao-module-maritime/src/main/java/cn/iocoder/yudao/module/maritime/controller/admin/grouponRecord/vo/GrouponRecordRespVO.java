package cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 拼团记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GrouponRecordRespVO {

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12326")
    private Long id;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "13340")
    private Long sessionId;

    @Schema(description = "课程名称", example = "基本安全培训")
    @ExcelProperty("课程名称")
    private String courseName;

    @Schema(description = "班期编号", example = "STCW-202607-001")
    @ExcelProperty("班期编号")
    private String sessionCode;

    @Schema(description = "班级名称", example = "A班")
    @ExcelProperty("班级名称")
    private String sessionName;

    @Schema(description = "开班日期", example = "2026-11-02")
    @ExcelProperty("开班日期")
    private LocalDate sessionStartDate;

    @Schema(description = "拼团邀请码（唯一）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团邀请码")
    private String inviteCode;

    @Schema(description = "发起人报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5696")
    private Long initiatorEnrollmentId;

    @Schema(description = "发起人报名单号", example = "EN202611020001")
    @ExcelProperty("发起人报名单号")
    private String initiatorEnrollmentNo;

    @Schema(description = "发起人姓名", example = "张三")
    @ExcelProperty("发起人姓名")
    private String initiatorRealName;

    @Schema(description = "发起人手机号", example = "13800000000")
    @ExcelProperty("发起人手机号")
    private String initiatorPhone;

    @Schema(description = "需要人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "32675")
    @ExcelProperty("需要人数")
    private Integer requiredCount;

    @Schema(description = "当前人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "17641")
    @ExcelProperty("当前人数")
    private Integer currentCount;

    @Schema(description = "拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("拼团状态")
    private String grouponStatus;

    @Schema(description = "拼团截止时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团截止时间")
    private LocalDateTime expireTime;

    @Schema(description = "拼团成功时间")
    @ExcelProperty("拼团成功时间")
    private LocalDateTime successTime;

    @Schema(description = "拼团结束原因（DEGRADED 时填写）")
    @ExcelProperty("拼团结束原因")
    private String degradeReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}