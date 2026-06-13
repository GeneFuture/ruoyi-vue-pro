package cn.iocoder.yudao.module.maritime.controller.admin.commissionAccount.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 佣金账户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CommissionAccountRespVO {

    @Schema(description = "账户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1299")
    @ExcelProperty("账户ID")
    private Long id;

    @Schema(description = "推荐人 member_id（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "10887")
    @ExcelProperty("推荐人 member_id（唯一）")
    private Long memberId;

    @Schema(description = "累计佣金总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("累计佣金总额（元）")
    private BigDecimal totalAmount;

    @Schema(description = "已发放金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已发放金额（元）")
    private BigDecimal paidAmount;

    @Schema(description = "待发放金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("待发放金额（元）")
    private BigDecimal pendingAmount;

    @Schema(description = "冻结中金额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结中金额（元）")
    private BigDecimal frozenAmount;

    @Schema(description = "被拒绝的佣金总额（元）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("被拒绝的佣金总额（元）")
    private BigDecimal rejectedAmount;

    @Schema(description = "累计成功推荐人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "29717")
    @ExcelProperty("累计成功推荐人数")
    private Integer totalReferralCount;

    @Schema(description = "乐观锁版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本号")
    private Integer version;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}