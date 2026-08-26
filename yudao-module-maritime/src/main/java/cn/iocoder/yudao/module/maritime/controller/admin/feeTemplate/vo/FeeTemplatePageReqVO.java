package cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 费用模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeeTemplatePageReqVO extends PageParam {

    @Schema(description = "模板名称", example = "STCW")
    private String name;

    @Schema(description = "状态（0停用 1启用）", example = "1")
    private Integer status;

    @Schema(description = "是否开启拼团", example = "true")
    private Boolean isGrouponEnabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
