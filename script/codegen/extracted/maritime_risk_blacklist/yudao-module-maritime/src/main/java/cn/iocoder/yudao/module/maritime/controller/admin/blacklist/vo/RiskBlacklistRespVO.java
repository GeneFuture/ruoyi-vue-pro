package cn.iocoder.yudao.module.maritime.controller.admin.blacklist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 风控黑名单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RiskBlacklistRespVO {

    @Schema(description = "黑名单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "970")
    @ExcelProperty("黑名单ID")
    private Long id;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "加入黑名单原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不好")
    @ExcelProperty("加入黑名单原因")
    private String reason;

    @Schema(description = "操作人（管理员）", example = "2478")
    @ExcelProperty("操作人（管理员）")
    private Long operatorId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}