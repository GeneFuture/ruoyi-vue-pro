package cn.iocoder.yudao.module.maritime.dal.dataobject.session;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 班期管理 DO
 *
 * @author Gene Ye
 */
@TableName("maritime_course_session")
@KeySequence("maritime_course_session_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseSessionDO extends BaseDO {

    /**
     * 班期ID
     */
    @TableId
    private Long id;
    /**
     * 课程ID
     */
    private Long courseId;
    /**
     * 班期编号（如：2026-06-SH-001）
     */
    private String sessionCode;
    /**
     * 班级名称（可选，如：A班）
     */
    private String sessionName;
    /**
     * 本期上课地点
     */
    private String location;
    /**
     * 本期课程天数
     */
    private Integer durationDays;
    /**
     * 本期讲师信息
     */
    private String instructorInfo;
    /**
     * 开班日期
     */
    private LocalDate startDate;
    /**
     * 结束日期
     */
    private LocalDate endDate;
    /**
     * 报名截止日期
     */
    private LocalDate enrollmentDeadline;
    /**
     * 最大招生人数
     */
    private Integer maxStudents;
    /**
     * 已报名人数（缓存值，以 enrollment 表实际计数为准）
     */
    private Integer enrolledCount;
    /**
     * 班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）
     */
    private String sessionStatus;
    /**
     * 班期备注
     */
    private String remark;


}