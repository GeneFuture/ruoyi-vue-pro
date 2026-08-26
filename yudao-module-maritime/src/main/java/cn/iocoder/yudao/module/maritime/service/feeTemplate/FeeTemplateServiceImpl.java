package cn.iocoder.yudao.module.maritime.service.feeTemplate;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.FeeTemplatePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.FeeTemplateSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.feeTemplate.FeeTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 费用模板 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class FeeTemplateServiceImpl implements FeeTemplateService {

    @Resource
    private FeeTemplateMapper feeTemplateMapper;

    @Override
    public Long createFeeTemplate(FeeTemplateSaveReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        FeeTemplateDO feeTemplate = BeanUtils.toBean(createReqVO, FeeTemplateDO.class);
        feeTemplateMapper.insert(feeTemplate);
        return feeTemplate.getId();
    }

    @Override
    public void updateFeeTemplate(FeeTemplateSaveReqVO updateReqVO) {
        // 校验存在
        validateFeeTemplateExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新（已快照到班期的费用配置不受影响）
        FeeTemplateDO updateObj = BeanUtils.toBean(updateReqVO, FeeTemplateDO.class);
        feeTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteFeeTemplate(Long id) {
        // 校验存在
        validateFeeTemplateExists(id);
        // 删除
        feeTemplateMapper.deleteById(id);
    }

    @Override
    public void deleteFeeTemplateListByIds(List<Long> ids) {
        feeTemplateMapper.deleteByIds(ids);
    }

    private void validateFeeTemplateExists(Long id) {
        if (feeTemplateMapper.selectById(id) == null) {
            throw exception(FEE_TEMPLATE_NOT_EXISTS);
        }
    }

    private void validateNameUnique(Long id, String name) {
        FeeTemplateDO template = feeTemplateMapper.selectByName(name);
        if (template == null || Objects.equals(template.getId(), id)) {
            return;
        }
        throw exception(FEE_TEMPLATE_NAME_DUPLICATE);
    }

    @Override
    public FeeTemplateDO getFeeTemplate(Long id) {
        return feeTemplateMapper.selectById(id);
    }

    @Override
    public FeeTemplateDO getEnabledFeeTemplate(Long id) {
        FeeTemplateDO template = feeTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(FEE_TEMPLATE_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus())) {
            throw exception(FEE_TEMPLATE_DISABLED);
        }
        return template;
    }

    @Override
    public PageResult<FeeTemplateDO> getFeeTemplatePage(FeeTemplatePageReqVO pageReqVO) {
        return feeTemplateMapper.selectPage(pageReqVO);
    }

}
