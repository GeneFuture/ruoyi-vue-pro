package cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "小程序 - 创建报名 Request VO")
@Data
public class AppEnrollmentCreateReqVO {

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "姓名不能超过50个字符")
    private String realName;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101199001011234")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$",
             message = "身份证号格式不正确")
    private String idCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "推荐码（可选，来自分享链接）", example = "REF-123-ABCD")
    private String referralCode;

}
