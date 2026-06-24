package cn.iocoder.yudao.module.maritime.controller.admin.riskEvent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 风控事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RiskEventPageReqVO extends PageParam {

    @Schema(description = "事件类型（BATCH_REFERRAL / GROUPON_FRAUD）", example = "BATCH_REFERRAL")
    private String eventType;

    @Schema(description = "涉及用户ID", example = "1024")
    private Long memberId;

    @Schema(description = "是否已审核（0否 1是）", example = "0")
    private Boolean isReviewed;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
