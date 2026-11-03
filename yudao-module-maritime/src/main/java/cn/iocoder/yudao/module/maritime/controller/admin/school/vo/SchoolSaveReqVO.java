package cn.iocoder.yudao.module.maritime.controller.admin.school.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 学校库新增/修改 Request VO")
@Data
public class SchoolSaveReqVO {

    @Schema(description = "学校ID", example = "1")
    private Long id;

    @Schema(description = "学校名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "青岛船员培训基地")
    @NotEmpty(message = "学校名称不能为空")
    @Size(max = 100, message = "学校名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "省", example = "山东省")
    @Size(max = 50, message = "省长度不能超过 50 个字符")
    private String province;

    @Schema(description = "市", example = "青岛市")
    @Size(max = 50, message = "市长度不能超过 50 个字符")
    private String city;

    @Schema(description = "区 / 县", example = "市北区")
    @Size(max = 50, message = "区县长度不能超过 50 个字符")
    private String district;

    @Schema(description = "详细地址（省市区之后的部分）", example = "某某路 1 号")
    @Size(max = 255, message = "详细地址长度不能超过 255 个字符")
    private String address;

    @Schema(description = "纬度（-90 ~ 90），为空表示未配置坐标", example = "36.067117")
    @DecimalMin(value = "-90", message = "纬度不能小于 -90")
    @DecimalMax(value = "90", message = "纬度不能大于 90")
    private BigDecimal latitude;

    @Schema(description = "经度（-180 ~ 180），为空表示未配置坐标", example = "120.382665")
    @DecimalMin(value = "-180", message = "经度不能小于 -180")
    @DecimalMax(value = "180", message = "经度不能大于 180")
    private BigDecimal longitude;

    @Schema(description = "咨询电话", example = "0532-88888888")
    @Size(max = 50, message = "咨询电话长度不能超过 50 个字符")
    private String contactPhone;

    @Schema(description = "学校概况（富文本HTML）")
    private String campusIntro;

    @Schema(description = "教学条件（富文本HTML）")
    private String classroomIntro;

    @Schema(description = "住宿条件（富文本HTML，可空）")
    private String accommodationIntro;

    @Schema(description = "状态（0开启 1关闭）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "排序权重（越大越靠前）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序权重不能为空")
    private Integer sortOrder;

}
