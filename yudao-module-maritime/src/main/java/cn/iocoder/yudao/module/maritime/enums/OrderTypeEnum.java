package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 报名订单类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    DEPOSIT("DEPOSIT", "定金"),
    BALANCE("BALANCE", "尾款"),
    FULL("FULL", "全款");

    private final String type;
    private final String name;

    public static OrderTypeEnum getByType(String type) {
        return EnumUtil.getBy(OrderTypeEnum.class, e -> Objects.equals(type, e.getType()));
    }

}
