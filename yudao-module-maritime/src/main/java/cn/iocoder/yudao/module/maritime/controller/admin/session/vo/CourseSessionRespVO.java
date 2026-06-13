package cn.iocoder.yudao.module.maritime.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 班期管理 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CourseSessionRespVO {

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6879")
    @ExcelProperty("班期ID")
    private Long id;

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8931")
    @ExcelProperty("课程ID")
    private Long courseId;

    @Schema(description = "班期编号（如：2026-06-SH-001）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("班期编号（如：2026-06-SH-001）")
    private String sessionCode;

    @Schema(description = "班级名称（可选，如：A班）", example = "芋艿")
    @ExcelProperty("班级名称（可选，如：A班）")
    private String sessionName;

    @Schema(description = "本期上课地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本期上课地点")
    private String location;

    @Schema(description = "本期课程天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本期课程天数")
    private Integer durationDays;

    @Schema(description = "本期讲师信息")
    @ExcelProperty("本期讲师信息")
    private String instructorInfo;

    @Schema(description = "开班日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开班日期")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("结束日期")
    private LocalDate endDate;

    @Schema(description = "报名截止日期")
    @ExcelProperty("报名截止日期")
    private LocalDate enrollmentDeadline;

    @Schema(description = "最大招生人数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("最大招生人数")
    private Integer maxStudents;

    @Schema(description = "已报名人数（缓存值，以 enrollment 表实际计数为准）", requiredMode = Schema.RequiredMode.REQUIRED, example = "22608")
    @ExcelProperty("已报名人数（缓存值，以 enrollment 表实际计数为准）")
    private Integer enrolledCount;

    @Schema(description = "班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）")
    private String sessionStatus;

    @Schema(description = "班期备注", example = "随便")
    @ExcelProperty("班期备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}