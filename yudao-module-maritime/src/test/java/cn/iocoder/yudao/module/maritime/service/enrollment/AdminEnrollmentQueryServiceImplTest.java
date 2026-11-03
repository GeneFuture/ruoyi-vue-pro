package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AdminEnrollmentQueryServiceImpl} 的单元测试类
 *
 * 重点覆盖：报名记录列表/详情把内部 ID 组装成课程、班期、拼团可读信息。
 */
@Import(AdminEnrollmentQueryServiceImpl.class)
public class AdminEnrollmentQueryServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminEnrollmentQueryServiceImpl adminEnrollmentQueryService;
    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private CourseSessionMapper courseSessionMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Test
    public void testGetEnrollment_success() {
        EnrollmentDO enrollment = insertEnrollment();

        EnrollmentRespVO detail = adminEnrollmentQueryService.getEnrollment(enrollment.getId());

        assertNotNull(detail);
        assertEquals("基本安全培训", detail.getCourseName());
        assertEquals("STCW-202607-001", detail.getSessionCode());
        assertEquals("A班", detail.getSessionName());
        assertEquals(LocalDate.of(2026, 11, 2), detail.getSessionStartDate());
        assertEquals("GRP-TEST-001", detail.getGrouponInviteCode());
        assertEquals("IN_PROGRESS", detail.getGrouponStatus());
        assertEquals("1101**********2345", detail.getIdCardMasked());
    }

    @Test
    public void testGetEnrollmentPage_success() {
        insertEnrollment();
        EnrollmentPageReqVO reqVO = new EnrollmentPageReqVO();
        reqVO.setPageNo(1);
        reqVO.setPageSize(10);

        PageResult<EnrollmentRespVO> page = adminEnrollmentQueryService.getEnrollmentPage(reqVO);

        assertEquals(1, page.getTotal());
        EnrollmentRespVO row = page.getList().get(0);
        assertEquals("张三", row.getRealName());
        assertEquals("基本安全培训", row.getCourseName());
        assertEquals("GRP-TEST-001", row.getGrouponInviteCode());
    }

    private EnrollmentDO insertEnrollment() {
        CourseDO course = CourseDO.builder()
                .name("基本安全培训")
                .certificateType("STCW")
                .status(1)
                .sortOrder(0)
                .build();
        courseMapper.insert(course);

        CourseSessionDO session = CourseSessionDO.builder()
                .courseId(course.getId())
                .sessionCode("STCW-202607-001")
                .sessionName("A班")
                .durationDays(5)
                .startDate(LocalDate.of(2026, 11, 2))
                .endDate(LocalDate.of(2026, 11, 6))
                .maxStudents(30)
                .enrolledCount(1)
                .sessionStatus("OPEN")
                .build();
        courseSessionMapper.insert(session);

        GrouponRecordDO groupon = GrouponRecordDO.builder()
                .sessionId(session.getId())
                .inviteCode("GRP-TEST-001")
                .initiatorEnrollmentId(1L)
                .requiredCount(3)
                .currentCount(1)
                .grouponStatus("IN_PROGRESS")
                .expireTime(LocalDateTime.now().plusDays(1))
                .build();
        grouponRecordMapper.insert(groupon);

        EnrollmentDO enrollment = EnrollmentDO.builder()
                .enrollmentNo("EN202611020001")
                .memberId(100L)
                .sessionId(session.getId())
                .grouponRecordId(groupon.getId())
                .realName("张三")
                .idCard("110101199002022345")
                .phone("13800000000")
                .totalAmount(new BigDecimal("3800.00"))
                .depositAmountSnapshot(new BigDecimal("500.00"))
                .balanceAmount(new BigDecimal("3300.00"))
                .paidAmount(new BigDecimal("500.00"))
                .balanceDueDate(LocalDate.of(2026, 10, 30))
                .enrollmentStatus("DEPOSITED")
                .build();
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

}
