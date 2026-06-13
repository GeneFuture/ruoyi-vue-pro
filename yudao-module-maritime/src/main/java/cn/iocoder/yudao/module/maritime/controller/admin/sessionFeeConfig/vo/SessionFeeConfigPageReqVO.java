package cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 班期费用配置分页 Request VO")
@Data
public class SessionFeeConfigPageReqVO extends PageParam {

    @Schema(description = "班期ID（唯一）", example = "5994")
    private Long sessionId;

    @Schema(description = "学费总额（元）")
    private BigDecimal tuitionAmount;

    @Schema(description = "定金金额（元）")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠减免金额（元）")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数", example = "17593")
    private Integer grouponRequiredCount;

    @Schema(description = "拼团有效时间（小时）")
    private Integer grouponExpireHours;

    @Schema(description = "拼团失败降级优惠金额（元，单人可享）")
    private BigDecimal grouponFailDiscountAmount;

    @Schema(description = "推荐佣金金额（元，每成功推荐一人）")
    private BigDecimal referralCommissionAmount;

    @Schema(description = "是否开启推荐佣金")
    private Boolean isReferralCommissionEnabled;

    @Schema(description = "定金是否可退")
    private Boolean isDepositRefundable;

    @Schema(description = "尾款截止：开班前N天")
    private Integer balanceDueDaysBeforeStart;

    @Schema(description = "退款政策说明文本")
    private String refundPolicyText;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}