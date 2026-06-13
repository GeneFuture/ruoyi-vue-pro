package cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 站内消息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserMessageRespVO {

    @Schema(description = "消息ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10673")
    @ExcelProperty("消息ID")
    private Long id;

    @Schema(description = "接收人 member_user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17069")
    @ExcelProperty("接收人 member_user.id")
    private Long memberId;

    @Schema(description = "消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）")
    private String type;

    @Schema(description = "消息标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("消息标题")
    private String title;

    @Schema(description = "消息正文")
    @ExcelProperty("消息正文")
    private String content;

    @Schema(description = "关联业务ID（enrollment_id/commission_record_id等，可为空）", example = "7454")
    @ExcelProperty("关联业务ID（enrollment_id/commission_record_id等，可为空）")
    private Long relatedId;

    @Schema(description = "关联业务类型（ENROLLMENT/COMMISSION/GROUPON）", example = "1")
    @ExcelProperty("关联业务类型（ENROLLMENT/COMMISSION/GROUPON）")
    private String relatedType;

    @Schema(description = "是否已读（0未读 1已读）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否已读（0未读 1已读）")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    @ExcelProperty("阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}