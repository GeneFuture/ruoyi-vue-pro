package cn.iocoder.yudao.module.maritime.controller.app.referral.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 推荐记录 Response VO")
@Data
public class AppReferralRecordRespVO {

    @Schema(description = "推荐关系ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "被推荐人姓名（脱敏，如'张**'）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String referredName;

    @Schema(description = "班期名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionName;

    @Schema(description = "推荐时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "推荐关系状态（ACTIVE/INVALID）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String relationStatus;

    @Schema(description = "对应佣金金额（元），无佣金时为 null")
    private BigDecimal commissionAmount;

    @Schema(description = "佣金状态，无佣金时为 null")
    private String commissionStatus;
}
