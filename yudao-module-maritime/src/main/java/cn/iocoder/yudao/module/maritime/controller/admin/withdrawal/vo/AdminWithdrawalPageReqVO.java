package cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 提现申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdminWithdrawalPageReqVO extends PageParam {

    @Schema(description = "申请人会员ID", example = "1024")
    private Long memberId;

    @Schema(description = "状态（PENDING/PROCESSING/SUCCESS/FAILED/REJECTED）", example = "PENDING")
    private String applyStatus;

    @Schema(description = "提现渠道（WX_BALANCE/BANK_CARD）", example = "WX_BALANCE")
    private String channel;

    @Schema(description = "申请时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
