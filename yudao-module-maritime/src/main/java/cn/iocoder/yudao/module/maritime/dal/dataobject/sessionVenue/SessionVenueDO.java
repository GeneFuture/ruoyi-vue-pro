package cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 班期场地信息 DO
 *
 * 与班期 1:1（一个班期一条），即「学校信息」：名称、地址、经纬度坐标，
 * 以及学校概况 / 住宿条件 / 教学条件三段富文本（图片由富文本内嵌，不单列图片字段）；
 * 与 maritime_session_fee_config 同属班期附属配置，随班期表单内嵌面板维护
 *
 * @author Gene Ye
 */
@TableName(value = "maritime_session_venue", autoResultMap = true)
@KeySequence("maritime_session_venue_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVenueDO extends BaseDO {

    /**
     * 班期场地ID
     */
    @TableId
    private Long id;
    /**
     * 班期ID（唯一）
     */
    private Long sessionId;
    /**
     * 学校名称
     */
    private String campusName;
    /**
     * 省（新建班期时从学校带入）
     */
    private String province;
    /**
     * 市（新建班期时从学校带入，列表按此筛选）
     */
    private String city;
    /**
     * 区 / 县（新建班期时从学校带入）
     */
    private String district;
    /**
     * 详细地址（省市区之后的部分，如「某某路 1 号」）
     */
    private String address;
    /**
     * 纬度（-90 ~ 90），为空表示未配置坐标
     */
    private BigDecimal latitude;
    /**
     * 经度（-180 ~ 180），为空表示未配置坐标
     */
    private BigDecimal longitude;
    /**
     * 咨询电话
     */
    private String contactPhone;
    /**
     * 学校概况（富文本HTML）
     */
    private String campusIntro;
    /**
     * 住宿条件（富文本HTML，可空）
     */
    private String accommodationIntro;
    /**
     * 教学条件（富文本HTML）
     */
    private String classroomIntro;

}
