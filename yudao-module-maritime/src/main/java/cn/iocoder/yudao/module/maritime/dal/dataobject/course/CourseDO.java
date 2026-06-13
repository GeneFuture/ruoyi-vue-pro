package cn.iocoder.yudao.module.maritime.dal.dataobject.course;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 课程模板 DO
 */
@TableName(value = "maritime_course", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDO extends BaseDO {

    @TableId
    private Long id;

    private String name;

    private String certificateType;

    private String coverImage;

    private String description;

    private String enrollmentConditionText;

    /** 状态（0下架 1上架） */
    private Integer status;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    private Long tenantId;

}
