package cn.iocoder.yudao.module.maritime.controller.admin.stats.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EnrollmentExcelVO {

    @ExcelProperty("序号")
    @ColumnWidth(8)
    private Integer index;

    @ExcelProperty("姓名（脱敏）")
    @ColumnWidth(14)
    private String realName;

    @ExcelProperty("身份证号（脱敏）")
    @ColumnWidth(22)
    private String idCard;

    @ExcelProperty("手机号（脱敏）")
    @ColumnWidth(16)
    private String phone;

    @ExcelProperty("班期编号")
    @ColumnWidth(20)
    private String sessionCode;

    @ExcelProperty("报名时间")
    @ColumnWidth(20)
    private String createTime;

    @ExcelProperty("报名状态")
    @ColumnWidth(16)
    private String enrollmentStatus;

    @ExcelProperty("定金金额（元）")
    @ColumnWidth(15)
    private BigDecimal depositAmount;

    @ExcelProperty("尾款金额（元）")
    @ColumnWidth(15)
    private BigDecimal balanceAmount;

    @ExcelProperty("是否有推荐人")
    @ColumnWidth(14)
    private String hasReferrer;

    @ExcelProperty("推荐人姓名（脱敏）")
    @ColumnWidth(18)
    private String referrerName;

}
