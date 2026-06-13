package cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 最新动态分页 Request VO")
@Data
public class AnnouncementPageReqVO extends PageParam {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "正文内容（富文本HTML）")
    private String content;

    @Schema(description = "摘要（列表页展示，1-2行）")
    private String summary;

    @Schema(description = "缩略图/封面图URL")
    private String coverImage;

    @Schema(description = "类型（NEW_COURSE新课/NOTICE公告/ACTIVITY活动/CASE成功案例）", example = "1")
    private String type;

    @Schema(description = "外链URL（可选，点击后跳转）", example = "https://www.iocoder.cn")
    private String externalUrl;

    @Schema(description = "发布时间（支持定时发布，NULL表示立即发布）")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] publishTime;

    @Schema(description = "是否置顶（0否 1是，最多3条同时置顶）")
    private Boolean isTop;

    @Schema(description = "置顶排序序号（置顶时有效，越小越靠前）")
    private Integer topOrder;

    @Schema(description = "非置顶优先级权重（越大越靠前）")
    private Integer sortOrder;

    @Schema(description = "状态（DRAFT草稿/PUBLISHED已发布）", example = "2")
    private String status;

    @Schema(description = "浏览次数", example = "22689")
    private Integer viewCount;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}