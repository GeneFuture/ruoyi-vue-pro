package cn.iocoder.yudao.module.maritime.dal.dataobject.memberSubscribe;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@TableName("maritime_member_subscribe")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSubscribeDO extends BaseDO {

    @TableId
    private Long id;

    private Long memberId;

    /** 微信订阅消息模板标题（对应 SocialWxaSubscribeMessageSendReqDTO.templateTitle） */
    private String templateTitle;

    /** 是否已授权订阅（用户授权后为 true；reject 或过期后为 false） */
    private Boolean subscribed;

}
