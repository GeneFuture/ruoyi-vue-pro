package cn.iocoder.yudao.module.maritime.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Schema(description = "小程序 - 班期详情 Response VO")
@Data
public class AppSessionDetailRespVO {

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

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

    @Schema(description = "班期状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionStatus;

    @Schema(description = "班期备注")
    private String remark;

    // 费用信息
    @Schema(description = "学费（元）")
    private BigDecimal tuitionAmount;

    @Schema(description = "学费说明（key=名称, value=金额描述）")
    private Map<String, String> tuitionDescription;

    @Schema(description = "定金（元）")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团定金（元，= 定金 - 拼团优惠）")
    private BigDecimal grouponDepositAmount;

    @Schema(description = "拼团所需人数")
    private Integer grouponRequiredCount;

    @Schema(description = "推荐佣金（元）")
    private BigDecimal referralCommissionAmount;

    @Schema(description = "是否开启推荐佣金")
    private Boolean isReferralCommissionEnabled;

    @Schema(description = "退款政策说明")
    private String refundPolicyText;

    @Schema(description = "进行中的拼团列表（空表示暂无拼团）")
    private List<AppActiveGrouponVO> activeGroupons;

}
