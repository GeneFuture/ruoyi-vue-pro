package cn.iocoder.yudao.module.statistics.convert.pay;

import cn.iocoder.yudao.module.statistics.controller.admin.pay.vo.PaySummaryRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;

/**
 * 支付统计 Convert
 *
 * @author owen
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayStatisticsConvert {

    PayStatisticsConvert INSTANCE = Mappers.getMapper(PayStatisticsConvert.class);

    PaySummaryRespVO convert(Integer rechargePrice);

}
