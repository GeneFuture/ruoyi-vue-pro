package cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 班期费用配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SessionFeeConfigRespVO {

    @Schema(description = "费用配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5265")
    @ExcelProperty("费用配置ID")
    private Long id;

    @Schema(description = "班期ID（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5994")
    @ExcelProperty("班期ID（唯一）")
    private Long sessionId;

    @Schema(description = "学费总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("学费总额（元）")
    private BigDecimal tuitionAmount;

    @Schema(description = "学费说明（key=名称, value=金额描述）")
    @ExcelProperty("学费说明")
    private Map<String, String> tuitionDescription;

    @Schema(description = "定金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定金金额（元）")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否开启拼团")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠减免金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团优惠减免金额（元）")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "17593")
    @ExcelProperty("拼团所需人数")
    private Integer grouponRequiredCount;

    @Schema(description = "拼团有效时间（小时）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团有效时间（小时）")
    private Integer grouponExpireHours;

    @Schema(description = "拼团失败降级优惠金额（元，单人可享）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("拼团失败降级优惠金额（元，单人可享）")
    private BigDecimal grouponFailDiscountAmount;

    @Schema(description = "推荐佣金金额（元，每成功推荐一人）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("推荐佣金金额（元，每成功推荐一人）")
    private BigDecimal referralCommissionAmount;

    @Schema(description = "是否开启推荐佣金", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否开启推荐佣金")
    private Boolean isReferralCommissionEnabled;

    @Schema(description = "定金是否可退", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定金是否可退")
    private Boolean isDepositRefundable;

    @Schema(description = "尾款截止：开班前N天", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("尾款截止：开班前N天")
    private Integer balanceDueDaysBeforeStart;

    @Schema(description = "退款政策说明文本")
    @ExcelProperty("退款政策说明文本")
    private String refundPolicyText;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}