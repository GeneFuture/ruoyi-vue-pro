package cn.iocoder.yudao.module.maritime.service.refundApply;

import java.math.BigDecimal;
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

    // ========== 管理端 T07 ==========

    /**
     * 管理端：审核通过退费（发起微信退款、取消报名、冻结佣金）
     *
     * @param id           退费申请ID
     * @param refundAmount 实际退款金额（元）
     * @param approverId   审核人（系统用户ID）
     * @param adminRemark  管理员备注
     */
    void approveRefund(Long id, BigDecimal refundAmount, Long approverId, String adminRemark);

    /**
     * 管理端：拒绝退费申请
     *
     * @param id           退费申请ID
     * @param approverId   审核人（系统用户ID）
     * @param rejectReason 拒绝原因
     */
    void rejectRefund(Long id, Long approverId, String rejectReason);

}