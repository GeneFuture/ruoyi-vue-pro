package cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 佣金账户新增/修改 Request VO")
@Data
public class CommissionAccountSaveReqVO {

    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1299")
    private Long id;

    @Schema(description = "推荐人 member_id（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10887")
    @NotNull(message = "推荐人 member_id（唯一）不能为空")
    private Long memberId;

    @Schema(description = "累计佣金总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计佣金总额（元）不能为空")
    private BigDecimal totalAmount;

    @Schema(description = "已发放金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已发放金额（元）不能为空")
    private BigDecimal paidAmount;

    @Schema(description = "待发放金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "待发放金额（元）不能为空")
    private BigDecimal pendingAmount;

    @Schema(description = "冻结中金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "冻结中金额（元）不能为空")
    private BigDecimal frozenAmount;

    @Schema(description = "被拒绝的佣金总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "被拒绝的佣金总额（元）不能为空")
    private BigDecimal rejectedAmount;

    @Schema(description = "累计成功推荐人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "29717")
    @NotNull(message = "累计成功推荐人数不能为空")
    private Integer totalReferralCount;

    @Schema(description = "乐观锁版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本号不能为空")
    private Integer version;

}