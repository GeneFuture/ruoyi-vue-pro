package cn.iocoder.yudao.module.maritime.service.grouponMember;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 拼团成员 Service 接口
 *
 * @author Gene Ye
 */
public interface GrouponMemberService {

    /**
     * 创建拼团成员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGrouponMember(@Valid GrouponMemberSaveReqVO createReqVO);

    /**
     * 更新拼团成员
     *
     * @param updateReqVO 更新信息
     */
    void updateGrouponMember(@Valid GrouponMemberSaveReqVO updateReqVO);

    /**
     * 删除拼团成员
     *
     * @param id 编号
     */
    void deleteGrouponMember(Long id);

    /**
    * 批量删除拼团成员
    *
    * @param ids 编号
    */
    void deleteGrouponMemberListByIds(List<Long> ids);

    /**
     * 获得拼团成员
     *
     * @param id 编号
     * @return 拼团成员
     */
    GrouponMemberDO getGrouponMember(Long id);

    /**
     * 获得拼团成员分页
     *
     * @param pageReqVO 分页查询
     * @return 拼团成员分页
     */
    PageResult<GrouponMemberDO> getGrouponMemberPage(GrouponMemberPageReqVO pageReqVO);

}