package cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 报名管理 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EnrollmentRespVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17876")
    @ExcelProperty("报名ID")
    private Long id;

    @Schema(description = "报名单号（系统唯一，格式：EN+时间戳）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("报名单号（系统唯一，格式：EN+时间戳）")
    private String enrollmentNo;

    @Schema(description = "学员 member_user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18818")
    @ExcelProperty("学员 member_user.id")
    private Long memberId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4680")
    @ExcelProperty("班期ID")
    private Long sessionId;

    @Schema(description = "关联拼团记录（可为空）", example = "29350")
    @ExcelProperty("关联拼团记录（可为空）")
    private Long grouponRecordId;

    @Schema(description = "推荐人 member_user.id", example = "1937")
    @ExcelProperty("推荐人 member_user.id")
    private Long referredByMemberId;

    @Schema(description = "报名时填写的推荐码")
    @ExcelProperty("报名时填写的推荐码")
    private String referralCodeUsed;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("真实姓名")
    private String realName;

    @Schema(description = "身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）")
    private String idCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("手机号")
    private String phone;

    @Schema(description = "应缴总金额（元，报名时快照）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("应缴总金额（元，报名时快照）")
    private BigDecimal totalAmount;

    @Schema(description = "定金金额快照（元，报名时费用配置的定金值）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定金金额快照（元，报名时费用配置的定金值）")
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "尾款金额（元，= total_amount - deposit_amount_snapshot）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("尾款金额（元，= total_amount - deposit_amount_snapshot）")
    private BigDecimal balanceAmount;

    @Schema(description = "已支付金额（元，含定金+已付尾款）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已支付金额（元，含定金+已付尾款）")
    private BigDecimal paidAmount;

    @Schema(description = "尾款截止缴纳日期")
    @ExcelProperty("尾款截止缴纳日期")
    private LocalDate balanceDueDate;

    @Schema(description = "报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)")
    private String enrollmentStatus;

    @Schema(description = "是否已获得推荐权（支付定金后=1）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否已获得推荐权（支付定金后=1）")
    private Boolean referralRightGranted;

    @Schema(description = "取消原因", example = "不对")
    @ExcelProperty("取消原因")
    private String cancelReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}