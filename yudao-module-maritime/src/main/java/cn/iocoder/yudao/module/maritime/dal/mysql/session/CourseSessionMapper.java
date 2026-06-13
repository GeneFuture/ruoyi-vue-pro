package cn.iocoder.yudao.module.maritime.dal.mysql.session;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.*;

/**
 * 班期管理 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface CourseSessionMapper extends BaseMapperX<CourseSessionDO> {

    default PageResult<CourseSessionDO> selectPage(CourseSessionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourseSessionDO>()
                .eqIfPresent(CourseSessionDO::getCourseId, reqVO.getCourseId())
                .eqIfPresent(CourseSessionDO::getSessionCode, reqVO.getSessionCode())
                .likeIfPresent(CourseSessionDO::getSessionName, reqVO.getSessionName())
                .eqIfPresent(CourseSessionDO::getLocation, reqVO.getLocation())
                .eqIfPresent(CourseSessionDO::getDurationDays, reqVO.getDurationDays())
                .eqIfPresent(CourseSessionDO::getInstructorInfo, reqVO.getInstructorInfo())
                .betweenIfPresent(CourseSessionDO::getStartDate, reqVO.getStartDate())
                .betweenIfPresent(CourseSessionDO::getEndDate, reqVO.getEndDate())
                .eqIfPresent(CourseSessionDO::getEnrollmentDeadline, reqVO.getEnrollmentDeadline())
                .eqIfPresent(CourseSessionDO::getMaxStudents, reqVO.getMaxStudents())
                .eqIfPresent(CourseSessionDO::getEnrolledCount, reqVO.getEnrolledCount())
                .eqIfPresent(CourseSessionDO::getSessionStatus, reqVO.getSessionStatus())
                .eqIfPresent(CourseSessionDO::getRemark, reqVO.getRemark())
                .betweenIfPresent(CourseSessionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CourseSessionDO::getId));
    }

}