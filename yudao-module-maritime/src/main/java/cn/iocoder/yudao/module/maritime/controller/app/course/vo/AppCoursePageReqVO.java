package cn.iocoder.yudao.module.maritime.controller.app.course.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "小程序 - 课程列表查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCoursePageReqVO extends PageParam {

    @Schema(description = "证书类型", example = "STCW")
    private String certificateType;

    @Schema(description = "上课地点（模糊匹配）", example = "上海")
    private String location;

    @Schema(description = "开班月份（格式：2026-06）", example = "2026-06")
    private String sessionMonth;

}
