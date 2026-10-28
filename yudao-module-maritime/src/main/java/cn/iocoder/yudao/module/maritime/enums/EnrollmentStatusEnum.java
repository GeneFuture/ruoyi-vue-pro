package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 报名状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnrollmentStatusEnum {

    PENDING_DEPOSIT("PENDING_DEPOSIT", "待支付定金"),
    DEPOSITED("DEPOSITED", "已付定金"),
    /**
     * 尾款/全款已付清，等待开班
     */
    PAID_OFF("PAID_OFF", "已付清/待开班"),
    /**
     * 尾款逾期、退费申请等需要人工介入的场景
     */
    MANUAL_PROCESSING("MANUAL_PROCESSING", "人工处理"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String status;
    private final String name;

    public static EnrollmentStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(EnrollmentStatusEnum.class, e -> Objects.equals(status, e.getStatus()));
    }

}
