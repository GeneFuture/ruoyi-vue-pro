package cn.iocoder.yudao.module.maritime.dal.dataobject.studentInfo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学员信息 DO
 *
 * 每个会员一条，用于报名时自动带出姓名 / 身份证 / 手机号。
 * 身份证号与报名记录一样使用 AES 加密存储。
 *
 * @author Gene Ye
 */
@TableName(value = "maritime_student_info", autoResultMap = true)
@KeySequence("maritime_student_info_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfoDO extends BaseDO {

    /**
     * 学员信息ID
     */
    @TableId
    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

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

}
