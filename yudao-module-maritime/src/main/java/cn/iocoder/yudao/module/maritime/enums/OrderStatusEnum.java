package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 报名订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING("PENDING", "待支付"),
    PAID("PAID", "已支付"),
    CLOSED("CLOSED", "已关闭"),
    REFUNDED("REFUNDED", "已退款");

    private final String status;
    private final String name;

    public static OrderStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(OrderStatusEnum.class, e -> Objects.equals(status, e.getStatus()));
    }

}
