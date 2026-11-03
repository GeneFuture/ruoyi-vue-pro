package cn.iocoder.yudao.module.maritime.dal.dataobject.fee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 另行缴费项目
 *
 * 指需要到校报到时缴纳的缴费项目（区别于在线支付的学费与定金），
 * 以 JSON 数组形式存在费用模板与班期费用配置的 extra_fee_items 列上，
 * 例如：[{"name":"教材费","amount":200,"required":true},{"name":"住宿费","amount":800,"required":false}]
 *
 * 放在 dal/dataobject/fee 下：它是持久化的数据结构，模板与班期配置共用，
 * 管理端与小程序端 VO 也直接引用，避免各处各写一份。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtraFeeItem {

    @Schema(description = "项目名称", example = "教材费")
    private String name;

    @Schema(description = "金额（元）", example = "200")
    private BigDecimal amount;

    @Schema(description = "是否必须缴纳：true 必须、false 可选", example = "true")
    private Boolean required;

}
