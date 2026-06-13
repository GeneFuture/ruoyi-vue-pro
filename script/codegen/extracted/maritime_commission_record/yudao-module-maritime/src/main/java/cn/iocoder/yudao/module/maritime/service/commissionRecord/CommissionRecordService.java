package cn.iocoder.yudao.module.maritime.service.commissionRecord;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.commissionRecord.vo.*;
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

}