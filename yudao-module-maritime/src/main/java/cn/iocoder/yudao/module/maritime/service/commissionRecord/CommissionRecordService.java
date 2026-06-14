package cn.iocoder.yudao.module.maritime.service.commissionRecord;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.referral.vo.AppCommissionRecordRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.commissionRecord.CommissionRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 佣金记录 Service 接口
 *
 * @author Gene Ye
 */
public interface CommissionRecordService {

    /**
     * 创建佣金记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCommissionRecord(@Valid CommissionRecordSaveReqVO createReqVO);

    /**
     * 更新佣金记录
     *
     * @param updateReqVO 更新信息
     */
    void updateCommissionRecord(@Valid CommissionRecordSaveReqVO updateReqVO);

    /**
     * 删除佣金记录
     *
     * @param id 编号
     */
    void deleteCommissionRecord(Long id);

    /**
    * 批量删除佣金记录
    *
    * @param ids 编号
    */
    void deleteCommissionRecordListByIds(List<Long> ids);

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    CommissionRecordDO getCommissionRecord(Long id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<CommissionRecordDO> getCommissionRecordPage(CommissionRecordPageReqVO pageReqVO);

    /**
     * 全款支付后触发佣金（幂等，无推荐关系或已触发则忽略）
     *
     * @param enrollmentId 报名ID
     */
    void triggerCommissionIfEligible(Long enrollmentId);

    /**
     * 管理员审核通过佣金
     *
     * @param id         佣金记录ID
     * @param approverId 审核人ID
     * @param remark     备注
     */
    void approveCommission(Long id, Long approverId, String remark);

    /**
     * 管理员拒绝佣金
     *
     * @param id         佣金记录ID
     * @param approverId 审核人ID
     * @param reason     拒绝原因
     * @param remark     备注
     */
    void rejectCommission(Long id, Long approverId, String reason, String remark);

    /**
     * 获得我的佣金记录分页（App 端）
     *
     * @param memberId  推荐人ID
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<AppCommissionRecordRespVO> getMyCommissionRecords(Long memberId, PageParam pageParam);

    /**
     * 结算检查：WAITING_FOR_CLASS → PENDING_REVIEW 或 FROZEN（T08 Job 调用）
     *
     * @param recordId 佣金记录ID
     */
    void processSettleCheck(Long recordId);

    /**
     * 发放单条佣金（T08 Job 调用）
     *
     * @param recordId 佣金记录ID
     */
    void payoutCommission(Long recordId);

    /**
     * 手动补发（仅 FAILED 状态可操作）
     *
     * @param recordId 佣金记录ID
     */
    void manualPayout(Long recordId);

}