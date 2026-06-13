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
    PENDING_BALANCE("PENDING_BALANCE", "待付尾款"),
    IN_PROGRESS("IN_PROGRESS", "上课中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中");

    private final String status;
    private final String name;

    public static EnrollmentStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(EnrollmentStatusEnum.class, e -> Objects.equals(status, e.getStatus()));
    }

}
