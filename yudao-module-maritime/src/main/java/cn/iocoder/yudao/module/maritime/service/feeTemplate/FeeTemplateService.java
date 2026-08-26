package cn.iocoder.yudao.module.maritime.service.feeTemplate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.FeeTemplatePageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.feeTemplate.vo.FeeTemplateSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 费用模板 Service 接口
 *
 * 独立的可复用定价方案；新增班期时选择模板并快照复制到班期费用配置。
 *
 * @author Gene Ye
 */
public interface FeeTemplateService {

    /**
     * 创建费用模板
     *
     * @param createReqVO 创建信息
     * @return 模板编号
     */
    Long createFeeTemplate(@Valid FeeTemplateSaveReqVO createReqVO);

    /**
     * 更新费用模板（不影响已快照的班期费用配置）
     *
     * @param updateReqVO 更新信息
     */
    void updateFeeTemplate(@Valid FeeTemplateSaveReqVO updateReqVO);

    /**
     * 删除费用模板
     *
     * @param id 编号
     */
    void deleteFeeTemplate(Long id);

    /**
     * 批量删除费用模板
     *
     * @param ids 编号列表
     */
    void deleteFeeTemplateListByIds(List<Long> ids);

    /**
     * 获得费用模板
     *
     * @param id 编号
     * @return 费用模板
     */
    FeeTemplateDO getFeeTemplate(Long id);

    /**
     * 获得启用状态的费用模板（新增班期下拉用），不存在则抛出异常
     *
     * @param id 编号
     * @return 启用中的费用模板
     */
    FeeTemplateDO getEnabledFeeTemplate(Long id);

    /**
     * 获得费用模板分页
     *
     * @param pageReqVO 分页查询
     * @return 费用模板分页
     */
    PageResult<FeeTemplateDO> getFeeTemplatePage(FeeTemplatePageReqVO pageReqVO);

}
