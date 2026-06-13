package cn.iocoder.yudao.module.maritime.controller.admin.announcement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 最新动态 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AnnouncementRespVO {

    @Schema(description = "公告ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23520")
    @ExcelProperty("公告ID")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标题")
    private String title;

    @Schema(description = "正文内容（富文本HTML）")
    @ExcelProperty("正文内容（富文本HTML）")
    private String content;

    @Schema(description = "摘要（列表页展示，1-2行）")
    @ExcelProperty("摘要（列表页展示，1-2行）")
    private String summary;

    @Schema(description = "缩略图/封面图URL")
    @ExcelProperty("缩略图/封面图URL")
    private String coverImage;

    @Schema(description = "类型（NEW_COURSE新课/NOTICE公告/ACTIVITY活动/CASE成功案例）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("类型（NEW_COURSE新课/NOTICE公告/ACTIVITY活动/CASE成功案例）")
    private String type;

    @Schema(description = "外链URL（可选，点击后跳转）", example = "https://www.iocoder.cn")
    @ExcelProperty("外链URL（可选，点击后跳转）")
    private String externalUrl;

    @Schema(description = "发布时间（支持定时发布，NULL表示立即发布）")
    @ExcelProperty("发布时间（支持定时发布，NULL表示立即发布）")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶（0否 1是，最多3条同时置顶）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否置顶（0否 1是，最多3条同时置顶）")
    private Boolean isTop;

    @Schema(description = "置顶排序序号（置顶时有效，越小越靠前）")
    @ExcelProperty("置顶排序序号（置顶时有效，越小越靠前）")
    private Integer topOrder;

    @Schema(description = "非置顶优先级权重（越大越靠前）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("非置顶优先级权重（越大越靠前）")
    private Integer sortOrder;

    @Schema(description = "状态（DRAFT草稿/PUBLISHED已发布）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态（DRAFT草稿/PUBLISHED已发布）")
    private String status;

    @Schema(description = "浏览次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "22689")
    @ExcelProperty("浏览次数")
    private Integer viewCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}