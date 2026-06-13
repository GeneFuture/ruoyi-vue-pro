package cn.iocoder.yudao.module.maritime.controller.app.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "小程序 - 课程详情 Response VO")
@Data
public class AppCourseDetailRespVO {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "证书类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String certificateType;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "课程详情（富文本HTML）")
    private String description;

    @Schema(description = "报名条件说明")
    private String enrollmentConditionText;

    @Schema(description = "可报名班期列表（仅OPEN状态）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppSessionSummaryVO> openSessions;

    @Schema(description = "小程序 - 课程下开放班期摘要 VO")
    @Data
    public static class AppSessionSummaryVO {

        @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long id;

        @Schema(description = "班期编号", requiredMode = Schema.RequiredMode.REQUIRED)
        private String sessionCode;

        @Schema(description = "班级名称")
        private String sessionName;

        @Schema(description = "上课地点", requiredMode = Schema.RequiredMode.REQUIRED)
        private String location;

        @Schema(description = "课程天数", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer durationDays;

        @Schema(description = "讲师信息")
        private String instructorInfo;

        @Schema(description = "开班日期", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDate startDate;

        @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDate endDate;

        @Schema(description = "报名截止日期")
        private LocalDate enrollmentDeadline;

        @Schema(description = "最大招生人数", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer maxStudents;

        @Schema(description = "已报名人数", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer enrolledCount;

        @Schema(description = "剩余名额", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer remainingCount;

        @Schema(description = "学费（元）")
        private BigDecimal tuitionAmount;

        @Schema(description = "定金（元）")
        private BigDecimal depositAmount;

        @Schema(description = "是否开启拼团")
        private Boolean isGrouponEnabled;

        @Schema(description = "拼团定金（元，= 定金 - 拼团优惠）")
        private BigDecimal grouponDepositAmount;

    }

}
