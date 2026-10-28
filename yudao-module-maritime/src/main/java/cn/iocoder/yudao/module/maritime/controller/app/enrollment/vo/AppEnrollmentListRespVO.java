package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "小程序 - 我的报名列表项 Response VO")
@Data
public class AppEnrollmentListRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "报名单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "EN20240101000001")
    private String enrollmentNo;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long sessionId;

    @Schema(description = "班期编号", example = "STCW-2024-01")
    private String sessionCode;

    @Schema(description = "班级名称", example = "2024年1月STCW基本安全培训班")
    private String sessionName;

    @Schema(description = "开班日期", example = "2024-01-15")
    private LocalDate startDate;

    @Schema(description = "学校所在城市", example = "青岛市")
    private String city;

    @Schema(description = "定金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "学费总额（元）")
    private BigDecimal totalAmount;

    @Schema(description = "是否全款报名（定金为 0 或 ≥ 学费）")
    private Boolean fullPayment;

    @Schema(description = "报名状态", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "PENDING_DEPOSIT",
            allowableValues = {"PENDING_DEPOSIT", "DEPOSITED", "PAID_OFF", "MANUAL_PROCESSING", "COMPLETED", "CANCELLED"})
    private String enrollmentStatus;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
