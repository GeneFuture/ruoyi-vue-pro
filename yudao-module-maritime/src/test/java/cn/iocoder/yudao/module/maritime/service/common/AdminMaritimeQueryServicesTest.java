package cn.iocoder.yudao.module.maritime.service.common;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollmentOrder.vo.EnrollmentOrderRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponMember.vo.GrouponMemberRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.grouponRecord.vo.GrouponRecordRespVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.manualTask.vo.ManualTaskRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollmentOrder.EnrollmentOrderDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponMember.GrouponMemberDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.manualTask.ManualTaskDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollmentOrder.EnrollmentOrderMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponMember.GrouponMemberMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.manualTask.ManualTaskMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskStatusEnum;
import cn.iocoder.yudao.module.maritime.enums.ManualTaskTypeEnum;
import cn.iocoder.yudao.module.maritime.service.enrollmentOrder.AdminEnrollmentOrderQueryServiceImpl;
import cn.iocoder.yudao.module.maritime.service.grouponMember.AdminGrouponMemberQueryServiceImpl;
import cn.iocoder.yudao.module.maritime.service.grouponRecord.AdminGrouponRecordQueryServiceImpl;
import cn.iocoder.yudao.module.maritime.service.manualTask.AdminManualTaskQueryServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 管理后台列表展示辅助查询的单元测试
 *
 * 覆盖：支付订单、人工处理、拼团记录、拼团成员把内部 ID 组装为可读名称。
 */
@Import({AdminMaritimeQuerySupport.class, AdminEnrollmentOrderQueryServiceImpl.class,
        AdminManualTaskQueryServiceImpl.class, AdminGrouponRecordQueryServiceImpl.class,
        AdminGrouponMemberQueryServiceImpl.class})
public class AdminMaritimeQueryServicesTest extends BaseDbUnitTest {

    @Resource
    private AdminEnrollmentOrderQueryServiceImpl orderQueryService;
    @Resource
    private AdminManualTaskQueryServiceImpl manualTaskQueryService;
    @Resource
    private AdminGrouponRecordQueryServiceImpl grouponRecordQueryService;
    @Resource
    private AdminGrouponMemberQueryServiceImpl grouponMemberQueryService;

    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CourseSessionMapper sessionMapper;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private EnrollmentOrderMapper orderMapper;
    @Resource
    private ManualTaskMapper manualTaskMapper;
    @Resource
    private RefundApplyMapper refundApplyMapper;
    @Resource
    private GrouponRecordMapper grouponRecordMapper;
    @Resource
    private GrouponMemberMapper grouponMemberMapper;

    @Test
    public void testOrderPage() {
        insertData();
        EnrollmentOrderPageReqVO reqVO = new EnrollmentOrderPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setRealName("张");

        PageResult<EnrollmentOrderRespVO> page = orderQueryService.getEnrollmentOrderPage(reqVO);

        assertEquals(1, page.getTotal());
        EnrollmentOrderRespVO row = page.getList().get(0);
        assertEquals("张三", row.getRealName());
        assertEquals("基本安全培训", row.getCourseName());
        assertEquals("A班", row.getSessionName());
        assertEquals("EN202611020001", row.getEnrollmentNo());
    }

    @Test
    public void testManualTaskPage_filterByStudentName() {
        insertData();
        ManualTaskPageReqVO reqVO = new ManualTaskPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setRealName("张");

        PageResult<ManualTaskRespVO> page = manualTaskQueryService.getManualTaskPage(reqVO);

        assertEquals(1, page.getTotal());
        ManualTaskRespVO row = page.getList().get(0);
        assertEquals("张三", row.getRealName());
        assertEquals("基本安全培训", row.getCourseName());
        assertEquals(new BigDecimal("500.00"), row.getRefundAmount());
        assertEquals("PENDING", row.getApplyStatus());
    }

    @Test
    public void testGrouponRecordPage() {
        insertData();
        GrouponRecordPageReqVO reqVO = new GrouponRecordPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<GrouponRecordRespVO> page = grouponRecordQueryService.getGrouponRecordPage(reqVO);

        assertEquals(1, page.getTotal());
        GrouponRecordRespVO row = page.getList().get(0);
        assertEquals("GRP-TEST-001", row.getInviteCode());
        assertEquals("张三", row.getInitiatorRealName());
        assertEquals("基本安全培训", row.getCourseName());
    }

    @Test
    public void testGrouponMemberPage_filterByInviteCode() {
        insertData();
        GrouponMemberPageReqVO reqVO = new GrouponMemberPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);
        reqVO.setInviteCode("GRP-TEST-001");

        PageResult<GrouponMemberRespVO> page = grouponMemberQueryService.getGrouponMemberPage(reqVO);

        assertEquals(1, page.getTotal());
        GrouponMemberRespVO row = page.getList().get(0);
        assertEquals("GRP-TEST-001", row.getInviteCode());
        assertEquals("张三", row.getRealName());
        assertEquals("基本安全培训", row.getCourseName());
    }

    private void insertData() {
        CourseDO course = CourseDO.builder()
                .name("基本安全培训").certificateType("STCW").status(1).sortOrder(0).build();
        courseMapper.insert(course);
        CourseSessionDO session = CourseSessionDO.builder()
                .courseId(course.getId()).sessionCode("STCW-202607-001").sessionName("A班")
                .durationDays(5).startDate(LocalDate.of(2026, 11, 2)).endDate(LocalDate.of(2026, 11, 6))
                .maxStudents(30).enrolledCount(1).sessionStatus("OPEN").build();
        sessionMapper.insert(session);
        EnrollmentDO enrollment = EnrollmentDO.builder()
                .enrollmentNo("EN202611020001").memberId(100L).sessionId(session.getId())
                .realName("张三").idCard("110101199002022345").phone("13800000000")
                .totalAmount(new BigDecimal("3800.00")).depositAmountSnapshot(new BigDecimal("500.00"))
                .balanceAmount(new BigDecimal("3300.00")).paidAmount(new BigDecimal("500.00"))
                .enrollmentStatus("DEPOSITED").build();
        enrollmentMapper.insert(enrollment);

        EnrollmentOrderDO order = EnrollmentOrderDO.builder()
                .enrollmentId(enrollment.getId()).orderNo("ORDER-001").orderType("DEPOSIT")
                .amount(new BigDecimal("500.00")).orderStatus("PAID").build();
        orderMapper.insert(order);

        RefundApplyDO refundApply = RefundApplyDO.builder()
                .enrollmentId(enrollment.getId()).orderId(order.getId()).memberId(100L)
                .applyReason("临时有事").refundAmount(new BigDecimal("500.00")).applyStatus("PENDING").build();
        refundApplyMapper.insert(refundApply);
        ManualTaskDO task = ManualTaskDO.builder()
                .taskNo("MT001").enrollmentId(enrollment.getId()).memberId(100L)
                .orderId(order.getId()).sourceType("REFUND").sourceId(refundApply.getId())
                .taskType(ManualTaskTypeEnum.REFUND_APPLY.getType())
                .status(ManualTaskStatusEnum.PENDING.getStatus()).priority(0)
                .reason("学员申请退费：临时有事").build();
        manualTaskMapper.insert(task);

        GrouponRecordDO groupon = GrouponRecordDO.builder()
                .sessionId(session.getId()).inviteCode("GRP-TEST-001")
                .initiatorEnrollmentId(enrollment.getId()).requiredCount(3).currentCount(1)
                .grouponStatus("IN_PROGRESS").expireTime(LocalDateTime.now().plusDays(1)).build();
        grouponRecordMapper.insert(groupon);
        GrouponMemberDO member = GrouponMemberDO.builder()
                .grouponRecordId(groupon.getId()).enrollmentId(enrollment.getId()).memberId(100L)
                .joinTime(LocalDateTime.now()).memberStatus("ACTIVE").build();
        grouponMemberMapper.insert(member);

    }

}
