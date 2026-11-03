package cn.iocoder.yudao.module.maritime.service.session;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.service.feeTemplate.FeeTemplateService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CourseSessionServiceImpl} 的单元测试类
 *
 * 重点覆盖：班期编号由系统自动生成、创建后不可修改、序号不复用（含已软删除的班期）。
 */
@Import(CourseSessionServiceImpl.class)
public class CourseSessionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CourseSessionServiceImpl courseSessionService;
    @Resource
    private CourseSessionMapper courseSessionMapper;
    @Resource
    private CourseMapper courseMapper;

    @MockitoBean
    private FeeTemplateService feeTemplateService;

    private Long createCourse() {
        CourseDO course = new CourseDO();
        course.setName("测试课程");
        course.setCertificateType("适任培训");
        course.setStatus(1);
        course.setSortOrder(0);
        courseMapper.insert(course);
        return course.getId();
    }

    private static String codePrefix(Long courseId) {
        return "C" + Long.toString(courseId, 36).toUpperCase(Locale.ROOT) + "-202610-";
    }

    private CourseSessionSaveReqVO buildReqVO(Long courseId) {
        CourseSessionSaveReqVO reqVO = new CourseSessionSaveReqVO();
        reqVO.setCourseId(courseId);
        reqVO.setStartDate(LocalDate.of(2026, 10, 15));
        reqVO.setEndDate(LocalDate.of(2026, 11, 15));
        reqVO.setDurationDays(30);
        reqVO.setMaxStudents(30);
        reqVO.setSessionStatus("DRAFT");
        return reqVO;
    }

    @Test
    public void testCreateCourseSession_generateSessionCode() {
        Long courseId = createCourse();
        String prefix = codePrefix(courseId);

        // 前端传入的编号应被忽略，由系统按课程 + 开班月份生成
        CourseSessionSaveReqVO reqVO = buildReqVO(courseId);
        reqVO.setSessionCode("MANUAL-CODE");
        Long id01 = courseSessionService.createCourseSession(reqVO);
        assertEquals(prefix + "001", courseSessionMapper.selectById(id01).getSessionCode());

        // 同课程同月份递增
        Long id02 = courseSessionService.createCourseSession(buildReqVO(courseId));
        assertEquals(prefix + "002", courseSessionMapper.selectById(id02).getSessionCode());
    }

    @Test
    public void testCreateCourseSession_sequenceNotReusedAfterDelete() {
        Long courseId = createCourse();
        String prefix = codePrefix(courseId);

        Long id01 = courseSessionService.createCourseSession(buildReqVO(courseId));
        assertEquals(prefix + "001", courseSessionMapper.selectById(id01).getSessionCode());

        // 软删除后，编号不能被重新使用，否则同编号第二次删除会撞 (session_code, deleted) 唯一键
        courseSessionService.deleteCourseSession(id01);
        Long id02 = courseSessionService.createCourseSession(buildReqVO(courseId));
        assertEquals(prefix + "002", courseSessionMapper.selectById(id02).getSessionCode());
    }

    @Test
    public void testUpdateCourseSession_sessionCodeImmutable() {
        Long courseId = createCourse();
        String code = codePrefix(courseId) + "001";
        Long id = courseSessionService.createCourseSession(buildReqVO(courseId));
        assertEquals(code, courseSessionMapper.selectById(id).getSessionCode());

        // 即使前端回传了不同的编号，也不允许修改
        CourseSessionSaveReqVO updateReqVO = buildReqVO(courseId);
        updateReqVO.setId(id);
        updateReqVO.setSessionCode("HACKED-CODE");
        courseSessionService.updateCourseSession(updateReqVO);
        assertEquals(code, courseSessionMapper.selectById(id).getSessionCode());
    }

}
