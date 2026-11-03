package cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 人工处理单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ManualTaskPageReqVO extends PageParam {

    @Schema(description = "报名ID", example = "1")
    private Long enrollmentId;

    @Schema(description = "会员ID", example = "247")
    private Long memberId;

    @Schema(description = "学员姓名", example = "张三")
    private String realName;

    @Schema(description = "学员手机号", example = "13800000000")
    private String phone;

    @Schema(description = "报名ID集合（内部筛选使用）", hidden = true)
    private List<Long> enrollmentIds;

    @Schema(description = "任务类型", example = "BALANCE_OVERDUE")
    private String taskType;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
