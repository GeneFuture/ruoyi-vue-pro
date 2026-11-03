package cn.iocoder.yudao.module.maritime.service.manualTask;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRefundResolveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.manualTask.ManualTaskMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.MANUAL_TASK_NOT_REFUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

/**
 * {@link ManualTaskRefundServiceImpl} 的单元测试类
 *
 * 重点覆盖：退费人工单详情聚合、通过/拒绝审核委托给 RefundApplyService、非退费单拦截。
 */
@Import(ManualTaskRefundServiceImpl.class)
public class ManualTaskRefundServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ManualTaskRefundServiceImpl manualTaskRefundService;
    @Resource
    private ManualTaskMapper manualTaskMapper;
    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @MockitoBean
    private RefundApplyService refundApplyService;

    @Test
    public void testGetRefundDetail_success() {
        EnrollmentOrderDO order = EnrollmentOrderDO.builder()
                .enrollmentId(100L)
                .orderNo("DEPOSIT-100")
                .orderType("DEPOSIT")
                .amount(new BigDecimal("500.00"))
                .orderStatus("PAID")
                .build();
        enrollmentOrderMapper.insert(order);
        RefundApplyDO apply = RefundApplyDO.builder()
                .enrollmentId(100L)
                .orderId(order.getId())
                .memberId(200L)
                .applyReason("临时有事")
                .refundAmount(new BigDecimal("500.00"))
                .applyStatus("PENDING")
                .build();
        refundApplyMapper.insert(apply);
        ManualTaskDO task = ManualTaskDO.builder()
                .taskNo("MT100")
                .enrollmentId(100L)
                .memberId(200L)
                .orderId(order.getId())
                .sourceType("REFUND")
                .sourceId(apply.getId())
                .taskType(ManualTaskTypeEnum.REFUND_APPLY.getType())
                .status(ManualTaskStatusEnum.PENDING.getStatus())
                .priority(0)
                .reason("学员申请退费：临时有事")
                .build();
        manualTaskMapper.insert(task);

        ManualTaskRefundDetailRespVO detail = manualTaskRefundService.getRefundDetail(task.getId());

        assertEquals(task.getId(), detail.getTaskId());
        assertEquals(apply.getId(), detail.getRefundApplyId());
        assertEquals("DEPOSIT-100", detail.getOrderNo());
        assertEquals(new BigDecimal("500.00"), detail.getRefundAmount());
        assertEquals("PENDING", detail.getApplyStatus());
        assertNotNull(detail.getCreateTime());
    }

    @Test
    public void testGetRefundDetail_closedTaskAllowed() {
        ManualTaskDO task = insertRefundTask();
        ManualTaskDO updateTask = new ManualTaskDO();
        updateTask.setId(task.getId());
        updateTask.setStatus(ManualTaskStatusEnum.DONE.getStatus());
        manualTaskMapper.updateById(updateTask);

        ManualTaskRefundDetailRespVO detail = manualTaskRefundService.getRefundDetail(task.getId());

        assertEquals(task.getId(), detail.getTaskId());
        assertEquals(ManualTaskStatusEnum.DONE.getStatus(), detail.getTaskStatus());
    }

    @Test
    public void testResolveRefundTask_approve() {
        ManualTaskDO task = insertRefundTask();
        RefundApplyDO apply = refundApplyMapper.selectById(task.getSourceId());

        ManualTaskRefundResolveReqVO reqVO = new ManualTaskRefundResolveReqVO();
        reqVO.setId(task.getId());
        reqVO.setResult("APPROVE");
        reqVO.setRefundAmount(new BigDecimal("300.00"));
        reqVO.setAdminRemark("同意退款");
        manualTaskRefundService.resolveRefundTask(reqVO, 1L);

        verify(refundApplyService).approveRefund(apply.getId(), new BigDecimal("300.00"), 1L, "同意退款");
    }

    @Test
    public void testResolveRefundTask_reject() {
        ManualTaskDO task = insertRefundTask();
        RefundApplyDO apply = refundApplyMapper.selectById(task.getSourceId());

        ManualTaskRefundResolveReqVO reqVO = new ManualTaskRefundResolveReqVO();
        reqVO.setId(task.getId());
        reqVO.setResult("REJECT");
        reqVO.setRejectReason("已开班，不符合退费政策");
        manualTaskRefundService.resolveRefundTask(reqVO, 1L);

        verify(refundApplyService).rejectRefund(apply.getId(), 1L, "已开班，不符合退费政策");
    }

    @Test
    public void testResolveRefundTask_notRefundTask() {
        ManualTaskDO task = ManualTaskDO.builder()
                .taskNo("MT200")
                .enrollmentId(100L)
                .memberId(200L)
                .sourceType("OTHER")
                .taskType(ManualTaskTypeEnum.BALANCE_OVERDUE.getType())
                .status(ManualTaskStatusEnum.PENDING.getStatus())
                .priority(0)
                .reason("尾款逾期")
                .build();
        manualTaskMapper.insert(task);
        ManualTaskRefundResolveReqVO reqVO = new ManualTaskRefundResolveReqVO();
        reqVO.setId(task.getId());
        reqVO.setResult("APPROVE");
        reqVO.setRefundAmount(BigDecimal.ONE);

        assertServiceException(() -> manualTaskRefundService.resolveRefundTask(reqVO, 1L),
                MANUAL_TASK_NOT_REFUND);
    }

    private ManualTaskDO insertRefundTask() {
        RefundApplyDO apply = RefundApplyDO.builder()
                .enrollmentId(100L)
                .memberId(200L)
                .applyReason("临时有事")
                .applyStatus("PENDING")
                .build();
        refundApplyMapper.insert(apply);
        ManualTaskDO task = ManualTaskDO.builder()
                .taskNo("MT" + apply.getId())
                .enrollmentId(100L)
                .memberId(200L)
                .sourceType("REFUND")
                .sourceId(apply.getId())
                .taskType(ManualTaskTypeEnum.REFUND_APPLY.getType())
                .status(ManualTaskStatusEnum.PENDING.getStatus())
                .priority(0)
                .reason("学员申请退费：临时有事")
                .build();
        manualTaskMapper.insert(task);
        return task;
    }

}
