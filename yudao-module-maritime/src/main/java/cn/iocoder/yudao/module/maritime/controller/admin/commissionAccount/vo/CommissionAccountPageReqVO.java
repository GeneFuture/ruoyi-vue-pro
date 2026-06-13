package cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 佣金账户分页 Request VO")
@Data
public class CommissionAccountPageReqVO extends PageParam {

    @Schema(description = "推荐人 member_id（唯一）", example = "10887")
    private Long memberId;

    @Schema(description = "累计佣金总额（元）")
    private BigDecimal totalAmount;

    @Schema(description = "已发放金额（元）")
    private BigDecimal paidAmount;

    @Schema(description = "待发放金额（元）")
    private BigDecimal pendingAmount;

    @Schema(description = "冻结中金额（元）")
    private BigDecimal frozenAmount;

    @Schema(description = "被拒绝的佣金总额（元）")
    private BigDecimal rejectedAmount;

    @Schema(description = "累计成功推荐人数", example = "29717")
    private Integer totalReferralCount;

    @Schema(description = "乐观锁版本号")
    private Integer version;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}