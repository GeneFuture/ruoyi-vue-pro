package cn.iocoder.yudao.module.maritime.service.enrollmentOrder;

import cn.hutool.core.collection.CollUtil;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 报名订单 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class EnrollmentOrderServiceImpl implements EnrollmentOrderService {

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Override
    public Long createEnrollmentOrder(EnrollmentOrderSaveReqVO createReqVO) {
        // 插入
        EnrollmentOrderDO enrollmentOrder = BeanUtils.toBean(createReqVO, EnrollmentOrderDO.class);
        enrollmentOrderMapper.insert(enrollmentOrder);

        // 返回
        return enrollmentOrder.getId();
    }

    @Override
    public void updateEnrollmentOrder(EnrollmentOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateEnrollmentOrderExists(updateReqVO.getId());
        // 更新
        EnrollmentOrderDO updateObj = BeanUtils.toBean(updateReqVO, EnrollmentOrderDO.class);
        enrollmentOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteEnrollmentOrder(Long id) {
        // 校验存在
        validateEnrollmentOrderExists(id);
        // 删除
        enrollmentOrderMapper.deleteById(id);
    }

    @Override
        public void deleteEnrollmentOrderListByIds(List<Long> ids) {
        // 删除
        enrollmentOrderMapper.deleteByIds(ids);
        }


    private void validateEnrollmentOrderExists(Long id) {
        if (enrollmentOrderMapper.selectById(id) == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public EnrollmentOrderDO getEnrollmentOrder(Long id) {
        return enrollmentOrderMapper.selectById(id);
    }

    @Override
    public PageResult<EnrollmentOrderDO> getEnrollmentOrderPage(EnrollmentOrderPageReqVO pageReqVO) {
        return enrollmentOrderMapper.selectPage(pageReqVO);
    }

}