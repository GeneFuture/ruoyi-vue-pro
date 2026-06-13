package cn.iocoder.yudao.module.maritime.service.enrollment;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 报名管理 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class EnrollmentServiceImpl implements EnrollmentService {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Override
    public Long createEnrollment(EnrollmentSaveReqVO createReqVO) {
        // 插入
        EnrollmentDO enrollment = BeanUtils.toBean(createReqVO, EnrollmentDO.class);
        enrollmentMapper.insert(enrollment);

        // 返回
        return enrollment.getId();
    }

    @Override
    public void updateEnrollment(EnrollmentSaveReqVO updateReqVO) {
        // 校验存在
        validateEnrollmentExists(updateReqVO.getId());
        // 更新
        EnrollmentDO updateObj = BeanUtils.toBean(updateReqVO, EnrollmentDO.class);
        enrollmentMapper.updateById(updateObj);
    }

    @Override
    public void deleteEnrollment(Long id) {
        // 校验存在
        validateEnrollmentExists(id);
        // 删除
        enrollmentMapper.deleteById(id);
    }

    @Override
    public void deleteEnrollmentListByIds(List<Long> ids) {
        // 删除
        enrollmentMapper.deleteByIds(ids);
    }


    private void validateEnrollmentExists(Long id) {
        if (enrollmentMapper.selectById(id) == null) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
    }

    @Override
    public EnrollmentDO getEnrollment(Long id) {
        return enrollmentMapper.selectById(id);
    }

    @Override
    public PageResult<EnrollmentDO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO) {
        return enrollmentMapper.selectPage(pageReqVO);
    }

}