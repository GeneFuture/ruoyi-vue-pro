package cn.iocoder.yudao.module.maritime.dal.dataobject.school;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 学校库 DO
 *
 * 独立的学校资料；新增班期时选择学校并由服务端快照复制到班期场地信息
 * （maritime_session_venue），学校后续修改不影响已创建的班期。
 *
 * @author Gene Ye
 */
@TableName(value = "maritime_school", autoResultMap = true)
@KeySequence("maritime_school_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDO extends BaseDO {

    /**
     * 学校ID
     */
    @TableId
    private Long id;
    /**
     * 学校名称
     */
    private String name;
    /**
     * 省（地图拾取自动填充）
     */
    private String province;
    /**
     * 市（地图拾取自动填充，列表按此筛选）
     */
    private String city;
    /**
     * 区 / 县（地图拾取自动填充）
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
     * 教学条件（富文本HTML）
     */
    private String classroomIntro;
    /**
     * 住宿条件（富文本HTML，可空）
     */
    private String accommodationIntro;
    /**
     * 状态（0开启 1关闭）
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 排序权重（越大越靠前）
     */
    private Integer sortOrder;

}
