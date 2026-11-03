package cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 班期场地信息（学校信息）新增/修改 Request VO")
@Data
public class SessionVenueSaveReqVO {

    @Schema(description = "班期场地ID", example = "1024")
    private Long id;

    @Schema(description = "班期ID（唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5994")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "学校名称", example = "青岛船员培训基地")
    @Size(max = 100, message = "学校名称长度不能超过 100 个字符")
    private String campusName;

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

    @Schema(description = "住宿条件（富文本HTML，可空）")
    private String accommodationIntro;

    @Schema(description = "教学条件（富文本HTML）")
    private String classroomIntro;

}
