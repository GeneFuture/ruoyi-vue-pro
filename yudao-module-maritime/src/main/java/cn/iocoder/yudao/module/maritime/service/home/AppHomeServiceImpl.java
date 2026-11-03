package cn.iocoder.yudao.module.maritime.service.home;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.maritime.controller.app.home.vo.AppHomeRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue.SessionVenueMapper;
import cn.iocoder.yudao.module.maritime.enums.EnrollmentStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小程序首页聚合 Service 实现类
 *
 * 聚合平台统计、近期开班两类数据，
 * 结果整体写入 Redis 缓存，避免高频访问下多次 COUNT/列表查询打库。
 *
 * @author Gene Ye
 */
@Slf4j
@Service
public class AppHomeServiceImpl implements AppHomeService {

    /**
     * 缓存 Key 前缀，按租户隔离：maritime:home:{tenantId}
     */
    private static final String REDIS_KEY_PREFIX = "maritime:home:";

    /**
     * 缓存时长（分钟）：首页允许分钟级延迟
     */
    private static final long CACHE_MINUTES = 5;

    /**
     * 近期开窗展示的天数窗口
     */
    private static final int UPCOMING_DAYS = 30;

    /**
     * 近期开班最多展示条数
     */
    private static final int UPCOMING_LIMIT = 6;

    @Resource
    private EnrollmentMapper enrollmentMapper;
    @Resource
    private CourseSessionMapper courseSessionMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;
    @Resource
    private SessionVenueMapper sessionVenueMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AppHomeRespVO getHome() {
        // 1. 优先读缓存
        String cacheKey = REDIS_KEY_PREFIX + TenantContextHolder.getTenantId();
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cached)) {
                AppHomeRespVO cachedVO = JsonUtils.parseObject(cached, AppHomeRespVO.class);
                if (cachedVO != null) {
                    return cachedVO;
                }
            }
        } catch (Exception e) {
            log.warn("[getHome] 读取首页缓存失败，降级查库", e);
        }

        // 2. 缓存未命中，组装数据
        AppHomeRespVO vo = buildHome();

        // 3. 回写缓存（失败不影响本次返回）
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, JsonUtils.toJsonString(vo), CACHE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("[getHome] 回写首页缓存失败", e);
        }
        return vo;
    }

    private AppHomeRespVO buildHome() {
        AppHomeRespVO vo = new AppHomeRespVO();

        // 1. 平台统计
        AppHomeRespVO.StatsVO stats = new AppHomeRespVO.StatsVO();
        stats.setTotalStudents(enrollmentMapper.countByStatuses(Arrays.asList(
                EnrollmentStatusEnum.DEPOSITED.getStatus(),
                EnrollmentStatusEnum.PAID_OFF.getStatus(),
                EnrollmentStatusEnum.MANUAL_PROCESSING.getStatus(),
                EnrollmentStatusEnum.COMPLETED.getStatus())));
        Long openCount = courseSessionMapper.selectOpenCount();
        stats.setOpenSessionCount(openCount != null ? openCount.intValue() : 0);
        List<CourseSessionDO> openSessions = courseSessionMapper.selectOpenList();
        stats.setMonthRemainingSeats(openSessions.stream()
                .mapToInt(session -> session.getMaxStudents()
                        - (session.getEnrolledCount() != null ? session.getEnrolledCount() : 0))
                .sum());
        vo.setStats(stats);

        // 2. 近期开班（未来 30 天内，课程信息 + 定金批量拼接）
        LocalDate today = LocalDate.now();
        List<CourseSessionDO> upcomingSessions = courseSessionMapper
                .selectOpenByStartDateBetween(today, today.plusDays(UPCOMING_DAYS));
        vo.setUpcomingSessions(buildUpcomingSessions(upcomingSessions));
        return vo;
    }

    private List<AppHomeRespVO.UpcomingSessionVO> buildUpcomingSessions(List<CourseSessionDO> sessions) {
        if (CollUtil.isEmpty(sessions)) {
            return new ArrayList<>();
        }
        List<CourseSessionDO> limited = sessions.size() > UPCOMING_LIMIT
                ? sessions.subList(0, UPCOMING_LIMIT) : sessions;

        Set<Long> courseIds = limited.stream().map(CourseSessionDO::getCourseId).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(CourseDO::getId, Function.identity()));
        Set<Long> sessionIds = limited.stream().map(CourseSessionDO::getId).collect(Collectors.toSet());
        Map<Long, SessionFeeConfigDO> feeMap = sessionFeeConfigMapper.selectBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionFeeConfigDO::getSessionId, Function.identity(), (a, b) -> a));
        Map<Long, SessionVenueDO> venueMap = sessionVenueMapper.selectMapBySessionIds(sessionIds);

        List<AppHomeRespVO.UpcomingSessionVO> result = new ArrayList<>(limited.size());
        for (CourseSessionDO session : limited) {
            AppHomeRespVO.UpcomingSessionVO item = BeanUtils.toBean(session, AppHomeRespVO.UpcomingSessionVO.class);
            int enrolled = session.getEnrolledCount() != null ? session.getEnrolledCount() : 0;
            item.setEnrolledCount(enrolled);
            item.setRemainingCount(session.getMaxStudents() - enrolled);

            CourseDO course = courseMap.get(session.getCourseId());
            if (course != null) {
                item.setCourseName(course.getName());
                item.setCertificateType(course.getCertificateType());
                item.setCoverImage(course.getCoverImage());
            }
            // 城市取自场地（未选学校的班期没有场地，城市留空）
            SessionVenueDO venue = venueMap.get(session.getId());
            if (venue != null) {
                item.setCity(venue.getCity());
            }
            SessionFeeConfigDO feeConfig = feeMap.get(session.getId());
            if (feeConfig != null) {
                item.setTuitionAmount(feeConfig.getTuitionAmount());
                item.setDepositAmount(feeConfig.getDepositAmount());
                item.setIsGrouponEnabled(feeConfig.getIsGrouponEnabled());
                item.setGrouponDiscountAmount(feeConfig.getGrouponDiscountAmount());
            }
            result.add(item);
        }
        return result;
    }

}
