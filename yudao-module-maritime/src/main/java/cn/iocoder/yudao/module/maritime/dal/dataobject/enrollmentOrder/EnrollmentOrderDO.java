package cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 报名订单 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_enrollment_order")
@KeySequence("maritime_enrollment_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentOrderDO extends BaseDO {

    /**
     * 订单ID
     */
    @TableId
    private Long id;
    /**
     * 报名ID
     */
    private Long enrollmentId;
    /**
     * 订单号（系统生成，即 PayOrderCreateReqDTO.merchantOrderId）
     */
    private String orderNo;
    /**
     * 订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）
     */
    private String orderType;
    /**
     * 应付金额（元）
     */
    private BigDecimal amount;
    /**
     * ruoyi pay模块订单ID
     */
    private Long payOrderId;
    /**
     * 支付渠道（wx_lite等）
     */
    private String payChannel;
    /**
     * 订单状态（PENDING/PAID/CLOSED/REFUNDED）
     */
    private String orderStatus;
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    /**
     * 订单过期时间
     */
    private LocalDateTime expireTime;


}