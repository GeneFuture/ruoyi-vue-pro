package cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 报名管理分页 Request VO")
@Data
public class EnrollmentPageReqVO extends PageParam {

    @Schema(description = "报名单号（系统唯一，格式：EN+时间戳）")
    private String enrollmentNo;

    @Schema(description = "学员 member_user.id", example = "18818")
    private Long memberId;

    @Schema(description = "班期ID", example = "4680")
    private Long sessionId;

    @Schema(description = "关联拼团记录（可为空）", example = "29350")
    private Long grouponRecordId;

    @Schema(description = "推荐人 member_user.id", example = "1937")
    private Long referredByMemberId;

    @Schema(description = "报名时填写的推荐码")
    private String referralCodeUsed;

    @Schema(description = "真实姓名", example = "李四")
    private String realName;

    @Schema(description = "身份证号（加密存储，EnrollmentDO 需配置 EncryptTypeHandler）")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "应缴总金额（元，报名时快照）")
    private BigDecimal totalAmount;

    @Schema(description = "定金金额快照（元，报名时费用配置的定金值）")
    private BigDecimal depositAmountSnapshot;

    @Schema(description = "尾款金额（元，= total_amount - deposit_amount_snapshot）")
    private BigDecimal balanceAmount;

    @Schema(description = "已支付金额（元，含定金+已付尾款）")
    private BigDecimal paidAmount;

    @Schema(description = "尾款截止缴纳日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] balanceDueDate;

    @Schema(description = "报名状态(PENDING_DEPOSIT/DEPOSITED/PENDING_BALANCE/IN_PROGRESS/COMPLETED/CANCELLED/REFUNDING)", example = "1")
    private String enrollmentStatus;

    @Schema(description = "是否已获得推荐权（支付定金后=1）")
    private Boolean referralRightGranted;

    @Schema(description = "取消原因", example = "不对")
    private String cancelReason;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}