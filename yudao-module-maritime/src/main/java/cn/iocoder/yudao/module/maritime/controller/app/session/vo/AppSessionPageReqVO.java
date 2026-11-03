package cn.iocoder.yudao.module.maritime.controller.app.session.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "小程序 - 班期分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSessionPageReqVO extends PageParam {

    @Schema(description = "开班月份（yyyy-MM）", example = "2026-09")
    private String month;

    @Schema(description = "城市（模糊匹配，如「青岛」可匹配「青岛市」）", example = "青岛")
    private String city;

    @Schema(description = "证书类型", example = "STCW")
    private String certificateType;

}
