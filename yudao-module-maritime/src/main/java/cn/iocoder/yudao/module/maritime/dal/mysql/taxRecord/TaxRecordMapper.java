package cn.iocoder.yudao.module.maritime.dal.mysql.taxRecord;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.taxRecord.TaxRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 税务代扣月度记录 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface TaxRecordMapper extends BaseMapperX<TaxRecordDO> {

    default TaxRecordDO selectByMemberAndMonth(Long memberId, String taxMonth) {
        return selectOne(new LambdaQueryWrapperX<TaxRecordDO>()
                .eq(TaxRecordDO::getMemberId, memberId)
                .eq(TaxRecordDO::getTaxMonth, taxMonth));
    }

    /** 乐观锁累加月收入和税额 */
    @Update("UPDATE maritime_tax_record SET month_income = month_income + #{addIncome}, " +
            "tax_paid = tax_paid + #{addTax}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{expectedVersion}")
    int updateIncrease(@Param("id") Long id,
                       @Param("addIncome") BigDecimal addIncome,
                       @Param("addTax") BigDecimal addTax,
                       @Param("expectedVersion") Integer expectedVersion);

}
