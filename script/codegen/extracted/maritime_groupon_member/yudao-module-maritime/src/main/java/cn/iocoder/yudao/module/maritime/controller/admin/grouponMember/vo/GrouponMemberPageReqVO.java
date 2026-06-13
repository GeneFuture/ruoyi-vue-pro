package cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 拼团成员分页 Request VO")
@Data
public class GrouponMemberPageReqVO extends PageParam {

    @Schema(description = "拼团记录ID", example = "18827")
    private Long grouponRecordId;

    @Schema(description = "报名ID", example = "969")
    private Long enrollmentId;

    @Schema(description = "学员ID", example = "6388")
    private Long memberId;

    @Schema(description = "加入时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] joinTime;

    @Schema(description = "成员状态（ACTIVE正常/CANCELLED已退出）", example = "2")
    private String memberStatus;

    @Schema(description = "定金支付时间（用于24h拼团有效期检查）")
    private LocalDateTime depositPaidAt;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}