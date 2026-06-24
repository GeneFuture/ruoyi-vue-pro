package cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 风控事件 Response VO")
@Data
public class RiskEventRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "事件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "BATCH_REFERRAL")
    private String eventType;

    @Schema(description = "涉及用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long memberId;

    @Schema(description = "事件详情", example = "24h内推荐超过限制")
    private String detail;

    @Schema(description = "是否已审核", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isReviewed;

    @Schema(description = "审核人管理员ID", example = "1")
    private Long reviewerId;

    @Schema(description = "审核结果（NORMAL / ABNORMAL）", example = "NORMAL")
    private String reviewResult;

    @Schema(description = "审核备注", example = "正常推荐行为")
    private String reviewRemark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
