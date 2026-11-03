package cn.iocoder.yudao.module.maritime.service.grouponMember;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberRespVO;

/**
 * 管理后台 - 拼团成员查询 Service 接口
 *
 * @author Gene Ye
 */
public interface AdminGrouponMemberQueryService {

    /**
     * 获得拼团成员详情（含拼团、课程、班期、学员信息）
     */
    GrouponMemberRespVO getGrouponMember(Long id);

    /**
     * 获得拼团成员分页（含拼团、课程、班期、学员信息）
     */
    PageResult<GrouponMemberRespVO> getGrouponMemberPage(GrouponMemberPageReqVO pageReqVO);

}
