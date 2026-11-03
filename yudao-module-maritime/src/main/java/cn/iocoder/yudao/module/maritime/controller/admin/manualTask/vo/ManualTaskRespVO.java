package cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 人工处理单 Response VO")
@Data
public class ManualTaskRespVO {

    @Schema(description = "人工处理单ID", example = "1")
    private Long id;

    @Schema(description = "人工处理单号", example = "MT202610270001")
    private String taskNo;

    @Schema(description = "报名ID", example = "1")
    private Long enrollmentId;

    @Schema(description = "会员ID", example = "247")
    private Long memberId;

    @Schema(description = "报名单号", example = "EN202611020001")
    private String enrollmentNo;

    @Schema(description = "学员姓名", example = "张三")
    private String realName;

    @Schema(description = "学员手机号", example = "13800000000")
    private String phone;

    @Schema(description = "课程名称", example = "基本安全培训")
    private String courseName;

    @Schema(description = "班期编号", example = "STCW-202607-001")
    private String sessionCode;

    @Schema(description = "班级名称", example = "A班")
    private String sessionName;

    @Schema(description = "退费金额（元）", example = "500.00")
    private BigDecimal refundAmount;

    @Schema(description = "退费申请状态", example = "PENDING")
    private String applyStatus;

    @Schema(description = "关联支付订单ID", example = "2")
    private Long orderId;

    @Schema(description = "来源类型", example = "REFUND")
    private String sourceType;

    @Schema(description = "来源记录ID", example = "1")
    private Long sourceId;

    @Schema(description = "任务类型", example = "REFUND_APPLY")
    private String taskType;

    @Schema(description = "状态", example = "PENDING")
    private String status;

    @Schema(description = "优先级", example = "0")
    private Integer priority;

    @Schema(description = "进入人工的原因")
    private String reason;

    @Schema(description = "处理人用户ID", example = "1")
    private Long handlerId;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
