package cn.iocoder.yudao.module.maritime.service.refundApply;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.refundApply.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 退费申请 Service 接口
 *
 * @author Gene Ye
 */
public interface RefundApplyService {

    /**
     * 创建退费申请
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRefundApply(@Valid RefundApplySaveReqVO createReqVO);

    /**
     * 更新退费申请
     *
     * @param updateReqVO 更新信息
     */
    void updateRefundApply(@Valid RefundApplySaveReqVO updateReqVO);

    /**
     * 删除退费申请
     *
     * @param id 编号
     */
    void deleteRefundApply(Long id);

    /**
    * 批量删除退费申请
    *
    * @param ids 编号
    */
    void deleteRefundApplyListByIds(List<Long> ids);

    /**
     * 获得退费申请
     *
     * @param id 编号
     * @return 退费申请
     */
    RefundApplyDO getRefundApply(Long id);

    /**
     * 获得退费申请分页
     *
     * @param pageReqVO 分页查询
     * @return 退费申请分页
     */
    PageResult<RefundApplyDO> getRefundApplyPage(RefundApplyPageReqVO pageReqVO);

}