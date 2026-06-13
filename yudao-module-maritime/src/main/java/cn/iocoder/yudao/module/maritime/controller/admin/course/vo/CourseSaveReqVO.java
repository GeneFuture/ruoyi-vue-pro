package cn.iocoder.yudao.module.maritime.controller.admin.course.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 课程新增/修改 Request VO")
@Data
public class CourseSaveReqVO {

    @Schema(description = "课程ID（修改时必传）", example = "1")
    private Long id;

    @Schema(description = "课程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW基本安全培训")
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100)
    private String name;

    @Schema(description = "证书类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "STCW")
    @NotBlank(message = "证书类型不能为空")
    @Size(max = 50)
    private String certificateType;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverImage;

    @Schema(description = "课程详情（富文本HTML）")
    private String description;

    @Schema(description = "报名条件说明文本")
    private String enrollmentConditionText;

    @Schema(description = "状态（0下架 1上架）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值不合法")
    @Max(value = 1, message = "状态值不合法")
    private Integer status;

    @Schema(description = "排序权重（越大越靠前）", example = "0")
    private Integer sortOrder;

}
