package cn.iocoder.yudao.module.maritime.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "小程序 - 班期列表项 Response VO")
@Data
public class AppSessionRespVO {

    @Schema(description = "班期编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "课程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long courseId;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW基本安全培训")
    private String courseName;

    @Schema(description = "证书类型", example = "STCW")
    private String certificateType;

    @Schema(description = "课程封面图", example = "https://www.example.com/xxx.png")
    private String coverImage;

    @Schema(description = "班期编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW-202609")
    private String sessionCode;

    @Schema(description = "班级名称", example = "9月一期")
    private String sessionName;

    @Schema(description = "学校所在城市", example = "青岛市")
    private String city;

    @Schema(description = "开班日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "课程天数", example = "4")
    private Integer durationDays;

    @Schema(description = "报名截止日期")
    private LocalDate enrollmentDeadline;

    @Schema(description = "最大招生人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer maxStudents;

    @Schema(description = "已报名人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer enrolledCount;

    @Schema(description = "剩余名额", requiredMode = Schema.RequiredMode.REQUIRED, example = "18")
    private Integer remainingCount;

    @Schema(description = "学费总额（元）", example = "3800.00")
    private BigDecimal tuitionAmount;

    @Schema(description = "报名定金（元）", example = "500.00")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团", example = "true")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠（元，成团后从尾款抵扣）", example = "100.00")
    private BigDecimal grouponDiscountAmount;

}
