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
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue.SessionVenueMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.maritime.mq.event.BalancePaidEvent;
import cn.iocoder.yudao.module.maritime.mq.event.DepositPaidEvent;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCancelledEvent;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCompletedEvent;
import cn.iocoder.yudao.module.maritime.mq.event.EnrollmentCreatedEvent;
import cn.iocoder.yudao.module.maritime.mq.event.RefundAppliedEvent;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponProcessResult;
import cn.iocoder.yudao.module.maritime.service.groupon.GrouponService;
import cn.iocoder.yudao.module.maritime.service.manualTask.ManualTaskService;
import cn.iocoder.yudao.module.maritime.service.studentInfo.StudentInfoService;
import cn.iocoder.yudao.module.pay.api.order.PayOrderApi;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderCreateReqDTO;
import cn.iocoder.yudao.module.pay.api.order.dto.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
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
import java.util.Objects;
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
    private SessionVenueMapper sessionVenueMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private GrouponService grouponService;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private StudentInfoService studentInfoService;

    @Resource
    private ManualTaskService manualTaskService;

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
        EnrollmentDO enrollment = validateEnrollmentExists(updateReqVO.getId());
        String oldStatus = enrollment.getEnrollmentStatus();
        String newStatus = updateReqVO.getEnrollmentStatus();

        // 通用编辑不允许把已付款报名直接改为取消：必须走退费/人工处理，避免款项未退、名额未归还
        if (EnrollmentStatusEnum.CANCELLED.getStatus().equals(newStatus)
                && !EnrollmentStatusEnum.CANCELLED.getStatus().equals(oldStatus)) {
            if (!EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus().equals(oldStatus)) {
                throw exception(ENROLLMENT_CANCEL_NOT_ALLOWED);
            }
            // 未付款报名走专用取消流程：关闭待支付订单、归还名额、发送取消通知
            self.cancelEnrollmentByAdmin(updateReqVO.getId(),
                    StrUtil.blankToDefault(updateReqVO.getCancelReason(), "管理员取消报名"));
            return;
        }

        EnrollmentDO updateObj = BeanUtils.toBean(updateReqVO, EnrollmentDO.class);
        enrollmentMapper.updateById(updateObj);

        // 管理员提前/补录结班：通用编辑把状态改为 COMPLETED 时补发完成通知
        if (EnrollmentStatusEnum.COMPLETED.getStatus().equals(newStatus)
                && !EnrollmentStatusEnum.COMPLETED.getStatus().equals(oldStatus)) {
            eventPublisher.publishEvent(new EnrollmentCompletedEvent(this, enrollment.getId(),
                    enrollment.getMemberId(), enrollment.getSessionId()));
        }
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
        // 定金为 0 或 ≥ 学费时按全款报名：报名单直接生成一笔全款订单，不再产生尾款
        boolean fullPayment = isFullPayment(feeConfig);
        String enrollmentNo = "EN" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 4).toUpperCase();
        EnrollmentDO enrollment = new EnrollmentDO();
        enrollment.setEnrollmentNo(enrollmentNo);
        enrollment.setMemberId(memberId);
        enrollment.setSessionId(createReqVO.getSessionId());
        enrollment.setRealName(createReqVO.getRealName());
        enrollment.setIdCard(createReqVO.getIdCard()); // 明文，EncryptTypeHandler 写库时自动加密
        enrollment.setPhone(createReqVO.getPhone());
        enrollment.setTotalAmount(feeConfig.getTuitionAmount());
        enrollment.setDepositAmountSnapshot(fullPayment ? BigDecimal.ZERO : feeConfig.getDepositAmount());
        enrollment.setBalanceAmount(fullPayment ? BigDecimal.ZERO
                : feeConfig.getTuitionAmount().subtract(feeConfig.getDepositAmount()));
        enrollment.setPaidAmount(BigDecimal.ZERO);
        enrollment.setEnrollmentStatus(EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus());
        // 尾款截止日期 = 开班日期 - N 天（全款报名无尾款）
        if (!fullPayment && session.getStartDate() != null && feeConfig.getBalanceDueDaysBeforeStart() != null) {
            enrollment.setBalanceDueDate(session.getStartDate().minusDays(feeConfig.getBalanceDueDaysBeforeStart()));
        }
        enrollmentMapper.insert(enrollment);
        // 首次报名时顺带补充学员信息，后续报名/资料页可直接带出
        studentInfoService.saveIfAbsent(memberId, createReqVO.getRealName(),
                createReqVO.getIdCard(), createReqVO.getPhone());
        eventPublisher.publishEvent(new EnrollmentCreatedEvent(
                this, enrollment.getId(), memberId, enrollment.getSessionId(), fullPayment));

        // Step 4.5: 处理拼团逻辑。定金统一按标准定金收取，拼团优惠在成团后冲抵尾款；
        // 全款报名没有定金，拼团不适用，忽略 joinGroupon
        BigDecimal depositAmount = feeConfig.getDepositAmount();
        if (Boolean.TRUE.equals(createReqVO.getJoinGroupon())) {
            if (fullPayment) {
                log.warn("[createEnrollmentForApp] 全款报名不支持拼团，已忽略 joinGroupon, sessionId={}",
                        enrollment.getSessionId());
            } else {
                GrouponProcessResult grouponResult = grouponService.joinOrCreateGroupon(
                        enrollment.getId(),
                        memberId,
                        createReqVO.getGrouponInviteCode(),
                        enrollment.getSessionId(),
                        feeConfig
                );
                enrollmentMapper.updateGrouponRecordId(enrollment.getId(), grouponResult.grouponRecordId());
            }
        }

        // Step 5: 创建报名订单（定金或全款，30分钟过期）
        String orderNo = "OD" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        EnrollmentOrderDO firstOrder = new EnrollmentOrderDO();
        firstOrder.setEnrollmentId(enrollment.getId());
        firstOrder.setOrderNo(orderNo);
        firstOrder.setOrderType(fullPayment ? OrderTypeEnum.FULL.getType() : OrderTypeEnum.DEPOSIT.getType());
        firstOrder.setAmount(fullPayment ? feeConfig.getTuitionAmount() : depositAmount);
        firstOrder.setOrderStatus(OrderStatusEnum.PENDING.getStatus());
        firstOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
        enrollmentOrderMapper.insert(firstOrder);

        // Step 6: CAS 原子递增报名人数（防超卖）
        int updated = courseSessionMapper.incrementEnrolledCount(session.getId(), session.getMaxStudents());
        if (updated == 0) {
            throw exception(SESSION_FULL);
        }

        AppEnrollmentCreateRespVO respVO = new AppEnrollmentCreateRespVO();
        respVO.setEnrollmentId(enrollment.getId());
        respVO.setOrderId(firstOrder.getId());
        respVO.setOrderNo(firstOrder.getOrderNo());
        respVO.setDepositAmount(firstOrder.getAmount());
        respVO.setExpireTime(firstOrder.getExpireTime());
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
        // 城市在场地表上，一次查完再按班期取
        Map<Long, SessionVenueDO> venueMap = sessionVenueMapper.selectMapBySessionIds(sessionIds);
        // 费用配置用于判断全款报名
        Map<Long, SessionFeeConfigDO> feeConfigMap = sessionFeeConfigMapper.selectBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionFeeConfigDO::getSessionId, f -> f, (a, b) -> a));

        return enrollments.stream().map(e -> {
            AppEnrollmentListRespVO vo = BeanUtils.toBean(e, AppEnrollmentListRespVO.class);
            CourseSessionDO session = sessionMap.get(e.getSessionId());
            if (session != null) {
                vo.setSessionCode(session.getSessionCode());
                vo.setSessionName(session.getSessionName());
                vo.setStartDate(session.getStartDate());
                SessionVenueDO venue = venueMap.get(e.getSessionId());
                vo.setCity(venue != null ? venue.getCity() : null);
            }
            vo.setFullPayment(isFullPayment(feeConfigMap.get(e.getSessionId())));
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

        // 拼团优惠：仅成团后生效，已包含在 balanceAmount 的抵扣里
        SessionFeeConfigDO feeConfig = sessionFeeConfigMapper.selectBySessionId(enrollment.getSessionId());
        if (feeConfig != null) {
            vo.setGrouponDiscountAmount(resolveGrouponDiscount(enrollment, feeConfig));
            vo.setFullPayment(isFullPayment(feeConfig));
        }

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
            SessionVenueDO venue = sessionVenueMapper.selectBySessionId(session.getId());
            vo.setCity(venue != null ? venue.getCity() : null);
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

        // 人工处理信息
        if (EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus().equals(enrollment.getEnrollmentStatus())) {
            ManualTaskDO manualTask = manualTaskService.getOpenTask(id);
            if (manualTask != null) {
                vo.setManualTaskId(manualTask.getId());
                vo.setManualTaskType(manualTask.getTaskType());
                vo.setManualTaskStatus(manualTask.getStatus());
                vo.setManualTaskReason(manualTask.getReason());
            }
        }

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
        updateObj.setCancelReason("用户取消报名");
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

        eventPublisher.publishEvent(new EnrollmentCancelledEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(), "用户取消报名"));
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
        // 已支付的报名必须走退费流程（人工处理），避免直接取消导致款项未退
        if (!EnrollmentStatusEnum.PENDING_DEPOSIT.getStatus().equals(status)) {
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

        eventPublisher.publishEvent(new EnrollmentCancelledEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(),
                "管理员取消报名：" + StrUtil.blankToDefault(cancelReason, "未说明原因")));
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
        boolean fullPayment = OrderTypeEnum.FULL.getType().equals(order.getOrderType());
        payReq.setSubject(fullPayment ? "海员培训全款" : "海员培训定金");
        payReq.setBody(order.getOrderNo() + (fullPayment ? " 全款" : " 定金"));
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
    public void handlePayNotify(String merchantOrderId, Long payOrderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByOrderNo(merchantOrderId);
        if (order == null) {
            log.error("[handlePayNotify] 订单不存在: {}", merchantOrderId);
            return;
        }
        // pay 模块一个应用只有一个回调地址，这里按订单类型分流：
        // 定金/全款 → handleDepositPaid；尾款 → handleBalancePaid
        if (OrderTypeEnum.BALANCE.getType().equals(order.getOrderType())) {
            self.handleBalancePaid(merchantOrderId, payOrderId);
        } else {
            self.handleDepositPaid(merchantOrderId, payOrderId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDepositPaid(String merchantOrderId, Long payOrderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByOrderNo(merchantOrderId);
        if (order == null) {
            log.error("[handleDepositPaid] 订单不存在: {}", merchantOrderId);
            return;
        }

        // 校验支付单（防止伪造回调 / 用其它支付单顶替），payOrderId 为 null 时跳过（管理员手动确认场景）
        if (payOrderId != null) {
            PayOrderRespDTO payOrderResp = payOrderApi.getOrder(payOrderId);
            // 必须存在、已支付、且商户订单号与本报名订单匹配
            if (payOrderResp == null
                    || !PayOrderStatusEnum.isSuccess(payOrderResp.getStatus())
                    || !Objects.equals(payOrderResp.getMerchantOrderId(), order.getOrderNo())) {
                log.error("[handleDepositPaid] 支付单无效，拒绝处理！orderNo={}, payOrderId={}, payOrder={}",
                        merchantOrderId, payOrderId, payOrderResp);
                return;
            }
            BigDecimal paidAmount = BigDecimal.valueOf(payOrderResp.getPrice())
                    .divide(BigDecimal.valueOf(100));
            if (paidAmount.compareTo(order.getAmount()) != 0) {
                log.error("[handleDepositPaid] 支付金额不匹配，暂停处理！orderNo={}, 期望={}元, 实际={}元",
                        merchantOrderId, order.getAmount(), paidAmount);
                return;
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

        // 更新报名状态：定金 → DEPOSITED；全款 → PAID_OFF；累加已付金额
        boolean fullPayment = OrderTypeEnum.FULL.getType().equals(order.getOrderType());
        EnrollmentDO enrollment = enrollmentMapper.selectById(order.getEnrollmentId());
        if (enrollment == null) {
            log.error("[handleDepositPaid] 报名记录不存在, enrollmentId={}", order.getEnrollmentId());
            return;
        }
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(fullPayment
                ? EnrollmentStatusEnum.PAID_OFF.getStatus() : EnrollmentStatusEnum.DEPOSITED.getStatus());
        updateEnrollment.setPaidAmount(enrollment.getPaidAmount().add(order.getAmount()));
        enrollmentMapper.updateById(updateEnrollment);

        eventPublisher.publishEvent(new DepositPaidEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(),
                order.getAmount(), fullPayment));

        // 处理拼团支付（全款不参与拼团；定金支付后记录并检查是否成团）
        if (!fullPayment) {
            grouponService.handleMemberPaid(enrollment.getId());
        }
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
            updateEnrollment.setCancelReason("定金订单超时未支付，报名已取消");
            enrollmentMapper.updateById(updateEnrollment);
            courseSessionMapper.decrementEnrolledCount(enrollment.getSessionId());

            eventPublisher.publishEvent(new EnrollmentCancelledEvent(
                    this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(),
                    "定金订单超时未支付，报名已取消"));
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
        // 与支付回调保持一致：尾款订单走尾款处理，定金/全款订单走定金处理
        // 管理员手动确认属于线下收款，传 null 跳过 pay 单状态校验
        // 通过 self 代理调用，确保 @Transactional 生效（避免 Spring AOP 自调用陷阱）
        if (OrderTypeEnum.BALANCE.getType().equals(order.getOrderType())) {
            self.handleBalancePaid(order.getOrderNo(), null);
        } else {
            self.handleDepositPaid(order.getOrderNo(), null);
        }
    }

    // ========== 小程序端 T07 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPayOrderRespVO payBalance(AppPayBalanceReqVO reqVO, Long memberId) {
        EnrollmentDO enrollment = enrollmentMapper.selectById(reqVO.getEnrollmentId());
        if (enrollment == null || !enrollment.getMemberId().equals(memberId)) {
            throw exception(ENROLLMENT_NOT_EXISTS);
        }
        String enrollmentStatus = enrollment.getEnrollmentStatus();
        boolean canPayBalance = EnrollmentStatusEnum.DEPOSITED.getStatus().equals(enrollmentStatus)
                || EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus().equals(enrollmentStatus);
        if (!canPayBalance) {
            throw exception(ENROLLMENT_STATUS_ERROR);
        }
        // 人工处理中只允许「尾款逾期」的报名继续补缴；退费等其他人工单不允许支付
        if (EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus().equals(enrollmentStatus)) {
            ManualTaskDO openTask = manualTaskService.getOpenTask(enrollment.getId());
            if (openTask == null
                    || !ManualTaskTypeEnum.BALANCE_OVERDUE.getType().equals(openTask.getTaskType())) {
                throw exception(ENROLLMENT_STATUS_ERROR);
            }
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
        if (isFullPayment(feeConfig)) {
            throw exception(BALANCE_NOT_REQUIRED);
        }
        // 尾款 = 学费 - 拼团优惠（仅成团生效）- 实付定金
        BigDecimal balanceAmount = feeConfig.getTuitionAmount()
                .subtract(resolveGrouponDiscount(enrollment, feeConfig))
                .subtract(depositOrder.getAmount());
        if (balanceAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(BALANCE_NOT_REQUIRED);
        }
        // 防重复创建：已有 PENDING 或 PAID 的尾款订单则复用
        EnrollmentOrderDO existingBalance = enrollmentOrderMapper.selectByEnrollmentIdAndType(
                enrollment.getId(), OrderTypeEnum.BALANCE.getType());

        // 已生成微信支付单时金额无法安全变更：保持原金额，避免出现「页面显示新价、实际支付旧价」
        BigDecimal payableAmount = balanceAmount;
        if (existingBalance != null
                && OrderStatusEnum.PENDING.getStatus().equals(existingBalance.getOrderStatus())
                && existingBalance.getPayOrderId() != null
                && existingBalance.getAmount().compareTo(balanceAmount) != 0) {
            log.info("[payBalance] 尾款订单已生成支付单，保持原金额: enrollmentId={}, orderAmount={}, currentAmount={}",
                    enrollment.getId(), existingBalance.getAmount(), balanceAmount);
            payableAmount = existingBalance.getAmount();
        }

        // 成团后尾款金额会变化，顺带修正报名表里的快照，保证详情页/提醒口径一致
        if (enrollment.getBalanceAmount() == null
                || enrollment.getBalanceAmount().compareTo(payableAmount) != 0) {
            EnrollmentDO balanceUpdate = new EnrollmentDO();
            balanceUpdate.setId(enrollment.getId());
            balanceUpdate.setBalanceAmount(payableAmount);
            enrollmentMapper.updateById(balanceUpdate);
        }

        if (existingBalance == null || OrderStatusEnum.CLOSED.getStatus().equals(existingBalance.getOrderStatus())) {
            String orderNo = "OB" + LocalDateTime.now().format(ORDER_NO_FMT) + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
            existingBalance = new EnrollmentOrderDO();
            existingBalance.setEnrollmentId(enrollment.getId());
            existingBalance.setOrderNo(orderNo);
            existingBalance.setOrderType(OrderTypeEnum.BALANCE.getType());
            existingBalance.setAmount(payableAmount);
            existingBalance.setOrderStatus(OrderStatusEnum.PENDING.getStatus());
            existingBalance.setExpireTime(LocalDateTime.now().plusHours(24));
            enrollmentOrderMapper.insert(existingBalance);
        } else if (OrderStatusEnum.PENDING.getStatus().equals(existingBalance.getOrderStatus())
                && existingBalance.getPayOrderId() == null
                && existingBalance.getAmount().compareTo(payableAmount) != 0) {
            // 尚未生成支付单，直接按最新尾款金额更新订单
            EnrollmentOrderDO amountUpdate = new EnrollmentOrderDO();
            amountUpdate.setId(existingBalance.getId());
            amountUpdate.setAmount(payableAmount);
            enrollmentOrderMapper.updateById(amountUpdate);
            existingBalance.setAmount(payableAmount);
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

    /** 定金为 0 或 ≥ 学费时按全款报名：报名时一次付清，不再产生尾款 */
    private boolean isFullPayment(SessionFeeConfigDO feeConfig) {
        if (feeConfig == null || feeConfig.getTuitionAmount() == null
                || feeConfig.getDepositAmount() == null) {
            return false;
        }
        return feeConfig.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0
                || feeConfig.getDepositAmount().compareTo(feeConfig.getTuitionAmount()) >= 0;
    }

    /**
     * 拼团优惠只在「已成团」时生效；进行中/已降级都按原价。
     * 定金统一按标准定金收取，优惠通过尾款抵扣体现。
     */
    private BigDecimal resolveGrouponDiscount(EnrollmentDO enrollment, SessionFeeConfigDO feeConfig) {
        if (enrollment.getGrouponRecordId() == null
                || feeConfig.getGrouponDiscountAmount() == null) {
            return BigDecimal.ZERO;
        }
        GrouponRecordDO record = grouponRecordMapper.selectById(enrollment.getGrouponRecordId());
        if (record != null && "SUCCESS".equals(record.getGrouponStatus())) {
            return feeConfig.getGrouponDiscountAmount();
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleBalancePaid(String merchantOrderId, Long payOrderId) {
        EnrollmentOrderDO order = enrollmentOrderMapper.selectByOrderNo(merchantOrderId);
        if (order == null) {
            log.error("[handleBalancePaid] 订单不存在: {}", merchantOrderId);
            return;
        }

        // 校验支付单（防止伪造回调 / 用其它支付单顶替）
        if (payOrderId != null) {
            PayOrderRespDTO payOrderResp = payOrderApi.getOrder(payOrderId);
            // 必须存在、已支付、且商户订单号与本报名订单匹配
            if (payOrderResp == null
                    || !PayOrderStatusEnum.isSuccess(payOrderResp.getStatus())
                    || !Objects.equals(payOrderResp.getMerchantOrderId(), order.getOrderNo())) {
                log.error("[handleBalancePaid] 支付单无效，拒绝处理！orderNo={}, payOrderId={}, payOrder={}",
                        merchantOrderId, payOrderId, payOrderResp);
                return;
            }
            BigDecimal paidAmount = BigDecimal.valueOf(payOrderResp.getPrice())
                    .divide(BigDecimal.valueOf(100));
            if (paidAmount.compareTo(order.getAmount()) != 0) {
                log.error("[handleBalancePaid] 支付金额不匹配！orderNo={}, 期望={}元, 实际={}元",
                        merchantOrderId, order.getAmount(), paidAmount);
                return;
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

        // 更新报名状态 → PAID_OFF（已付清/待开班），尾款清零，累加已付金额
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.PAID_OFF.getStatus());
        updateEnrollment.setPaidAmount(enrollment.getPaidAmount().add(order.getAmount()));
        updateEnrollment.setBalanceAmount(BigDecimal.ZERO);
        enrollmentMapper.updateById(updateEnrollment);

        eventPublisher.publishEvent(new BalancePaidEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId(), order.getAmount()));

        // 拼团未成团时提前付清尾款：结束拼团，避免成团后再次调整尾款
        if (enrollment.getGrouponRecordId() != null) {
            grouponService.degradeGroupon(enrollment.getGrouponRecordId(), "成员提前付清尾款，拼团结束");
        }

        // 尾款逾期的人工单自动关闭
        manualTaskService.closeOpenTask(enrollment.getId(), ManualTaskTypeEnum.BALANCE_OVERDUE.getType(),
                "学员已补缴尾款", 0L);

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
                || EnrollmentStatusEnum.PAID_OFF.getStatus().equals(status);
        if (!refundable) {
            throw exception(REFUND_NOT_ALLOWED);
        }

        // 防重复：已有活跃申请则拒绝
        if (refundApplyMapper.selectActiveByEnrollmentId(reqVO.getEnrollmentId()) != null) {
            throw exception(REFUND_APPLY_DUPLICATE);
        }

        // 找需要退款的订单（优先尾款，其次定金；全款报名没有定金，取 FULL 订单）
        EnrollmentOrderDO targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                enrollment.getId(), OrderTypeEnum.BALANCE.getType(), OrderStatusEnum.PAID.getStatus());
        if (targetOrder == null) {
            targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                    enrollment.getId(), OrderTypeEnum.DEPOSIT.getType(), OrderStatusEnum.PAID.getStatus());
        }
        if (targetOrder == null) {
            targetOrder = enrollmentOrderMapper.selectByEnrollmentIdAndTypeAndStatus(
                    enrollment.getId(), OrderTypeEnum.FULL.getType(), OrderStatusEnum.PAID.getStatus());
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

        // 退费统一转人工处理，并生成人工处理单
        manualTaskService.createTaskIfAbsent(enrollment.getId(), memberId, targetOrder.getId(),
                "REFUND", applyDO.getId(), ManualTaskTypeEnum.REFUND_APPLY.getType(),
                "学员申请退费：" + reqVO.getApplyReason());
        EnrollmentDO updateEnrollment = new EnrollmentDO();
        updateEnrollment.setId(enrollment.getId());
        updateEnrollment.setEnrollmentStatus(EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus());
        enrollmentMapper.updateById(updateEnrollment);

        eventPublisher.publishEvent(new RefundAppliedEvent(
                this, enrollment.getId(), enrollment.getMemberId(), enrollment.getSessionId()));
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
