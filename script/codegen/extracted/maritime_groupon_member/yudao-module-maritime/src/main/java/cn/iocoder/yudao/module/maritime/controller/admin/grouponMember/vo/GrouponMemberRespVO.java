package cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 拼团成员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GrouponMemberRespVO {

    @Schema(description = "拼团成员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1639")
    @ExcelProperty("拼团成员ID")
    private Long id;

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18827")
    @ExcelProperty("拼团记录ID")
    private Long grouponRecordId;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "969")
    @ExcelProperty("报名ID")
    private Long enrollmentId;

    @Schema(description = "学员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6388")
    @ExcelProperty("学员ID")
    private Long memberId;

    @Schema(description = "加入时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "成员状态（ACTIVE正常/CANCELLED已退出）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("成员状态（ACTIVE正常/CANCELLED已退出）")
    private String memberStatus;

    @Schema(description = "定金支付时间（用于24h拼团有效期检查）")
    @ExcelProperty("定金支付时间（用于24h拼团有效期检查）")
    private LocalDateTime depositPaidAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}