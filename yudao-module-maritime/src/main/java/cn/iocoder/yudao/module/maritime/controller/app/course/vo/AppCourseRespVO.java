package cn.iocoder.yudao.module.maritime.controller.app.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "小程序 - 课程卡片 Response VO")
@Data
public class AppCourseRespVO {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "证书类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String certificateType;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "最近开放班期ID")
    private Long nearestSessionId;

    @Schema(description = "最近开放班期开班日期")
    private LocalDate nearestStartDate;

    @Schema(description = "最近开放班期上课地点")
    private String location;

    @Schema(description = "最近开放班期课程天数")
    private Integer durationDays;

    @Schema(description = "最近开放班期状态")
    private String sessionStatus;

    @Schema(description = "最低学费（元）")
    private BigDecimal minTuition;

    @Schema(description = "最高学费（元）")
    private BigDecimal maxTuition;

}
