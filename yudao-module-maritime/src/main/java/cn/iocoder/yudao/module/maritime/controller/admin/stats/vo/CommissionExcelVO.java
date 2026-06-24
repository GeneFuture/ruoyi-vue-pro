package cn.iocoder.yudao.module.maritime.controller.admin.stats.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionExcelVO {

    @ExcelProperty("序号")
    @ColumnWidth(8)
    private Integer index;

    @ExcelProperty("推荐人姓名（脱敏）")
    @ColumnWidth(18)
    private String referrerName;

    @ExcelProperty("推荐人手机（脱敏）")
    @ColumnWidth(16)
    private String referrerPhone;

    @ExcelProperty("被推荐人姓名（脱敏）")
    @ColumnWidth(20)
    private String referredName;

    @ExcelProperty("班期编号")
    @ColumnWidth(20)
    private String sessionCode;

    @ExcelProperty("佣金金额（元）")
    @ColumnWidth(15)
    private BigDecimal commissionAmount;

    @ExcelProperty("佣金状态")
    @ColumnWidth(16)
    private String commissionStatus;

    @ExcelProperty("触发时间")
    @ColumnWidth(20)
    private String triggerTime;

    @ExcelProperty("发放时间")
    @ColumnWidth(20)
    private String payoutTime;

    @ExcelProperty("付款单号")
    @ColumnWidth(24)
    private String payTransferId;

}
