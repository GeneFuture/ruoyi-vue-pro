package cn.iocoder.yudao.module.maritime.controller.admin.session.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 班期管理分页 Request VO")
@Data
public class CourseSessionPageReqVO extends PageParam {

    @Schema(description = "课程ID", example = "8931")
    private Long courseId;

    @Schema(description = "班期编号（如：2026-06-SH-001）")
    private String sessionCode;

    @Schema(description = "班级名称（可选，如：A班）", example = "芋艿")
    private String sessionName;

    @Schema(description = "本期上课地点")
    private String location;

    @Schema(description = "本期课程天数")
    private Integer durationDays;

    @Schema(description = "本期讲师信息")
    private String instructorInfo;

    @Schema(description = "开班日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] startDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] endDate;

    @Schema(description = "报名截止日期")
    private LocalDate enrollmentDeadline;

    @Schema(description = "最大招生人数")
    private Integer maxStudents;

    @Schema(description = "已报名人数（缓存值，以 enrollment 表实际计数为准）", example = "22608")
    private Integer enrolledCount;

    @Schema(description = "班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）", example = "1")
    private String sessionStatus;

    @Schema(description = "班期备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}