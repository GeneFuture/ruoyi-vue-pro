package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundResolveReqVO;

/**
 * 人工处理单 - 退费审核 Service 接口
 *
 * 退费申请已合并到人工处理，由本 Service 统一提供审核详情与审核操作，
 * 底层仍复用 {@link cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService}
 * 的退款登记、订单状态更新、名额归还与人工单关闭逻辑。
 *
 * @author Gene Ye
 */
public interface ManualTaskRefundService {

    /**
     * 获得退费类人工单的审核详情
     *
     * @param taskId 人工处理单ID
     * @return 人工单 + 退费申请详情
     */
    ManualTaskRefundDetailRespVO getRefundDetail(Long taskId);

    /**
     * 审核退费类人工单
     *
     * @param reqVO     审核参数
     * @param handlerId 审核人（系统用户ID）
     */
    void resolveRefundTask(ManualTaskRefundResolveReqVO reqVO, Long handlerId);

}
