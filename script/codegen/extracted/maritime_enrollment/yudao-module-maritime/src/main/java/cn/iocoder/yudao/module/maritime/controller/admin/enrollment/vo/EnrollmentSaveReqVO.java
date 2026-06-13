package cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 报名管理新增/修改 Request VO")
@Data
public class EnrollmentSaveReqVO {

    @Schema(description = "报名ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "17876")
    private Long id;

    @Schema(description = "报名单号（系统唯一，格式：EN+时间戳）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "报名单号（系统唯一，格式：EN+时间戳）不能为空")
    private String enrollmentNo;

    @Schema(description = "学员 member_user.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "18818")
    @NotNull(message = "学员 member_user.id不能为空")
    private Long memberId;

    @Schema(description = "班期ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4680")
    @NotNull(message = "班期ID不能为空")
    private Long sessionId;

    @Schema(description = "关联拼团记录（可为空）", example = "29350")
    private Long grouponRecordId;

    @Schema(description = "推荐人 member_user.id", example = "1937")
    private Long referredByMemberId;

    @Schema(description = "报名时填写的推荐码")
    private String referralCodeUsed;

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）不能为空")
    private String idCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "手机号不能为空")
    private String phone;

    @Schema(description = "应缴总金额（元，报名时快照）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应缴总金额（元，报名时快照）不能为空")
    private BigDecimal totalAmount;

    @Schema(description = "定金金额快照（元，报名时费用配置的定金值）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定金金额快照（元，报名时费用配置的定金值）不能为空")
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "尾款金额（元，= total_amount - deposit_amount_snapshot）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "尾款金额（元，= total_amount - deposit_amount_snapshot）不能为空")
    private BigDecimal balanceAmount;

    @Schema(description = "已支付金额（元，含定金+已付尾款）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已支付金额（元，含定金+已付尾款）不能为空")
    private BigDecimal paidAmount;

    @Schema(description = "尾款截止缴纳日期")
    private LocalDate balanceDueDate;

    @Schema(description = "报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)不能为空")
    private String enrollmentStatus;

    @Schema(description = "是否已获得推荐权（支付定金后=1）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否已获得推荐权（支付定金后=1）不能为空")
    private Boolean referralRightGranted;

    @Schema(description = "取消原因", example = "不对")
    private String cancelReason;

}