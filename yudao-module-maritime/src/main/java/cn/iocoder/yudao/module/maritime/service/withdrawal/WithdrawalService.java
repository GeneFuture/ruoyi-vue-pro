package cn.iocoder.yudao.module.maritime.service.withdrawal;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.withdrawal.vo.AdminWithdrawalRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.withdrawal.vo.*;

/**
 * 佣金提现 Service 接口
 *
 * @author Gene Ye
 */
public interface WithdrawalService {

    /**
     * 申请提现
     */
    AppWithdrawalApplyRespVO applyWithdrawal(Long memberId, AppWithdrawalApplyReqVO reqVO);

    /**
     * 我的提现记录（分页）
     */
    PageResult<AppWithdrawalRecordRespVO> getMyWithdrawals(Long memberId, PageParam pageParam);

    /**
     * 提现前税额预览
     */
    AppTaxPreviewRespVO previewTax(Long memberId, java.math.BigDecimal amount);

    /**
     * 管理端：分页查询提现申请
     */
    PageResult<AdminWithdrawalRespVO> getWithdrawalPage(AdminWithdrawalPageReqVO reqVO);

    /**
     * 管理端：拒绝提现申请（PENDING → REJECTED，解冻余额）
     */
    void rejectWithdrawal(Long id, String reason);

}
