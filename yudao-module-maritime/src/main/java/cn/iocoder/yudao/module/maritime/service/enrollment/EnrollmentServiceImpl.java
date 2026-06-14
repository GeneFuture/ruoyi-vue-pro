package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentCreateRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.AppEnrollmentListRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 报名管理 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private EnrollmentOrderMapper enrollmentOrderMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    // ========== Admin CRUD ==========

    @Override
    public Long createEnrollment(EnrollmentSaveReqVO createReqVO) {
        EnrollmentDO enrollment = BeanUtils.toBean(createReqVO, EnrollmentDO.class);
        enrollmentMapper.insert(enrollment);
        return enrollment.getId();
    }

    @Override
    public void updateEnrollment(EnrollmentSaveReqVO updateReqVO) {
        validateEnrollmentExists(updateReqVO.getId());
        EnrollmentDO updateObj = BeanUtils.toBean(updateReqVO, EnrollmentDO.class);
        enrollmentMapper.updateById(updateObj);
    }

    @Override
    public void deleteEnrollment(Long id) {
        validateEnrollmentExists(id);
        enrollmentMapper.deleteById(id);
    }

    @Override
    public void deleteEnrollmentListByIds(List<Long> ids) {
        enrollmentMapper.deleteByIds(ids);
    }

    @Override
    public EnrollmentDO getEnrollment(Long id) {
        return enrollmentMapper.selectById(id);
    }

    @Override
    public PageResult<EnrollmentDO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO) {
        return enrollmentMapper.selectPage(pageReqVO);
    }

    // ========== 小程序端 T03 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppEnrollmentCreateRespVO createEnrollmentForApp(AppEnrollmentCreateReqVO createReqVO, Long memberId) {
        // Step 1: 班期存在且状态为 OPEN
        CourseSessionDO session = courseSessionMapper.selectById(createReqVO.getSessionId());
        if (session == null) {
            throw exception(SESSION_NOT_EXISTS);
        }
        if (!"OPEN".equals(session.getSessionStatus())) {
            throw exception(SESSION_NOT_OPEN);
        }
        if (session.getEnrolledCount() >= session.getMaxStudents()) {
            throw exception(SESSION_FULL);
        }

        // Step 2: 获取费用配置（必须存在）
        SessionFeeConfigDO feeConfig = sessionFeeConfigMapper.selectBySessionId(session.getId());
        if (feeConfig == null) {
            throw exception(SESSION_FEE_CONFIG_NOT_EXISTS);
        }

        // Step 3: 同一身份证未重复报名同班期（加密后与库中加密值比对）
        String encryptedIdCard = EncryptTypeHandler.encrypt(createReqVO.getIdCard());
        EnrollmentDO existing = enrollmentMapper.selectByIdCardAndSession(encryptedIdCard, createReqVO.getSessionId());
        if (existing != null) {
            throw exception(ENROLLMENT_DUPLICATE);
        }

        // Step 4: 创建报名记录（传入明文，TypeHandler 负责加密写库）
        String enrollmentNo = "EN" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
        EnrollmentDO enrollment = new EnrollmentDO();
        enrollment.setEnrollmentNo(enrollmentNo);
        enrollment.setMemberId(memberId);
        enrollment.setSessionId(createReqVO.getSessionId());
        enrollment.setRealName(createReqVO.getRealName());
        enrollment.setIdCard(createReqVO.getIdCard()); // 明文，EncryptTypeHandler 写库时自动加密
        enrollment.setPhone(createReqVO.getPhone());
        enrollment.setReferralCodeUsed(StrUtil.isBlank(createReqVO.getReferralCode()) ? null : createReqVO.getReferralCode());
        enrollment.setTotalAmount(feeConfig.getTuitionAmount());
        enrollment.setDepositAmountSnapshot(feeConfig.getDepositAmount());
        enrollment.setBalanceAmount(feeConfig.getTuitionAmount().subtract(feeConfig.getDepositAmount()));
        enrollment.setPaidAmount(java.math.BigDecimal.ZERO);
        enrollment.setEnrollmentStatus(EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus());
        enrollment.setReferralRightGranted(false);
        // 尾款截止日期 = 开班日期 - N 天
        if (session.getStartDate() != null && feeConfig.getBalanceDueDaysBeforeStart() != null) {
            enrollment.setBalanceDueDate(session.getStartDate().minusDays(feeConfig.getBalanceDueDaysBeforeStart()));
        }
        enrollmentMapper.insert(enrollment);

        // Step 5: 创建定金订单（30分钟过期）
        String orderNo = "OD" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        EnrollmentOrderDO depositOrder = new EnrollmentOrderDO();
        depositOrder.setEnrollmentId(enrollment.getId());
        depositOrder.setOrderNo(orderNo);
        depositOrder.setOrderType(OrderTypeEnum.DEPOSIT.getType());
        depositOrder.setAmount(feeConfig.getDepositAmount());
        depositOrder.setOrderStatus(OrderStatusEnum.PENDING.getStatus());
        depositOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
        enrollmentOrderMapper.insert(depositOrder);

        // Step 6: CAS 原子递增报名人数（防超卖）
        int updated = courseSessionMapper.incrementEnrolledCount(session.getId(), session.getMaxStudents());
        if (updated == 0) {
            throw exception(SESSION_FULL);
        }

        AppEnrollmentCreateRespVO respVO = new AppEnrollmentCreateRespVO();
        respVO.setEnrollmentId(enrollment.getId());
        respVO.setOrderId(depositOrder.getId());
        respVO.setOrderNo(depositOrder.getOrderNo());
        respVO.setDepositAmount(depositOrder.getAmount());
        respVO.setExpireTime(depositOrder.getExpireTime());
        return respVO;
    }

    @Override
    public List<AppEnrollmentListRespVO> getMyEnrollments(Long memberId) {
        List<EnrollmentDO> enrollments = enrollmentMapper.selectListByMemberId(memberId);
        if (enrollments.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 批量查询班期信息
        List<Long> sessionIds = enrollments.stream().map(EnrollmentDO::getSessionId).distinct().collect(Collectors.toList());
        Map<Long, CourseSessionDO> sessionMap = courseSessionMapper.selectList(CourseSessionDO::getId, sessionIds).stream()
                .collect(Collectors.toMap(CourseSessionDO::getId, s -> s));

        return enrollments.stream().map(e -> {
            AppEnrollmentListRespVO vo = BeanUtils.toBean(e, AppEnrollmentListRespVO.class);
            CourseSessionDO session = sessionMap.get(e.getSessionId());
            if (session != null) {
                vo.setSessionCode(session.getSessionCode());
                vo.setSessionName(session.getSessionName());
                vo.setStartDate(session.getStartDate());
                vo.setLocation(session.getLocation());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AppEnrollmentDetailRespVO getEnrollmentForApp(Long id, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        if (!enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        AppEnrollmentDetailRespVO vo = BeanUtils.toBean(enrollment, AppEnrollmentDetailRespVO.class);

        // 脱敏身份证：前4后4，中间*
        if (StrUtil.isNotBlank(enrollment.getIdCard())) {
            String raw = enrollment.getIdCard();
            if (raw.length() >= 8) {
                vo.setIdCardMasked(raw.substring(0, 4) + "**********" + raw.substring(raw.length() - 4));
            }
        }
        // 脱敏手机号：前3后4
        if (StrUtil.isNotBlank(enrollment.getPhone())) {
            String p = enrollment.getPhone();
            if (p.length() == 11) {
                vo.setPhoneMasked(p.substring(0, 3) + "****" + p.substring(7));
            }
            // 非标准长度：不展示原值，留 null 保护隐私
        }

        // 班期信息
        CourseSessionDO session = courseSessionMapper.selectById(enrollment.getSessionId());
        if (session != null) {
            vo.setSessionCode(session.getSessionCode());
            vo.setSessionName(session.getSessionName());
            vo.setStartDate(session.getStartDate());
            vo.setEndDate(session.getEndDate());
            vo.setLocation(session.getLocation());
        }

        // 订单列表
        List<EnrollmentOrderDO> orders = enrollmentOrderMapper.selectListByEnrollmentId(id);
        List<AppEnrollmentDetailRespVO.OrderItem> orderItems = orders.stream().map(o -> {
            AppEnrollmentDetailRespVO.OrderItem item = new AppEnrollmentDetailRespVO.OrderItem();
            item.setId(o.getId());
            item.setOrderNo(o.getOrderNo());
            item.setOrderType(o.getOrderType());
            item.setAmount(o.getAmount());
            item.setOrderStatus(o.getOrderStatus());
            item.setPayTime(o.getPayTime());
            item.setExpireTime(o.getExpireTime());
            return item;
        }).collect(Collectors.toList());
        vo.setOrders(orderItems);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnrollmentForApp(Long id, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        if (!enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        if (!EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus().equals(enrollment.getEnrollmentStatus())) {
            throw exception(ENROLLMENT_CANCEL_NOT_ALLOWED);
        }

        // 更新报名状态为取消
        EnrollmentDO updateObj = new EnrollmentDO();
        updateObj.setId(id);
        updateObj.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
        enrollmentMapper.updateById(updateObj);

        // 关闭待支付订单
        List<EnrollmentOrderDO> pendingOrders = enrollmentOrderMapper.selectPendingByEnrollmentId(id);
        for (EnrollmentOrderDO order : pendingOrders) {
            EnrollmentOrderDO closeObj = new EnrollmentOrderDO();
            closeObj.setId(order.getId());
            closeObj.setOrderStatus(OrderStatusEnum.CLOSED.getStatus());
            enrollmentOrderMapper.updateById(closeObj);
        }

        // 归还班期名额
        courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());
    }

    // ========== 后台管理端 T03 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmEnrollmentByAdmin(Long id) {
        EnrollmentDO enrollment = validateEnrollmentExists(id);
        String status = enrollment.getEnrollmentStatus();
        // 已终态（完成/取消）不允许确认
        if (EnrollmentStatusEnum.COMPLETED.getStatus().equals(status)
                || EnrollmentStatusEnum.CANCELLED.getStatus().equals(status)) {
            throw exception(ENROLLMENT_CONFIRM_NOT_ALLOWED);
        }
        // 已过定金阶段，幂等返回
        if (!EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus().equals(status)) {
            return;
        }
        EnrollmentDO updateObj = new EnrollmentDO();
        updateObj.setId(id);
        updateObj.setEnrollmentStatus(EnrollmentStatusEnum.DEPOSITED.getStatus());
        enrollmentMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnrollmentByAdmin(Long id, String cancelReason) {
        EnrollmentDO enrollment = validateEnrollmentExists(id);
        String status = enrollment.getEnrollmentStatus();
        // 已完成或已取消的不能再取消
        if (EnrollmentStatusEnum.COMPLETED.getStatus().equals(status)
                || EnrollmentStatusEnum.CANCELLED.getStatus().equals(status)) {
            throw exception(ENROLLMENT_CANCEL_NOT_ALLOWED);
        }

        EnrollmentDO updateObj = new EnrollmentDO();
        updateObj.setId(id);
        updateObj.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
        updateObj.setCancelReason(cancelReason);
        enrollmentMapper.updateById(updateObj);

        // 关闭待支付订单
        List<EnrollmentOrderDO> pendingOrders = enrollmentOrderMapper.selectPendingByEnrollmentId(id);
        for (EnrollmentOrderDO order : pendingOrders) {
            EnrollmentOrderDO closeObj = new EnrollmentOrderDO();
            closeObj.setId(order.getId());
            closeObj.setOrderStatus(OrderStatusEnum.CLOSED.getStatus());
            enrollmentOrderMapper.updateById(closeObj);
        }

        // 凡未到终态的报名都已占用名额（CAS 时已 +1），取消时必须归还
        courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());
    }

    // ========== 私有工具 ==========

    private EnrollmentDO validateEnrollmentExists(Long id) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        return enrollment;
    }

}
