package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 人工处理单类型
 */
@Getter
@AllArgsConstructor
public enum ManualTaskTypeEnum {

    BALANCE_OVERDUE("BALANCE_OVERDUE", "尾款逾期"),
    REFUND_APPLY("REFUND_APPLY", "退费申请"),
    REFUND_AMOUNT_CONFIRM("REFUND_AMOUNT_CONFIRM", "退款金额确认"),
    OTHER("OTHER", "其他");

    private final String type;
    private final String name;

    public static ManualTaskTypeEnum getByType(String type) {
        return EnumUtil.getBy(ManualTaskTypeEnum.class, e -> Objects.equals(type, e.getType()));
    }

}
