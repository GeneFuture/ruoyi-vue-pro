package cn.iocoder.yudao.module.maritime.controller.admin.referralRelation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 推荐关系 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ReferralRelationRespVO {

    @Schema(description = "推荐关系ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "14844")
    @ExcelProperty("推荐关系ID")
    private Long id;

    @Schema(description = "推荐人 member_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21268")
    @ExcelProperty("推荐人 member_id")
    private Long referrerMemberId;

    @Schema(description = "被推荐人的报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "22435")
    @ExcelProperty("被推荐人的报名ID")
    private Long referredEnrollmentId;

    @Schema(description = "被推荐人 member_id", requiredMode = Schema.RequiredMode.REQUIRED, example = "12587")
    @ExcelProperty("被推荐人 member_id")
    private Long referredMemberId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32343")
    @ExcelProperty("班期ID")
    private Long sessionId;

    @Schema(description = "关系状态（ACTIVE/INVALID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("关系状态（ACTIVE/INVALID）")
    private String relationStatus;

    @Schema(description = "无效原因（风控拒绝等）", example = "不好")
    @ExcelProperty("无效原因（风控拒绝等）")
    private String invalidReason;

    @Schema(description = "推荐链接首次点击时间")
    @ExcelProperty("推荐链接首次点击时间")
    private LocalDateTime firstClickAt;

    @Schema(description = "推荐关系锁定时间（被推荐人支付定金时）")
    @ExcelProperty("推荐关系锁定时间（被推荐人支付定金时）")
    private LocalDateTime referralLockedAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}