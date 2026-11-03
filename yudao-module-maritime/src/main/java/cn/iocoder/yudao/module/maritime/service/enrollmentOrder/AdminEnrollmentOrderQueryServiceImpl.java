package cn.iocoder.yudao.module.maritime.service.enrollmentOrder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.service.common.AdminMaritimeQuerySupport;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理后台 - 支付订单查询 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class AdminEnrollmentOrderQueryServiceImpl implements AdminEnrollmentOrderQueryService {

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private AdminMaritimeQuerySupport querySupport;

    @Override
    public EnrollmentOrderRespVO getEnrollmentOrder(Long id) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectById(id);
        if (order == null) {
            return null;
        }
        return convertList(Collections.singletonList(order)).get(0);
    }

    @Override
    public PageResult<EnrollmentOrderRespVO> getEnrollmentOrderPage(EnrollmentOrderPageReqVO pageReqVO) {
        // 页面没有报名ID输入框，用姓名/手机号先换算成报名ID
        if (StrUtil.isNotBlank(pageReqVO.getRealName()) || StrUtil.isNotBlank(pageReqVO.getPhone())) {
            List<Long> enrollmentIds = querySupport.findEnrollmentIds(
                    pageReqVO.getRealName(), pageReqVO.getPhone());
            if (CollUtil.isEmpty(enrollmentIds)) {
                return PageResult.empty();
            }
            pageReqVO.setEnrollmentIds(enrollmentIds);
        }
        PageResult<EnrollmentOrderDO> pageResult = enrollmentOrderMapper.selectPage(pageReqVO);
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    private List<EnrollmentOrderRespVO> convertList(List<EnrollmentOrderDO> orders) {
        if (CollUtil.isEmpty(orders)) {
            return Collections.emptyList();
        }
        Set<Long> enrollmentIds = orders.stream()
                .map(EnrollmentOrderDO::getEnrollmentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, EnrollmentDO> enrollmentMap = querySupport.getEnrollmentMap(enrollmentIds);

        Set<Long> sessionIds = enrollmentMap.values().stream()
                .map(EnrollmentDO::getSessionId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseSessionDO> sessionMap = querySupport.getSessionMap(sessionIds);

        Set<Long> courseIds = sessionMap.values().stream()
                .map(CourseSessionDO::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = querySupport.getCourseMap(courseIds);

        return orders.stream().map(order -> {
            EnrollmentOrderRespVO vo = BeanUtils.toBean(order, EnrollmentOrderRespVO.class);
            EnrollmentDO enrollment = enrollmentMap.get(order.getEnrollmentId());
            if (enrollment != null) {
                vo.setEnrollmentNo(enrollment.getEnrollmentNo());
                vo.setRealName(enrollment.getRealName());
                vo.setPhone(enrollment.getPhone());
                CourseSessionDO session = sessionMap.get(enrollment.getSessionId());
                if (session != null) {
                    vo.setSessionCode(session.getSessionCode());
                    vo.setSessionName(session.getSessionName());
                    vo.setSessionStartDate(session.getStartDate());
                    CourseDO course = courseMap.get(session.getCourseId());
                    if (course != null) {
                        vo.setCourseName(course.getName());
                    }
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

}
