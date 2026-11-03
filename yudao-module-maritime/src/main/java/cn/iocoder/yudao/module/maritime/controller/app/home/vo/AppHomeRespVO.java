package cn.iocoder.yudao.module.maritime.controller.app.home.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "小程序 - 首页聚合数据 Response VO")
@Data
public class AppHomeRespVO {

    @Schema(description = "平台统计")
    private StatsVO stats;

    @Schema(description = "近期开班（未来 30 天内）")
    private List<UpcomingSessionVO> upcomingSessions;

    @Schema(description = "平台统计")
    @Data
    public static class StatsVO {

        @Schema(description = "累计学员数（已付定金及以上）", example = "1286")
        private Integer totalStudents;

        @Schema(description = "开班中班期数", example = "12")
        private Integer openSessionCount;

        @Schema(description = "招生中班期剩余名额总数", example = "86")
        private Integer monthRemainingSeats;

    }

    @Schema(description = "近期开班")
    @Data
    public static class UpcomingSessionVO {

        @Schema(description = "班期编号", example = "1")
        private Long id;

        @Schema(description = "课程编号", example = "1")
        private Long courseId;

        @Schema(description = "课程名称", example = "STCW基本安全培训")
        private String courseName;

        @Schema(description = "证书类型", example = "STCW")
        private String certificateType;

        @Schema(description = "课程封面图")
        private String coverImage;

        @Schema(description = "班级名称", example = "9月一期")
        private String sessionName;

        @Schema(description = "班期编号", example = "STCW-202609")
        private String sessionCode;

        @Schema(description = "学校所在城市", example = "青岛市")
        private String city;

        @Schema(description = "开班日期")
        private LocalDate startDate;

        @Schema(description = "剩余名额", example = "8")
        private Integer remainingCount;

        @Schema(description = "最大招生人数", example = "30")
        private Integer maxStudents;

        @Schema(description = "已报名人数", example = "22")
        private Integer enrolledCount;

        @Schema(description = "课程天数", example = "7")
        private Integer durationDays;

        @Schema(description = "培训总费用（元）", example = "3800.00")
        private BigDecimal tuitionAmount;

        @Schema(description = "报名定金（元）", example = "500.00")
        private BigDecimal depositAmount;

        @Schema(description = "是否开启拼团", example = "true")
        private Boolean isGrouponEnabled;

        @Schema(description = "拼团优惠金额（元），未开启拼团时为空", example = "100.00")
        private BigDecimal grouponDiscountAmount;

    }

}
