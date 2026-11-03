package cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理后台 - 报名记录更新 Request VO
 *
 * 人工管理只允许修改报名状态、尾款截止日期和取消原因，
 * 不允许在这里改金额、学员、班期等报名快照字段。
 *
 * @author Gene Ye
 */
@Schema(description = "管理后台 - 报名记录更新 Request VO")
@Data
public class EnrollmentUpdateReqVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17876")
    @NotNull(message = "报名ID不能为空")
    private Long id;

    @Schema(description = "报名状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DEPOSITED")
    @NotBlank(message = "报名状态不能为空")
    private String enrollmentStatus;

    @Schema(description = "尾款截止缴纳日期", example = "2026-11-01")
    private LocalDate balanceDueDate;

    @Schema(description = "取消原因", example = "学员个人原因")
    @Size(max = 500, message = "取消原因不能超过 500 个字符")
    private String cancelReason;

}
