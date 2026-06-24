package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.type.EncryptTypeHandler;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayBalanceReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.order.vo.AppPayOrderStatusRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.service.refundApply.RefundApplyService;
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
import cn.iocoder.yudao.module.maritime.mq.event.DepositPaidEvent;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCreatedEvent;
import cn.iocoder.yudao.module.maritime.service.commissionAccount.CommissionAccountService;
import cn.iocoder.yudao.module.maritime.service.commissionRecord.CommissionRecordService;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponProcessResult;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponService;
import cn.iocoder.yudao.module.maritime.service.referralRelation.ReferralRelationService;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.servlet.ServletUtils.getClientIP;
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

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private CommissionAccountService commissionAccountService;

    @Resource
    private ReferralRelationService referralRelationService;

    @Resource
    private CommissionRecordService commissionRecordService;

    @Resource
    private GrouponService grouponService;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    @Resource
    private RefundApplyService refundApplyService;

    @Resource
    @org.springframework.context.annotation.Lazy
    private EnrollmentService self;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    private static final String PAY_APP_KEY = "maritime";

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
        eventPublisher.publishEvent(new EnrollmentCreatedEvent(this, enrollment.getId(), memberId, enrollment.getSessionId()));

        // Step 4.5: 处理拼团逻辑（在创建订单前确定实际定金金额）
        java.math.BigDecimal depositAmount = feeConfig.getDepositAmount();
        if (Boolean.TRUE.equals(createReqVO.getJoinGroupon())) {
            GrouponProcessResult grouponResult = grouponService.joinOrCreateGroupon(
                    enrollment.getId(),
                    memberId,
                    createReqVO.getGrouponInviteCode(),
                    enrollment.getSessionId(),
                    feeConfig
            );
            enrollmentMapper.updateGrouponRecordId(enrollment.getId(), grouponResult.grouponRecordId());
            // 定金用拼团优惠价
            if (feeConfig.getGrouponDiscountAmount() != null) {
                depositAmount = depositAmount.subtract(feeConfig.getGrouponDiscountAmount());
            }
        }

        // Step 5: 创建定金订单（30分钟过期）
        String orderNo = "OD" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        EnrollmentOrderDO depositOrder = new EnrollmentOrderDO();
        depositOrder.setEnrollmentId(enrollment.getId());
        depositOrder.setOrderNo(orderNo);
        depositOrder.setOrderType(OrderTypeEnum.DEPOSIT.getType());
        depositOrder.setAmount(depositAmount);
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

    // ========== 小程序端 T04 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPayOrderRespVO payDeposit(Long orderId, Long memberId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }
        if (!OrderStatusEnum.PENDING.getStatus().equals(order.getOrderStatus())) {
            throw exception(ORDER_ALREADY_PAID);
        }
        if (order.getExpireTime().isBefore(LocalDateTime.now())) {
            throw exception(ORDER_EXPIRED);
        }
        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        // 幂等：已创建过 pay 单则直接返回
        if (order.getPayOrderId() != null) {
            AppPayOrderRespVO resp = new AppPayOrderRespVO();
            resp.setPayOrderId(order.getPayOrderId());
            resp.setAmount(order.getAmount());
            return resp;
        }

        // 调用 pay 模块创建支付单
        PayOrderCreateReqDTO payReq = new PayOrderCreateReqDTO();
        payReq.setAppKey(PAY_APP_KEY);
        payReq.setUserIp(getClientIP());
        payReq.setUserId(memberId);
        payReq.setUserType(UserTypeEnum.MEMBER.getValue());
        payReq.setMerchantOrderId(order.getOrderNo());
        payReq.setSubject("海员培训定金");
        payReq.setBody(order.getOrderNo() + " 定金");
        payReq.setPrice(order.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        payReq.setExpireTime(order.getExpireTime());
        Long payOrderId = payOrderApi.createOrder(payReq);

        // 记录 pay 模块订单ID
        enrollmentOrderMapper.updatePayOrderId(orderId, payOrderId);

        AppPayOrderRespVO resp = new AppPayOrderRespVO();
        resp.setPayOrderId(payOrderId);
        resp.setAmount(order.getAmount());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDepositPaid(String merchantOrderId, Long payOrderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByOrderNo(merchantOrderId);
        if (order == null) {
            log.error("[handleDepositPaid] 订单不存在: {}", merchantOrderId);
            return;
        }

        // 校验支付金额（防止金额篡改），payOrderId 为 null 时跳过（管理员手动确认场景）
        if (payOrderId != null) {
            PayOrderRespDTO payOrderResp = payOrderApi.getOrder(payOrderId);
            if (payOrderResp != null) {
                BigDecimal paidAmount = BigDecimal.valueOf(payOrderResp.getPrice())
                        .divide(BigDecimal.valueOf(100));
                if (paidAmount.compareTo(order.getAmount()) != 0) {
                    log.error("[handleDepositPaid] 支付金额不匹配，暂停处理！orderNo={}, 期望={}元, 实际={}元",
                            merchantOrderId, order.getAmount(), paidAmount);
                    return;
                }
            }
        }

        // CAS 幂等：只处理 PENDING → PAID
        int updated = enrollmentOrderMapper.updateStatusIfPending(order.getId(), OrderStatusEnum.PAID.getStatus());
        if (updated == 0) {
            log.warn("[handleDepositPaid] 订单已处理，跳过: {}", merchantOrderId);
            return;
        }

        // 记录支付时间
        EnrollmentOrderDO updateOrder = new EnrollmentOrderDO();
        updateOrder.setId(order.getId());
        updateOrder.setPayTime(LocalDateTime.now());
        enrollmentOrderMapper.updateById(updateOrder);

        // 更新报名状态：PENDING_DEPOSIT → DEPOSITED，授予推荐权，累加已付金额
        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment == null) {
            log.error("[handleDepositPaid] 报名记录不存在, enrollmentId={}", order.getEnrollmentId());
            return;
        }
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.DEPOSITED.getStatus());
        updateEnrollment.setReferralRightGranted(true);
        updateEnrollment.setPaidAmount(enrollment.getPaidAmount().add(order.getAmount()));
        enrollmentMapper.updateById(updateEnrollment);

        eventPublisher.publishEvent(new DepositPaidEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(), order.getAmount()));

        // 初始化佣金账户（幂等）
        commissionAccountService.initAccountIfAbsent(enrollment.getMemberId());

        // 建立推荐关系（若有推荐码）
        referralRelationService.establishRelationIfPresent(enrollment.getId());

        // 若无尾款（全款即定金），定金支付即触发佣金
        if (enrollment.getBalanceAmount() == null
                || enrollment.getBalanceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            commissionRecordService.triggerCommissionIfEligible(enrollment.getId());
        }

        // 处理拼团支付（若参与了拼团，记录支付并检查是否达成成团条件）
        grouponService.handleMemberPaid(enrollment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredOrder(Long orderId) {
        // CAS 幂等：只关闭 PENDING 订单
        int updated = enrollmentOrderMapper.updateStatusIfPending(orderId, OrderStatusEnum.CLOSED.getStatus());
        if (updated == 0) {
            return;
        }

        EnrollmentOrderDO order = enrollmentOrderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        // 若报名仍是 PENDING_DEPOSIT，取消报名并归还名额
        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment != null
                && EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus().equals(enrollment.getEnrollmentStatus())) {
            EnrollmentDO updateEnrollment = new EnrollmentDO();
            updateEnrollment.setId(enrollment.getId());
            updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.CANCELLED.getStatus());
            enrollmentMapper.updateById(updateEnrollment);
            courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());
        }
    }

    @Override
    public AppPayOrderStatusRespVO getPayOrderStatus(Long payOrderId, Long memberId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByPayOrderId(payOrderId);
        if (order == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }
        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }

        boolean paid = OrderStatusEnum.PAID.getStatus().equals(order.getOrderStatus());
        AppPayOrderStatusRespVO respVO = new AppPayOrderStatusRespVO();
        respVO.setPayOrderId(payOrderId);
        respVO.setPaid(paid);
        if (paid) {
            respVO.setEnrollmentId(order.getEnrollmentId());
        }
        return respVO;
    }

    // ========== 后台管理端 T04 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualConfirmDeposit(Long orderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }
        // 通过 self 代理调用，确保 @Transactional 生效（避免 Spring AOP 自调用陷阱）
        self.handleDepositPaid(order.getOrderNo(), order.getPayOrderId());
    }

    // ========== 小程序端 T07 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPayOrderRespVO payBalance(AppPayBalanceReqVO reqVO, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(reqVO.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        if (!EnrollmentStatusEnum.DEPOSITED.getStatus().equals(enrollment.getEnrollmentStatus())) {
            throw exception(ENROLLMENT_STATUS_ERROR);
        }

        // 查找已付定金订单，计算尾款
        EnrollmentOrderDO depositOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollment.getId(), OrderTypeEnum.DEPOSIT.getType(), OrderStatusEnum.PAID.getStatus());
        if (depositOrder == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }
        SessionFeeConfigDO feeConfig = sessionFeeConfigMapper.selectBySessionId(enrollment.getSessionId());
        if (feeConfig == null) {
            throw exception(SESSION_FEE_CONFIG_NOT_EXISTS);
        }
        BigDecimal balanceAmount = feeConfig.getTuitionAmount().subtract(depositOrder.getAmount());
        if (balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(BALANCE_NOT_REQUIRED);
        }

        // 防重复创建：已有 PENDING 或 PAID 的尾款订单则复用
        EnrollmentOrderDO existingBalance = enrollmentOrderMapper.selectByEnrollmentIdAndType(
                enrollment.getId(), OrderTypeEnum.BALANCE.getType());
        if (existingBalance == null || OrderStatusEnum.CLOSED.getStatus().equals(existingBalance.getOrderStatus())) {
            String orderNo = "OB" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
            existingBalance = new EnrollmentOrderDO();
            existingBalance.setEnrollmentId(enrollment.getId());
            existingBalance.setOrderNo(orderNo);
            existingBalance.setOrderType(OrderTypeEnum.BALANCE.getType());
            existingBalance.setAmount(balanceAmount);
            existingBalance.setOrderStatus(OrderStatusEnum.PENDING.getStatus());
            existingBalance.setExpireTime(LocalDateTime.now().plusHours(24));
            enrollmentOrderMapper.insert(existingBalance);
        }

        if (OrderStatusEnum.PAID.getStatus().equals(existingBalance.getOrderStatus())) {
            AppPayOrderRespVO resp = new AppPayOrderRespVO();
            resp.setPayOrderId(existingBalance.getPayOrderId());
            resp.setAmount(existingBalance.getAmount());
            return resp;
        }

        // 幂等：已创建 pay 单则直接返回
        if (existingBalance.getPayOrderId() != null) {
            AppPayOrderRespVO resp = new AppPayOrderRespVO();
            resp.setPayOrderId(existingBalance.getPayOrderId());
            resp.setAmount(existingBalance.getAmount());
            return resp;
        }

        // 调用 pay 模块创建支付单
        PayOrderCreateReqDTO payReq = new PayOrderCreateReqDTO();
        payReq.setAppKey(PAY_APP_KEY);
        payReq.setUserIp(getClientIP());
        payReq.setUserId(memberId);
        payReq.setUserType(cn.iocoder.yudao.framework.common.enums.UserTypeEnum.MEMBER.getValue());
        payReq.setMerchantOrderId(existingBalance.getOrderNo());
        payReq.setSubject("海员培训尾款");
        payReq.setBody(existingBalance.getOrderNo() + " 尾款");
        payReq.setPrice(existingBalance.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        payReq.setExpireTime(existingBalance.getExpireTime());
        Long payOrderId = payOrderApi.createOrder(payReq);

        enrollmentOrderMapper.updatePayOrderId(existingBalance.getId(), payOrderId);

        AppPayOrderRespVO resp = new AppPayOrderRespVO();
        resp.setPayOrderId(payOrderId);
        resp.setAmount(existingBalance.getAmount());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleBalancePaid(String merchantOrderId, Long payOrderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByOrderNo(merchantOrderId);
        if (order == null) {
            log.error("[handleBalancePaid] 订单不存在: {}", merchantOrderId);
            return;
        }

        if (payOrderId != null) {
            PayOrderRespDTO payOrderResp = payOrderApi.getOrder(payOrderId);
            if (payOrderResp != null) {
                BigDecimal paidAmount = BigDecimal.valueOf(payOrderResp.getPrice())
                        .divide(BigDecimal.valueOf(100));
                if (paidAmount.compareTo(order.getAmount()) != 0) {
                    log.error("[handleBalancePaid] 支付金额不匹配！orderNo={}, 期望={}元, 实际={}元",
                            merchantOrderId, order.getAmount(), paidAmount);
                    return;
                }
            }
        }

        int updated = enrollmentOrderMapper.updateStatusIfPending(order.getId(), OrderStatusEnum.PAID.getStatus());
        if (updated == 0) {
            log.warn("[handleBalancePaid] 订单已处理，跳过: {}", merchantOrderId);
            return;
        }

        EnrollmentOrderDO updateOrder = new EnrollmentOrderDO();
        updateOrder.setId(order.getId());
        updateOrder.setPayTime(LocalDateTime.now());
        enrollmentOrderMapper.updateById(updateOrder);

        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment == null) {
            log.error("[handleBalancePaid] 报名记录不存在, enrollmentId={}", order.getEnrollmentId());
            return;
        }

        // 更新报名状态 → IN_PROGRESS，累加已付金额
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.IN_PROGRESS.getStatus());
        updateEnrollment.setPaidAmount(enrollment.getPaidAmount().add(order.getAmount()));
        enrollmentMapper.updateById(updateEnrollment);

        // 尾款支付成功后触发佣金（有尾款情况下，在这里触发）
        commissionRecordService.triggerCommissionIfEligible(enrollment.getId());

        log.info("[handleBalancePaid] 尾款支付完成, enrollmentId={}", enrollment.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(AppRefundApplyReqVO reqVO, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(reqVO.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        String status = enrollment.getEnrollmentStatus();
        boolean refundable = EnrollmentStatusEnum.DEPOSITED.getStatus().equals(status)
                || EnrollmentStatusEnum.IN_PROGRESS.getStatus().equals(status);
        if (!refundable) {
            throw exception(REFUND_NOT_ALLOWED);
        }

        // 防重复：已有活跃申请则拒绝
        if (refundApplyMapper.selectActiveByEnrollmentId(reqVO.getEnrollmentId()) != null) {
            throw exception(REFUND_APPLY_DUPLICATE);
        }

        // 找需要退款的订单（优先尾款，其次定金）
        EnrollmentOrderDO targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollment.getId(), OrderTypeEnum.BALANCE.getType(), OrderStatusEnum.PAID.getStatus());
        if (targetOrder == null) {
            targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                    enrollment.getId(), OrderTypeEnum.DEPOSIT.getType(), OrderStatusEnum.PAID.getStatus());
        }
        if (targetOrder == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }

        RefundApplyDO applyDO = new RefundApplyDO();
        applyDO.setEnrollmentId(reqVO.getEnrollmentId());
        applyDO.setOrderId(targetOrder.getId());
        applyDO.setMemberId(memberId);
        applyDO.setApplyReason(reqVO.getApplyReason());
        applyDO.setApplyStatus("PENDING");
        refundApplyMapper.insert(applyDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyTransfer(AppTransferApplyReqVO reqVO, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(reqVO.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        String status = enrollment.getEnrollmentStatus();
        boolean transferable = EnrollmentStatusEnum.DEPOSITED.getStatus().equals(status)
                || EnrollmentStatusEnum.IN_PROGRESS.getStatus().equals(status);
        if (!transferable) {
            throw exception(ENROLLMENT_STATUS_ERROR);
        }

        if (refundApplyMapper.selectActiveByEnrollmentId(reqVO.getEnrollmentId()) != null) {
            throw exception(REFUND_APPLY_DUPLICATE);
        }

        EnrollmentOrderDO targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollment.getId(), OrderTypeEnum.DEPOSIT.getType(), OrderStatusEnum.PAID.getStatus());
        if (targetOrder == null) {
            throw exception(ENROLLMENT_ORDER_NOT_EXISTS);
        }

        RefundApplyDO applyDO = new RefundApplyDO();
        applyDO.setEnrollmentId(reqVO.getEnrollmentId());
        applyDO.setOrderId(targetOrder.getId());
        applyDO.setMemberId(memberId);
        applyDO.setApplyReason("【转课】" + reqVO.getApplyReason());
        applyDO.setApplyStatus("PENDING");
        refundApplyMapper.insert(applyDO);
    }

    @Override
    public AppRefundStatusRespVO getRefundStatus(Long enrollmentId, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }

        RefundApplyDO apply = refundApplyMapper.selectActiveByEnrollmentId(enrollmentId);
        if (apply == null) {
            return null;
        }

        AppRefundStatusRespVO vo = new AppRefundStatusRespVO();
        vo.setId(apply.getId());
        vo.setApplyStatus(apply.getApplyStatus());
        vo.setRefundAmount(apply.getRefundAmount());
        vo.setCreateTime(apply.getCreateTime());
        vo.setApproveTime(apply.getApproveTime());
        vo.setAdminRemark(apply.getAdminRemark());
        return vo;
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
