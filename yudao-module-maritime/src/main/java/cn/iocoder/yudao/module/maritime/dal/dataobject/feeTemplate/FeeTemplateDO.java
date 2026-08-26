package cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 费用模板 DO
 *
 * 独立的可复用定价方案；新增班期时选择模板并快照复制到班期费用配置，
 * 模板后续修改不影响已创建的班期。
 *
 * @author Gene Ye
 */
@TableName(value = "maritime_fee_template", autoResultMap = true)
@KeySequence("maritime_fee_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeTemplateDO extends BaseDO {

    /**
     * 模板ID
     */
    @TableId
    private Long id;
    /**
     * 模板名称（如：STCW 标准收费）
     */
    private String name;
    /**
     * 学费总额（元）
     */
    private BigDecimal tuitionAmount;
    /**
     * 学费说明（{"理论课":"1000","实操":"800"}）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> tuitionDescription;
    /**
     * 定金金额（元）
     */
    private BigDecimal depositAmount;
    /**
     * 是否开启拼团
     */
    private Boolean isGrouponEnabled;
    /**
     * 拼团优惠减免金额（元）
     */
    private BigDecimal grouponDiscountAmount;
    /**
     * 拼团所需人数
     */
    private Integer grouponRequiredCount;
    /**
     * 拼团有效时间（小时）
     */
    private Integer grouponExpireHours;
    /**
     * 拼团失败降级优惠金额（元，单人可享）
     */
    private BigDecimal grouponFailDiscountAmount;
    /**
     * 推荐佣金金额（元，每成功推荐一人）
     */
    private BigDecimal referralCommissionAmount;
    /**
     * 是否开启推荐佣金
     */
    private Boolean isReferralCommissionEnabled;
    /**
     * 定金是否可退
     */
    private Boolean isDepositRefundable;
    /**
     * 尾款截止：开班前N天
     */
    private Integer balanceDueDaysBeforeStart;
    /**
     * 退款政策说明文本
     */
    private String refundPolicyText;
    /**
     * 状态（0停用 1启用）
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
