package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 佣金状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommissionStatusEnum {

    WAITING_FOR_CLASS("WAITING_FOR_CLASS", "待开课"),
    FROZEN("FROZEN", "已冻结"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"),
    REVIEWING("REVIEWING", "审核中"),
    PENDING_PAYOUT("PENDING_PAYOUT", "待发放"),
    PAYING("PAYING", "发放中"),
    PAID("PAID", "已发放"),
    REJECTED("REJECTED", "已拒绝"),
    FAILED("FAILED", "发放失败"),
    MANUALLY_PAID("MANUALLY_PAID", "人工处理");

    private final String status;
    private final String name;

    public static CommissionStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(CommissionStatusEnum.class, e -> Objects.equals(status, e.getStatus()));
    }

}
