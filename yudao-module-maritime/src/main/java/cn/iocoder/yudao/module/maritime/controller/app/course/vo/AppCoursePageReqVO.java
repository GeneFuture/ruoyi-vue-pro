package cn.iocoder.yudao.module.maritime.controller.app.course.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "小程序 - 课程列表查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCoursePageReqVO extends PageParam {

    @Schema(description = "课程名称关键词（模糊匹配）", example = "三管轮")
    @Size(max = 50, message = "搜索关键词不能超过 50 个字符")
    private String keyword;

    @Schema(description = "证书类型", example = "STCW")
    private String certificateType;

    @Schema(description = "城市（模糊匹配，如「青岛」可匹配「青岛市」）", example = "青岛")
    private String city;

    @Schema(description = "开班月份（格式：2026-06）", example = "2026-06")
    private String sessionMonth;

}
