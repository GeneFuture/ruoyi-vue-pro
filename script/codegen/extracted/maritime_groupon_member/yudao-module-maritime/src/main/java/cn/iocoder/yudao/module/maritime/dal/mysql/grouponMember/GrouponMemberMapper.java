package cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.*;

/**
 * 拼团成员 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface GrouponMemberMapper extends BaseMapperX<GrouponMemberDO> {

    default PageResult<GrouponMemberDO> selectPage(GrouponMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GrouponMemberDO>()
                .eqIfPresent(GrouponMemberDO::getGrouponRecordId, reqVO.getGrouponRecordId())
                .eqIfPresent(GrouponMemberDO::getEnrollmentId, reqVO.getEnrollmentId())
                .eqIfPresent(GrouponMemberDO::getMemberId, reqVO.getMemberId())
                .betweenIfPresent(GrouponMemberDO::getJoinTime, reqVO.getJoinTime())
                .eqIfPresent(GrouponMemberDO::getMemberStatus, reqVO.getMemberStatus())
                .eqIfPresent(GrouponMemberDO::getDepositPaidAt, reqVO.getDepositPaidAt())
                .betweenIfPresent(GrouponMemberDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GrouponMemberDO::getId));
    }

}