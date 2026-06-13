package cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 风控黑名单分页 Request VO")
@Data
public class RiskBlacklistPageReqVO extends PageParam {

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "加入黑名单原因", example = "不好")
    private String reason;

    @Schema(description = "操作人（管理员）", example = "2478")
    private Long operatorId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}