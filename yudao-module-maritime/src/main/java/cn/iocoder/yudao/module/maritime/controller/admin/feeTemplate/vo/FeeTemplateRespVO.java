package cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 费用模板 Response VO")
@Data
public class FeeTemplateRespVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板名称（如：STCW 标准收费）", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW 标准收费")
    private String name;

    @Schema(description = "学费总额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800")
    private BigDecimal tuitionAmount;

    @Schema(description = "学费说明（{\"理论课\":\"1000\",\"实操\":\"800\"}）")
    private Map<String, String> tuitionDescription;

    @Schema(description = "定金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠减免金额（元）", example = "100")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数", example = "3")
    private Integer grouponRequiredCount;

    @Schema(description = "拼团有效时间（小时）", example = "24")
    private Integer grouponExpireHours;

    @Schema(description = "拼团失败降级优惠金额（元，单人可享）", example = "50")
    private BigDecimal grouponFailDiscountAmount;

    @Schema(description = "推荐佣金金额（元，每成功推荐一人）", example = "50")
    private BigDecimal referralCommissionAmount;

    @Schema(description = "是否开启推荐佣金", example = "true")
    private Boolean isReferralCommissionEnabled;

    @Schema(description = "定金是否可退", example = "true")
    private Boolean isDepositRefundable;

    @Schema(description = "尾款截止：开班前N天", example = "7")
    private Integer balanceDueDaysBeforeStart;

    @Schema(description = "退款政策说明文本")
    private String refundPolicyText;

    @Schema(description = "状态（0停用 1启用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
