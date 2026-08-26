package cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "管理后台 - 费用模板新增/修改 Request VO")
@Data
public class FeeTemplateSaveReqVO {

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "模板名称（如：STCW 标准收费）", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW 标准收费")
    @NotEmpty(message = "模板名称不能为空")
    private String name;

    @Schema(description = "学费总额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800")
    @NotNull(message = "学费总额不能为空")
    private BigDecimal tuitionAmount;

    @Schema(description = "学费说明（{\"理论课\":\"1000\",\"实操\":\"800\"}）")
    private Map<String, String> tuitionDescription;

    @Schema(description = "定金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "300")
    @NotNull(message = "定金金额不能为空")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否开启拼团不能为空")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠减免金额（元）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "拼团优惠减免金额不能为空")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "拼团所需人数不能为空")
    private Integer grouponRequiredCount;

    @Schema(description = "拼团有效时间（小时）", requiredMode = Schema.RequiredMode.REQUIRED, example = "24")
    @NotNull(message = "拼团有效时间不能为空")
    private Integer grouponExpireHours;

    @Schema(description = "拼团失败降级优惠金额（元，单人可享）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "拼团失败降级优惠金额不能为空")
    private BigDecimal grouponFailDiscountAmount;

    @Schema(description = "推荐佣金金额（元，每成功推荐一人）", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @NotNull(message = "推荐佣金金额不能为空")
    private BigDecimal referralCommissionAmount;

    @Schema(description = "是否开启推荐佣金", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否开启推荐佣金不能为空")
    private Boolean isReferralCommissionEnabled;

    @Schema(description = "定金是否可退", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "定金是否可退不能为空")
    private Boolean isDepositRefundable;

    @Schema(description = "尾款截止：开班前N天", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "尾款截止天数不能为空")
    private Integer balanceDueDaysBeforeStart;

    @Schema(description = "退款政策说明文本")
    private String refundPolicyText;

    @Schema(description = "状态（0停用 1启用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
