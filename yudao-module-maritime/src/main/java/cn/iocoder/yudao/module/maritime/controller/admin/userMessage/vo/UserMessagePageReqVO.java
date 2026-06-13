package cn.iocoder.yudao.module.maritime.controller.admin.userMessage.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 站内消息分页 Request VO")
@Data
public class UserMessagePageReqVO extends PageParam {

    @Schema(description = "接收人 member_user.id", example = "17069")
    private Long memberId;

    @Schema(description = "消息类型（ORDER=订单类/GROUPON=拼团类/COMMISSION=佣金类/SYSTEM=系统公告）", example = "1")
    private String type;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "关联业务ID（enrollment_id/commission_record_id等，可为空）", example = "7454")
    private Long relatedId;

    @Schema(description = "关联业务类型（ENROLLMENT/COMMISSION/GROUPON）", example = "1")
    private String relatedType;

    @Schema(description = "是否已读（0未读 1已读）")
    private Boolean isRead;

    @Schema(description = "阅读时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] readTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}