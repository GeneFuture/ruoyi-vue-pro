package cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 拼团成员 Response VO")
@Data
@ExcelIgnoreUnannotated
public class GrouponMemberRespVO {

    @Schema(description = "拼团成员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1639")
    private Long id;

    @Schema(description = "拼团记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18827")
    private Long grouponRecordId;

    @Schema(description = "拼团邀请码", example = "GRP-QX1AVE5O")
    @ExcelProperty("拼团邀请码")
    private String inviteCode;

    @Schema(description = "拼团状态", example = "IN_PROGRESS")
    @ExcelProperty("拼团状态")
    private String grouponStatus;

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "969")
    private Long enrollmentId;

    @Schema(description = "报名单号", example = "EN202611020001")
    @ExcelProperty("报名单号")
    private String enrollmentNo;

    @Schema(description = "学员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6388")
    private Long memberId;

    @Schema(description = "学员姓名", example = "张三")
    @ExcelProperty("学员姓名")
    private String realName;

    @Schema(description = "学员手机号", example = "13800000000")
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "课程名称", example = "基本安全培训")
    @ExcelProperty("课程名称")
    private String courseName;

    @Schema(description = "班期编号", example = "STCW-202607-001")
    @ExcelProperty("班期编号")
    private String sessionCode;

    @Schema(description = "班级名称", example = "A班")
    @ExcelProperty("班级名称")
    private String sessionName;

    @Schema(description = "开班日期", example = "2026-11-02")
    @ExcelProperty("开班日期")
    private LocalDate sessionStartDate;

    @Schema(description = "加入时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("加入时间")
    private LocalDateTime joinTime;

    @Schema(description = "成员状态（ACTIVE正常/CANCELLED已退出）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("成员状态")
    private String memberStatus;

    @Schema(description = "定金支付时间（用于24h拼团有效期检查）")
    @ExcelProperty("定金支付时间")
    private LocalDateTime depositPaidAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}