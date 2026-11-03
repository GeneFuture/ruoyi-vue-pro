package cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 报名记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EnrollmentRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17876")
    private Long id;

    @Schema(description = "报名单号（系统唯一，格式：EN+时间戳）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报名单号")
    private String enrollmentNo;

    @Schema(description = "学员 member_user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18818")
    private Long memberId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4680")
    private Long sessionId;

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

    @Schema(description = "关联拼团记录（可为空）", example = "29350")
    private Long grouponRecordId;

    @Schema(description = "拼团邀请码", example = "GRP-QX1AVE5O")
    @ExcelProperty("拼团邀请码")
    private String grouponInviteCode;

    @Schema(description = "拼团状态", example = "IN_PROGRESS")
    @ExcelProperty("拼团状态")
    private String grouponStatus;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("真实姓名")
    private String realName;

    @Schema(description = "身份证号（脱敏，前4后4中间*）")
    @ExcelProperty("身份证号（脱敏）")
    private String idCardMasked;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "应缴总金额（元，报名时快照）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("应缴总金额（元）")
    private BigDecimal totalAmount;

    @Schema(description = "定金金额快照（元，报名时费用配置的定金值）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定金金额（元）")
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "尾款金额（元，= total_amount - deposit_amount_snapshot）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("尾款金额（元）")
    private BigDecimal balanceAmount;

    @Schema(description = "已支付金额（元，含定金+已付尾款）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已支付金额（元）")
    private BigDecimal paidAmount;

    @Schema(description = "尾款截止缴纳日期")
    @ExcelProperty("尾款截止日期")
    private LocalDate balanceDueDate;

    @Schema(description = "报名状态(PENDING_DEPOSIT/DEPOSITED/PAID_OFF/MANUAL_PROCESSING/COMPLETED/CANCELLED)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("报名状态")
    private String enrollmentStatus;

    @Schema(description = "取消原因", example = "不对")
    @ExcelProperty("取消原因")
    private String cancelReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}