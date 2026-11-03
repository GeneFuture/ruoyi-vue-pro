package cn.iocoder.yudao.module.maritime.controller.admin.school.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学校库 Response VO")
@Data
public class SchoolRespVO {

    @Schema(description = "学校ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "青岛船员培训基地")
    private String name;

    @Schema(description = "省", example = "山东省")
    private String province;

    @Schema(description = "市", example = "青岛市")
    private String city;

    @Schema(description = "区 / 县", example = "市北区")
    private String district;

    @Schema(description = "详细地址（省市区之后的部分）", example = "某某路 1 号")
    private String address;

    @Schema(description = "纬度（-90 ~ 90），为空表示未配置坐标", example = "36.067117")
    private BigDecimal latitude;

    @Schema(description = "经度（-180 ~ 180），为空表示未配置坐标", example = "120.382665")
    private BigDecimal longitude;

    @Schema(description = "咨询电话", example = "0532-88888888")
    private String contactPhone;

    @Schema(description = "学校概况（富文本HTML）")
    private String campusIntro;

    @Schema(description = "教学条件（富文本HTML）")
    private String classroomIntro;

    @Schema(description = "住宿条件（富文本HTML，可空）")
    private String accommodationIntro;

    @Schema(description = "状态（0开启 1关闭）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "排序权重（越大越靠前）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer sortOrder;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
