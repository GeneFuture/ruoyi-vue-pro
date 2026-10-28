package cn.iocoder.yudao.module.maritime.controller.app.session.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import cn.iocoder.yudao.module.maritime.dal.dataobject.fee.ExtraFeeItem;

import java.util.List;

@Schema(description = "小程序 - 班期详情 Response VO")
@Data
public class AppSessionDetailRespVO {

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    // 所属课程上下文：班期详情有 4 个入口（首页金刚区/Banner、首页近期开班、开班计划列表、
    // 课程详情可报班期），其中 3 个不经过课程详情页，因此这里直接带上课程信息，
    // 避免用户落到班期详情后不知道这门课是干什么的
    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "证书类型")
    private String certificateType;

    @Schema(description = "课程封面图片URL")
    private String coverImage;

    @Schema(description = "课程详情（富文本HTML）")
    private String description;

    @Schema(description = "报名条件说明")
    private String enrollmentConditionText;

    @Schema(description = "班期编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionCode;

    @Schema(description = "班级名称")
    private String sessionName;

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

    @Schema(description = "学费是否包含住宿费用", example = "false")
    private Boolean tuitionIncludesAccommodation;

    @Schema(description = "另行缴费项目（到校报到时缴纳，分必须 / 可选）")
    private List<ExtraFeeItem> extraFeeItems;

    @Schema(description = "定金（元）")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠（元，成团后从尾款抵扣；未成团按原价）")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数")
    private Integer grouponRequiredCount;

    @Schema(description = "退款政策说明")
    private String refundPolicyText;

    @Schema(description = "进行中的拼团列表（空表示暂无拼团）")
    private List<AppActiveGrouponVO> activeGroupons;

    @Schema(description = "学校信息（名称/地址/坐标/概况/住宿/教学），未配置时为 null")
    private AppSessionVenueVO venue;

    @Schema(description = "小程序 - 学校信息 VO")
    @Data
    public static class AppSessionVenueVO {

        @Schema(description = "学校名称", example = "青岛船员培训基地")
        private String campusName;

        @Schema(description = "省", example = "山东省")
        private String province;

        @Schema(description = "市", example = "青岛市")
        private String city;

        @Schema(description = "区 / 县", example = "市北区")
        private String district;

        @Schema(description = "详细地址（省市区之后的部分），小程序端拼上省市区展示", example = "某某路 1 号")
        private String address;

        @Schema(description = "纬度（-90 ~ 90），与 longitude 同时有值才可导航")
        private BigDecimal latitude;

        @Schema(description = "经度（-180 ~ 180），与 latitude 同时有值才可导航")
        private BigDecimal longitude;

        @Schema(description = "咨询电话")
        private String contactPhone;

        @Schema(description = "学校概况（富文本HTML）")
        private String campusIntro;

        @Schema(description = "住宿条件（富文本HTML，可空）")
        private String accommodationIntro;

        @Schema(description = "教学条件（富文本HTML）")
        private String classroomIntro;

    }

}
