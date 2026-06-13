package cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 推荐关系分页 Request VO")
@Data
public class ReferralRelationPageReqVO extends PageParam {

    @Schema(description = "推荐人 member_id", example = "21268")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", example = "22435")
    private Long referredEnrollmentId;

    @Schema(description = "被推荐人 member_id", example = "12587")
    private Long referredMemberId;

    @Schema(description = "班期ID", example = "32343")
    private Long sessionId;

    @Schema(description = "关系状态（ACTIVE/INVALID）", example = "1")
    private String relationStatus;

    @Schema(description = "无效原因（风控拒绝等）", example = "不好")
    private String invalidReason;

    @Schema(description = "推荐链接首次点击时间")
    private LocalDateTime firstClickAt;

    @Schema(description = "推荐关系锁定时间（被推荐人支付定金时）")
    private LocalDateTime referralLockedAt;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}