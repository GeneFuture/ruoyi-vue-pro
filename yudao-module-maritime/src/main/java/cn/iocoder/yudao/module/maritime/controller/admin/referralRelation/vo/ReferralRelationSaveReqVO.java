package cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 推荐关系新增/修改 Request VO")
@Data
public class ReferralRelationSaveReqVO {

    @Schema(description = "推荐关系ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14844")
    private Long id;

    @Schema(description = "推荐人 member_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21268")
    @NotNull(message = "推荐人 member_id不能为空")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22435")
    @NotNull(message = "被推荐人的报名ID不能为空")
    private Long referredEnrollmentId;

    @Schema(description = "被推荐人 member_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12587")
    @NotNull(message = "被推荐人 member_id不能为空")
    private Long referredMemberId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32343")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "关系状态（ACTIVE/INVALID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "关系状态（ACTIVE/INVALID）不能为空")
    private String relationStatus;

    @Schema(description = "无效原因（风控拒绝等）", example = "不好")
    private String invalidReason;

    @Schema(description = "推荐链接首次点击时间")
    private LocalDateTime firstClickAt;

    @Schema(description = "推荐关系锁定时间（被推荐人支付定金时）")
    private LocalDateTime referralLockedAt;

}