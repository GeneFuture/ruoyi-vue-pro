package cn.iocoder.yudao.module.maritime.controller.app.studentInfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 学员信息 Response VO")
@Data
public class AppStudentInfoRespVO {

    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @Schema(description = "身份证号", example = "110101199001011234")
    private String idCard;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

}
