package cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 拼团成员 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_groupon_member")
@KeySequence("maritime_groupon_member_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrouponMemberDO extends BaseDO {

    /**
     * 拼团成员ID
     */
    @TableId
    private Long id;
    /**
     * 拼团记录ID
     */
    private Long grouponRecordId;
    /**
     * 报名ID
     */
    private Long enrollmentId;
    /**
     * 学员ID
     */
    private Long memberId;
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    /**
     * 成员状态（ACTIVE正常/CANCELLED已退出）
     */
    private String memberStatus;
    /**
     * 定金支付时间（用于24h拼团有效期检查）
     */
    private LocalDateTime depositPaidAt;


}