package cn.iocoder.yudao.module.maritime.dal.mysql.enrollment;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;

/**
 * 报名管理 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface EnrollmentMapper extends BaseMapperX<EnrollmentDO> {

    default PageResult<EnrollmentDO> selectPage(EnrollmentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EnrollmentDO>()
                .eqIfPresent(EnrollmentDO::getEnrollmentNo, reqVO.getEnrollmentNo())
                .eqIfPresent(EnrollmentDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(EnrollmentDO::getSessionId, reqVO.getSessionId())
                .eqIfPresent(EnrollmentDO::getGrouponRecordId, reqVO.getGrouponRecordId())
                .eqIfPresent(EnrollmentDO::getReferredByMemberId, reqVO.getReferredByMemberId())
                .eqIfPresent(EnrollmentDO::getReferralCodeUsed, reqVO.getReferralCodeUsed())
                .likeIfPresent(EnrollmentDO::getRealName, reqVO.getRealName())
                .eqIfPresent(EnrollmentDO::getPhone, reqVO.getPhone())
                .eqIfPresent(EnrollmentDO::getEnrollmentStatus, reqVO.getEnrollmentStatus())
                .betweenIfPresent(EnrollmentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EnrollmentDO::getId));
    }

    /** 按加密后的身份证 + 班期ID 查（用于重复报名检查） */
    default EnrollmentDO selectByIdCardAndSession(String encryptedIdCard, Long sessionId) {
        return selectOne(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getIdCard, encryptedIdCard)
                .eq(EnrollmentDO::getSessionId, sessionId)
                .ne(EnrollmentDO::getEnrollmentStatus, "CANCELLED"));
    }

    /** 查某学员的所有报名，按创建时间倒序 */
    default List<EnrollmentDO> selectListByMemberId(Long memberId) {
        return selectList(new LambdaQueryWrapperX<EnrollmentDO>()
                .eq(EnrollmentDO::getMemberId, memberId)
                .orderByDesc(EnrollmentDO::getCreateTime));
    }

}