package cn.iocoder.yudao.module.maritime.service.sessionVenue;

import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue.vo.SessionVenueSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue.SessionVenueMapper;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link SessionVenueServiceImpl} 的单元测试类
 *
 * 重点覆盖：学校信息（名称/地址/坐标/概况/教学/住宿）的落库与回读，
 * 尤其是经纬度作为可空 BigDecimal 的精度、未配置语义与取值范围的校验。
 */
@Import(SessionVenueServiceImpl.class)
public class SessionVenueServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SessionVenueServiceImpl sessionVenueService;

    @Resource
    private SessionVenueMapper sessionVenueMapper;

    /** 坐标列的 scale 是 7，不能直接用 BigDecimal.equals 比，否则 36.067117 与 36.0671170 判为不等 */
    private static void assertCoordinate(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static SessionVenueSaveReqVO buildReqVO(Long sessionId) {
        SessionVenueSaveReqVO reqVO = new SessionVenueSaveReqVO();
        reqVO.setSessionId(sessionId);
        reqVO.setCampusName("青岛船员培训基地");
        reqVO.setAddress("山东省青岛市市北区某某路 1 号");
        reqVO.setContactPhone("0532-88888888");
        reqVO.setCampusIntro("<p>学校概况</p>");
        reqVO.setClassroomIntro("<p>教学条件</p>");
        return reqVO;
    }

    @Test
    public void testSaveSessionVenue_create() {
        // 准备：带坐标的完整学校信息
        SessionVenueSaveReqVO reqVO = buildReqVO(1001L);
        reqVO.setLatitude(new BigDecimal("36.0671170"));
        reqVO.setLongitude(new BigDecimal("120.3826650"));
        reqVO.setAccommodationIntro("<p>住宿条件</p>");

        // 调用
        sessionVenueService.saveSessionVenue(reqVO);

        // 断言：坐标与三段富文本都落库且能原样回读
        SessionVenueDO venue = sessionVenueService.getSessionVenueBySessionId(1001L);
        assertNotNull(venue);
        assertEquals("青岛船员培训基地", venue.getCampusName());
        assertEquals("山东省青岛市市北区某某路 1 号", venue.getAddress());
        assertCoordinate("36.067117", venue.getLatitude());
        assertCoordinate("120.382665", venue.getLongitude());
        assertEquals("<p>学校概况</p>", venue.getCampusIntro());
        assertEquals("<p>教学条件</p>", venue.getClassroomIntro());
        assertEquals("<p>住宿条件</p>", venue.getAccommodationIntro());
    }

    @Test
    public void testSaveSessionVenue_createWithoutCoordinate() {
        // 准备：不填坐标（学校信息仍应可保存，住宿条件同为可空字段）
        SessionVenueSaveReqVO reqVO = buildReqVO(1002L);

        // 调用
        sessionVenueService.saveSessionVenue(reqVO);

        // 断言：坐标为 null，表示「未配置」，小程序端据此不展示导航入口
        SessionVenueDO venue = sessionVenueService.getSessionVenueBySessionId(1002L);
        assertNotNull(venue);
        assertNull(venue.getLatitude());
        assertNull(venue.getLongitude());
        assertNull(venue.getAccommodationIntro());
    }

    @Test
    public void testSaveSessionVenue_update() {
        // 准备：先建一条带坐标的记录
        SessionVenueSaveReqVO createReqVO = buildReqVO(1003L);
        createReqVO.setLatitude(new BigDecimal("36.0671170"));
        createReqVO.setLongitude(new BigDecimal("120.3826650"));
        sessionVenueService.saveSessionVenue(createReqVO);

        // 调用：同一班期再次保存，走更新分支（坐标改为新校区）
        SessionVenueSaveReqVO updateReqVO = buildReqVO(1003L);
        updateReqVO.setCampusName("青岛船员培训基地（新校区）");
        updateReqVO.setLatitude(new BigDecimal("36.1000000"));
        updateReqVO.setLongitude(new BigDecimal("120.4000000"));
        sessionVenueService.saveSessionVenue(updateReqVO);

        // 断言：按班期 1:1 更新，不产生第二条记录，坐标被覆盖
        List<SessionVenueDO> venues = sessionVenueMapper.selectList();
        assertEquals(1, venues.size());
        assertEquals("青岛船员培训基地（新校区）", venues.get(0).getCampusName());
        assertCoordinate("36.1", venues.get(0).getLatitude());
        assertCoordinate("120.4", venues.get(0).getLongitude());
    }

    @Test
    public void testSaveSessionVenue_coordinateOutOfRange() {
        // 准备：纬度超范围
        SessionVenueSaveReqVO latitudeReqVO = buildReqVO(1004L);
        latitudeReqVO.setLatitude(new BigDecimal("91"));

        // 调用 + 断言：被 @DecimalMax 拦截，不落库
        assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(latitudeReqVO));

        // 准备：经度超范围
        SessionVenueSaveReqVO longitudeReqVO = buildReqVO(1004L);
        longitudeReqVO.setLongitude(new BigDecimal("-180.0000001"));

        // 调用 + 断言
        assertThrows(ConstraintViolationException.class, () -> ValidationUtils.validate(longitudeReqVO));

        // 断言：非法数据没有被写入
        assertNull(sessionVenueService.getSessionVenueBySessionId(1004L));
    }

}
