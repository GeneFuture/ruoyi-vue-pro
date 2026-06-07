package cn.iocoder.yudao.module.mp.convert.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountSimpleRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MpAccountConvert {

    MpAccountConvert INSTANCE = Mappers.getMapper(MpAccountConvert.class);

    MpAccountDO convert(MpAccountCreateReqVO bean);

    MpAccountDO convert(MpAccountUpdateReqVO bean);

    MpAccountRespVO convert(MpAccountDO bean);

    List<MpAccountRespVO> convertList(List<MpAccountDO> list);

    PageResult<MpAccountRespVO> convertPage(PageResult<MpAccountDO> page);

    List<MpAccountSimpleRespVO> convertList02(List<MpAccountDO> list);

}
