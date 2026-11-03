package cn.iocoder.yudao.module.maritime.enums;

import cn.hutool.core.util.EnumUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 人工处理单状态
 */
@Getter
@AllArgsConstructor
public enum ManualTaskStatusEnum {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    DONE("DONE", "已完成"),
    CANCELLED("CANCELLED", "已取消");

    private final String status;
    private final String name;

    public static ManualTaskStatusEnum getByStatus(String status) {
        return EnumUtil.getBy(ManualTaskStatusEnum.class, e -> Objects.equals(status, e.getStatus()));
    }

}
