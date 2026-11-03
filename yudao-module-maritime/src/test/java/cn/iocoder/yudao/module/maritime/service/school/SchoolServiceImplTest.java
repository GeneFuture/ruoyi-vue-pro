package cn.iocoder.yudao.module.maritime.service.school;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.school.SchoolDO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SchoolServiceImpl} 的单元测试类
 *
 * 重点覆盖：学校名称唯一性、存在性校验、启用态过滤（供班期下拉）、分页过滤
 */
@Import(SchoolServiceImpl.class)
public class SchoolServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SchoolServiceImpl schoolService;

    private static SchoolSaveReqVO buildReqVO(String name) {
        SchoolSaveReqVO reqVO = new SchoolSaveReqVO();
        reqVO.setName(name);
        reqVO.setAddress("山东省青岛市市北区某某路 1 号");
        reqVO.setLatitude(new BigDecimal("36.0671170"));
        reqVO.setLongitude(new BigDecimal("120.3826650"));
        reqVO.setContactPhone("0532-88888888");
        reqVO.setCampusIntro("<p>学校概况</p>");
        reqVO.setClassroomIntro("<p>教学条件</p>");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setSortOrder(0);
        return reqVO;
    }

    private Long createSchool(String name) {
        return schoolService.createSchool(buildReqVO(name));
    }

    @Test
    public void testCreateSchool_success() {
        // 调用
        Long id = createSchool("青岛船员培训基地");

        // 断言
        SchoolDO school = schoolService.getSchool(id);
        assertNotNull(school);
        assertEquals("青岛船员培训基地", school.getName());
        assertEquals(0, new BigDecimal("36.067117").compareTo(school.getLatitude()));
        assertEquals(0, new BigDecimal("120.382665").compareTo(school.getLongitude()));
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), school.getStatus());
    }

    @Test
    public void testCreateSchool_nameDuplicate() {
        // 准备
        createSchool("青岛船员培训基地");

        // 调用 + 断言
        assertServiceException(() -> createSchool("青岛船员培训基地"), SCHOOL_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateSchool_sameNameAllowed() {
        // 准备：改名成自己原来的名字应当放行（唯一性校验要排除自身）
        Long id = createSchool("青岛船员培训基地");
        SchoolSaveReqVO reqVO = buildReqVO("青岛船员培训基地");
        reqVO.setId(id);
        reqVO.setContactPhone("0532-99999999");

        // 调用
        schoolService.updateSchool(reqVO);

        // 断言
        assertEquals("0532-99999999", schoolService.getSchool(id).getContactPhone());
    }

    @Test
    public void testUpdateSchool_nameDuplicateWithOther() {
        // 准备
        createSchool("青岛船员培训基地");
        Long otherId = createSchool("大连船员培训基地");
        SchoolSaveReqVO reqVO = buildReqVO("青岛船员培训基地");
        reqVO.setId(otherId);

        // 调用 + 断言
        assertServiceException(() -> schoolService.updateSchool(reqVO), SCHOOL_NAME_DUPLICATE);
    }

    @Test
    public void testUpdateSchool_notExists() {
        // 准备
        SchoolSaveReqVO reqVO = buildReqVO("不存在的学校");
        reqVO.setId(999999L);

        // 调用 + 断言
        assertServiceException(() -> schoolService.updateSchool(reqVO), SCHOOL_NOT_EXISTS);
    }

    @Test
    public void testDeleteSchool_notExists() {
        assertServiceException(() -> schoolService.deleteSchool(999999L), SCHOOL_NOT_EXISTS);
    }

    @Test
    public void testGetEnabledSchoolList_excludesDisabled() {
        // 准备：一所开启、一所关闭
        createSchool("青岛船员培训基地");
        Long disabledId = createSchool("已停用学校");
        SchoolSaveReqVO reqVO = buildReqVO("已停用学校");
        reqVO.setId(disabledId);
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        schoolService.updateSchool(reqVO);

        // 调用
        List<SchoolDO> list = schoolService.getEnabledSchoolList();

        // 断言：关闭的学校不出现在班期下拉中
        assertEquals(1, list.size());
        assertEquals("青岛船员培训基地", list.get(0).getName());
    }

    @Test
    public void testGetEnabledSchool_disabled() {
        // 准备：关闭状态的学校
        Long id = createSchool("已停用学校");
        SchoolSaveReqVO reqVO = buildReqVO("已停用学校");
        reqVO.setId(id);
        reqVO.setStatus(CommonStatusEnum.DISABLE.getStatus());
        schoolService.updateSchool(reqVO);

        // 调用 + 断言：新建班期引用已关闭的学校应报错
        assertServiceException(() -> schoolService.getEnabledSchool(id), SCHOOL_DISABLED);
    }

    @Test
    public void testGetEnabledSchool_notExists() {
        assertServiceException(() -> schoolService.getEnabledSchool(999999L), SCHOOL_NOT_EXISTS);
    }

    @Test
    public void testGetSchoolPage_filterByName() {
        // 准备
        createSchool("青岛船员培训基地");
        createSchool("大连船员培训基地");

        // 调用
        SchoolPageReqVO pageReqVO = new SchoolPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        pageReqVO.setName("青岛");
        PageResult<SchoolDO> pageResult = schoolService.getSchoolPage(pageReqVO);

        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals("青岛船员培训基地", pageResult.getList().get(0).getName());
    }

}
