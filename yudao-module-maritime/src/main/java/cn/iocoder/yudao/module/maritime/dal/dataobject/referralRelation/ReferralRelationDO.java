package cn.iocoder.yudao.module.maritime.dal.dataobject.referralRelation;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 推荐关系 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_referral_relation")
@KeySequence("maritime_referral_relation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralRelationDO extends BaseDO {

    /**
     * 推荐关系ID
     */
    @TableId
    private Long id;
    /**
     * 推荐人 member_id
     */
    private Long referrerMemberId;
    /**
     * 被推荐人的报名ID
     */
    private Long referredEnrollmentId;
    /**
     * 被推荐人 member_id
     */
    private Long referredMemberId;
    /**
     * 班期ID
     */
    private Long sessionId;
    /**
     * 关系状态（ACTIVE/INVALID）
     */
    private String relationStatus;
    /**
     * 无效原因（风控拒绝等）
     */
    private String invalidReason;
    /**
     * 推荐链接首次点击时间
     */
    private LocalDateTime firstClickAt;
    /**
     * 推荐关系锁定时间（被推荐人支付定金时）
     */
    private LocalDateTime referralLockedAt;


}