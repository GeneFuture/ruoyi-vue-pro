package cn.iocoder.yudao.module.maritime.service.grouponRecord;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 拼团记录 Service 接口
 *
 * @author Gene Ye
 */
public interface GrouponRecordService {

    /**
     * 创建拼团记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGrouponRecord(@Valid GrouponRecordSaveReqVO createReqVO);

    /**
     * 更新拼团记录
     *
     * @param updateReqVO 更新信息
     */
    void updateGrouponRecord(@Valid GrouponRecordSaveReqVO updateReqVO);

    /**
     * 删除拼团记录
     *
     * @param id 编号
     */
    void deleteGrouponRecord(Long id);

    /**
    * 批量删除拼团记录
    *
    * @param ids 编号
    */
    void deleteGrouponRecordListByIds(List<Long> ids);

    /**
     * 获得拼团记录
     *
     * @param id 编号
     * @return 拼团记录
     */
    GrouponRecordDO getGrouponRecord(Long id);

    /**
     * 获得拼团记录分页
     *
     * @param pageReqVO 分页查询
     * @return 拼团记录分页
     */
    PageResult<GrouponRecordDO> getGrouponRecordPage(GrouponRecordPageReqVO pageReqVO);

}