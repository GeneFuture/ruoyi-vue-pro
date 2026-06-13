package cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 拼团成员新增/修改 Request VO")
@Data
public class GrouponMemberSaveReqVO {

    @Schema(description = "拼团成员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1639")
    private Long id;

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18827")
    @NotNull(message = "拼团记录ID不能为空")
    private Long grouponRecordId;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "969")
    @NotNull(message = "报名ID不能为空")
    private Long enrollmentId;

    @Schema(description = "学员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6388")
    @NotNull(message = "学员ID不能为空")
    private Long memberId;

    @Schema(description = "加入时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "加入时间不能为空")
    private LocalDateTime joinTime;

    @Schema(description = "成员状态（ACTIVE正常/CANCELLED已退出）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "成员状态（ACTIVE正常/CANCELLED已退出）不能为空")
    private String memberStatus;

    @Schema(description = "定金支付时间（用于24h拼团有效期检查）")
    private LocalDateTime depositPaidAt;

}