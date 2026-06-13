package cn.iocoder.yudao.module.maritime.controller.admin.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "管理后台 - 班期管理新增/修改 Request VO")
@Data
public class CourseSessionSaveReqVO {

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6879")
    private Long id;

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "8931")
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "班期编号（如：2026-06-SH-001）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "班期编号（如：2026-06-SH-001）不能为空")
    private String sessionCode;

    @Schema(description = "班级名称（可选，如：A班）", example = "芋艿")
    private String sessionName;

    @Schema(description = "本期上课地点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "本期上课地点不能为空")
    private String location;

    @Schema(description = "本期课程天数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本期课程天数不能为空")
    private Integer durationDays;

    @Schema(description = "本期讲师信息")
    private String instructorInfo;

    @Schema(description = "开班日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开班日期不能为空")
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Schema(description = "报名截止日期")
    private LocalDate enrollmentDeadline;

    @Schema(description = "最大招生人数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "最大招生人数不能为空")
    private Integer maxStudents;

    @Schema(description = "已报名人数（缓存值，以 enrollment 表实际计数为准）", requiredMode = Schema.RequiredMode.REQUIRED, example = "22608")
    @NotNull(message = "已报名人数（缓存值，以 enrollment 表实际计数为准）不能为空")
    private Integer enrolledCount;

    @Schema(description = "班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）不能为空")
    private String sessionStatus;

    @Schema(description = "班期备注", example = "随便")
    private String remark;

}