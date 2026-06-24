package cn.iocoder.yudao.module.maritime.dal.dataobject.riskEvent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName("maritime_risk_event")
@KeySequence("maritime_risk_event_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEventDO extends BaseDO {

    @TableId
    private Long id;

    private String eventType;

    private Long memberId;

    private String detail;

    private Boolean isReviewed;

    private Long reviewerId;

    private String reviewResult;

    private String reviewRemark;

    private Long tenantId;

}
