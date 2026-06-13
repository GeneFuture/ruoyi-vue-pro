package cn.iocoder.yudao.module.maritime.service.session;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 班期管理 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class CourseSessionServiceImpl implements CourseSessionService {

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Override
    public Long createCourseSession(CourseSessionSaveReqVO createReqVO) {
        // 插入
        CourseSessionDO courseSession = BeanUtils.toBean(createReqVO, CourseSessionDO.class);
        courseSessionMapper.insert(courseSession);

        // 返回
        return courseSession.getId();
    }

    @Override
    public void updateCourseSession(CourseSessionSaveReqVO updateReqVO) {
        // 校验存在
        validateCourseSessionExists(updateReqVO.getId());
        // 更新
        CourseSessionDO updateObj = BeanUtils.toBean(updateReqVO, CourseSessionDO.class);
        courseSessionMapper.updateById(updateObj);
    }

    @Override
    public void deleteCourseSession(Long id) {
        // 校验存在
        validateCourseSessionExists(id);
        // 删除
        courseSessionMapper.deleteById(id);
    }

    @Override
        public void deleteCourseSessionListByIds(List<Long> ids) {
        // 删除
        courseSessionMapper.deleteByIds(ids);
        }


    private void validateCourseSessionExists(Long id) {
        if (courseSessionMapper.selectById(id) == null) {
            throw exception(COURSE_SESSION_NOT_EXISTS);
        }
    }

    @Override
    public CourseSessionDO getCourseSession(Long id) {
        return courseSessionMapper.selectById(id);
    }

    @Override
    public PageResult<CourseSessionDO> getCourseSessionPage(CourseSessionPageReqVO pageReqVO) {
        return courseSessionMapper.selectPage(pageReqVO);
    }

}