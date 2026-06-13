package cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 站内消息新增/修改 Request VO")
@Data
public class UserMessageSaveReqVO {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10673")
    private Long id;

    @Schema(description = "接收人 member_user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17069")
    @NotNull(message = "接收人 member_user.id不能为空")
    private Long memberId;

    @Schema(description = "消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）不能为空")
    private String type;

    @Schema(description = "消息标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "消息标题不能为空")
    private String title;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "关联业务ID（enrollment_id/commission_record_id等，可为空）", example = "7454")
    private Long relatedId;

    @Schema(description = "关联业务类型（ENROLLMENT/COMMISSION/GROUPON）", example = "1")
    private String relatedType;

    @Schema(description = "是否已读（0未读 1已读）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否已读（0未读 1已读）不能为空")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

}