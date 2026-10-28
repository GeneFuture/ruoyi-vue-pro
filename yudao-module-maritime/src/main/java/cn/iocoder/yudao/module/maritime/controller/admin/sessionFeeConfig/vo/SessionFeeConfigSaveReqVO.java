package cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import cn.iocoder.yudao.module.maritime.dal.dataobject.fee.ExtraFeeItem;

import java.util.List;

@Schema(description = "管理后台 - 班期费用配置新增/修改 Request VO")
@Data
public class SessionFeeConfigSaveReqVO {

    @Schema(description = "费用配置ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "5265")
    private Long id;

    @Schema(description = "班期ID（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5994")
    @NotNull(message = "班期ID（唯一）不能为空")
    private Long sessionId;

    @Schema(description = "学费总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学费总额（元）不能为空")
    private BigDecimal tuitionAmount;

    @Schema(description = "学费是否包含住宿费用", example = "false")
    private Boolean tuitionIncludesAccommodation;

    @Schema(description = "另行缴费项目（到校报到时缴纳，分必须 / 可选）")
    private List<ExtraFeeItem> extraFeeItems;

    @Schema(description = "定金金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定金金额（元）不能为空")
    private BigDecimal depositAmount;

    @Schema(description = "是否开启拼团", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否开启拼团不能为空")
    private Boolean isGrouponEnabled;

    @Schema(description = "拼团优惠减免金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拼团优惠减免金额（元）不能为空")
    private BigDecimal grouponDiscountAmount;

    @Schema(description = "拼团所需人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "17593")
    @NotNull(message = "拼团所需人数不能为空")
    private Integer grouponRequiredCount;

    @Schema(description = "拼团有效时间（小时）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拼团有效时间（小时）不能为空")
    private Integer grouponExpireHours;

    @Schema(description = "拼团失败降级优惠金额（元，单人可享）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拼团失败降级优惠金额（元，单人可享）不能为空")
    private BigDecimal grouponFailDiscountAmount;

    @Schema(description = "定金是否可退", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定金是否可退不能为空")
    private Boolean isDepositRefundable;

    @Schema(description = "尾款截止：开班前N天", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "尾款截止：开班前N天不能为空")
    private Integer balanceDueDaysBeforeStart;

    @Schema(description = "退款政策说明文本")
    private String refundPolicyText;

}