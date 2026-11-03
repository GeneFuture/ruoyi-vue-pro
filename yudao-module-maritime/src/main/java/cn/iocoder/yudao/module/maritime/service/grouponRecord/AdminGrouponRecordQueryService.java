package cn.iocoder.yudao.module.maritime.service.grouponRecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordRespVO;

/**
 * 管理后台 - 拼团记录查询 Service 接口
 *
 * @author Gene Ye
 */
public interface AdminGrouponRecordQueryService {

    /**
     * 获得拼团记录详情（含课程、班期、发起人信息）
     */
    GrouponRecordRespVO getGrouponRecord(Long id);

    /**
     * 获得拼团记录分页（含课程、班期、发起人信息）
     */
    PageResult<GrouponRecordRespVO> getGrouponRecordPage(GrouponRecordPageReqVO pageReqVO);

}
