package cn.iocoder.yudao.module.maritime.controller.admin.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 课程信息 Response VO")
@Data
public class CourseRespVO {

    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW基本安全培训")
    private String name;

    @Schema(description = "证书类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW")
    private String certificateType;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "课程详情（富文本HTML）")
    private String description;

    @Schema(description = "报名条件说明文本")
    private String enrollmentConditionText;

    @Schema(description = "状态（0下架 1上架）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "排序权重", example = "0")
    private Integer sortOrder;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
