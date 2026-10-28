package cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;

/**
 * 报名管理 DO
 *
 * @author Gene Ye
 */
@TableName(value = "maritime_enrollment", autoResultMap = true)
@KeySequence("maritime_enrollment_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDO extends BaseDO {

    /**
     * 报名ID
     */
    @TableId
    private Long id;
    /**
     * 报名单号（系统唯一，格式：EN+时间戳）
     */
    private String enrollmentNo;
    /**
     * 学员 member_user.id
     */
    private Long memberId;
    /**
     * 班期ID
     */
    private Long sessionId;
    /**
     * 关联拼团记录（可为空）
     */
    private Long grouponRecordId;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 身份证号（AES 加密存储）
     */
    @TableField(typeHandler = EncryptTypeHandler.class)
    private String idCard;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 应缴总金额（元，报名时快照）
     */
    private BigDecimal totalAmount;
    /**
     * 定金金额快照（元，报名时费用配置的定金值）
     */
    private BigDecimal depositAmountSnapshot;
    /**
     * 尾款金额（元，= total_amount - deposit_amount_snapshot）
     */
    private BigDecimal balanceAmount;
    /**
     * 已支付金额（元，含定金+已付尾款）
     */
    private BigDecimal paidAmount;
    /**
     * 尾款截止缴纳日期
     */
    private LocalDate balanceDueDate;
    /**
     * 最后一次尾款催缴提醒日期（用于去重，避免连续多天重复提醒）
     */
    private LocalDate lastBalanceRemindDate;
    /**
     * 报名状态(PENDING_DEPOSIT/DEPOSITED/PAID_OFF/MANUAL_PROCESSING/COMPLETED/CANCELLED)
     */
    private String enrollmentStatus;
    /**
     * 取消原因
     */
    private String cancelReason;


}