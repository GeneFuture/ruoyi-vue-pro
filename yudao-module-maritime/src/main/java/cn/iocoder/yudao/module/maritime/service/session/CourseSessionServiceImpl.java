package cn.iocoder.yudao.module.maritime.service.session;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionDetailRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionVenue.SessionVenueMapper;
import cn.iocoder.yudao.module.maritime.service.feeTemplate.FeeTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.COURSE_NOT_EXISTS;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.SESSION_NOT_EXISTS;

/**
 * 班期管理 Service 实现类
 */
@Slf4j
@Service
@Validated
public class CourseSessionServiceImpl implements CourseSessionService {

    /** 班期编号中的开班年月格式 */
    private static final DateTimeFormatter SESSION_CODE_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    /** 班期编号序号位数 */
    private static final int SESSION_CODE_SEQ_WIDTH = 3;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private FeeTemplateService feeTemplateService;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private SessionVenueMapper sessionVenueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourseSession(CourseSessionSaveReqVO createReqVO) {
        CourseSessionDO courseSession = BeanUtils.toBean(createReqVO, CourseSessionDO.class);
        // 已报名人数为系统维护的缓存值，新建班期一律从 0 起，不采信前端传值
        courseSession.setEnrolledCount(0);
        // 班期编号由系统生成，不采信前端传值
        courseSession.setSessionCode(generateSessionCode(courseSession.getCourseId(), courseSession.getStartDate()));
        courseSessionMapper.insert(courseSession);

        // 费用配置：选择费用模板时快照复制模板内容；否则创建零值占位配置，运营后续在费用配置页面补充
        SessionFeeConfigDO feeConfig = buildFeeConfig(createReqVO.getFeeTemplateId(), courseSession.getId());
        sessionFeeConfigMapper.insert(feeConfig);
        log.info("[createCourseSession] 班期 {} 已创建，费用配置来源：{}",
                courseSession.getId(), createReqVO.getFeeTemplateId() != null ? "模板快照" : "零值占位");

        // 学校信息不在这里创建：学校在班期表单的「学校信息」面板里从学校库选择，
        // 由该面板随表单一起保存（场地信息整体可选，与费用配置不同——后者因学费/定金 NOT NULL 必须有占位行）

        return courseSession.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseSession(CourseSessionSaveReqVO updateReqVO) {
        validateCourseSessionExists(updateReqVO.getId());
        CourseSessionDO updateObj = BeanUtils.toBean(updateReqVO, CourseSessionDO.class);
        // 班期编号由系统生成，创建后不允许修改；显式置空，避免前端回传的值覆盖
        updateObj.setSessionCode(null);
        courseSessionMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSession(Long id) {
        validateCourseSessionExists(id);
        courseSessionMapper.deleteById(id);
        // 级联软删除班期的附属配置，避免孤儿数据
        deleteFeeConfigsBySessionIds(List.of(id));
        deleteVenuesBySessionIds(List.of(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSessionListByIds(List<Long> ids) {
        courseSessionMapper.deleteByIds(ids);
        // 级联软删除班期的附属配置，避免孤儿数据
        deleteFeeConfigsBySessionIds(ids);
        deleteVenuesBySessionIds(ids);
    }

    /** 级联软删除指定班期的费用配置 */
    private void deleteFeeConfigsBySessionIds(List<Long> sessionIds) {
        List<SessionFeeConfigDO> feeConfigs = sessionFeeConfigMapper.selectBySessionIds(sessionIds);
        if (feeConfigs.isEmpty()) {
            return;
        }
        sessionFeeConfigMapper.deleteByIds(
                feeConfigs.stream().map(SessionFeeConfigDO::getId).collect(Collectors.toList()));
    }

    /** 级联软删除指定班期的场地信息 */
    private void deleteVenuesBySessionIds(List<Long> sessionIds) {
        List<SessionVenueDO> venues = sessionVenueMapper.selectBySessionIds(sessionIds);
        if (venues.isEmpty()) {
            return;
        }
        sessionVenueMapper.deleteByIds(
                venues.stream().map(SessionVenueDO::getId).collect(Collectors.toList()));
    }

    /**
     * 生成班期编号：C{课程ID 36进制}-{开班年月}-{3位序号}，例如 C1-202610-001。
     *
     * 1. 课程ID 使用 36 进制编码，避免雪花ID过长导致 session_code(30) 超长；
     * 2. 按「课程 + 开班月份」分别编号，序号从 001 开始；
     * 3. 查询时包含已软删除的班期，编号一经使用不再复用，避免 (session_code, deleted) 唯一键在重复删除时冲突；
     * 4. 通过锁定课程行串行化同一课程的并发新增，避免生成相同序号。
     */
    private String generateSessionCode(Long courseId, LocalDate startDate) {
        // 课程不存在时直接报错，同时用行锁保证同一课程下的并发新增串行执行
        if (courseSessionMapper.lockCourseForUpdate(courseId) == null) {
            throw exception(COURSE_NOT_EXISTS);
        }
        String courseToken = Long.toString(courseId, 36).toUpperCase(Locale.ROOT);
        String prefix = "C" + courseToken + "-" + startDate.format(SESSION_CODE_MONTH_FORMATTER) + "-";
        int maxSeq = courseSessionMapper.selectCodesByPattern(prefix + "%").stream()
                .filter(code -> code != null && code.startsWith(prefix))
                .map(code -> code.substring(prefix.length()))
                .filter(suffix -> StrUtil.isNumeric(suffix) && suffix.length() <= 9)
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        return prefix + String.format(Locale.ROOT, "%0" + SESSION_CODE_SEQ_WIDTH + "d", maxSeq + 1);
    }

    /**
     * 构建班期的费用配置：
     * 传了费用模板则快照复制模板内容（模板后续修改不影响本班期）；
     * 未传则创建零值占位配置（学费/定金 NOT NULL 且无 DB 默认值），运营后续在费用配置页面补充。
     */
    private SessionFeeConfigDO buildFeeConfig(Long feeTemplateId, Long sessionId) {
        SessionFeeConfigDO feeConfig = new SessionFeeConfigDO();
        feeConfig.setSessionId(sessionId);
        if (feeTemplateId == null) {
            feeConfig.setTuitionAmount(BigDecimal.ZERO);
            feeConfig.setDepositAmount(BigDecimal.ZERO);
            return feeConfig;
        }
        FeeTemplateDO template = feeTemplateService.getEnabledFeeTemplate(feeTemplateId);
        feeConfig.setTuitionAmount(template.getTuitionAmount());
        feeConfig.setTuitionIncludesAccommodation(template.getTuitionIncludesAccommodation());
        feeConfig.setExtraFeeItems(template.getExtraFeeItems());
        feeConfig.setDepositAmount(template.getDepositAmount());
        feeConfig.setIsGrouponEnabled(template.getIsGrouponEnabled());
        feeConfig.setGrouponDiscountAmount(template.getGrouponDiscountAmount());
        feeConfig.setGrouponRequiredCount(template.getGrouponRequiredCount());
        feeConfig.setGrouponExpireHours(template.getGrouponExpireHours());
        feeConfig.setGrouponFailDiscountAmount(template.getGrouponFailDiscountAmount());
        feeConfig.setIsDepositRefundable(template.getIsDepositRefundable());
        feeConfig.setBalanceDueDaysBeforeStart(template.getBalanceDueDaysBeforeStart());
        feeConfig.setRefundPolicyText(template.getRefundPolicyText());
        return feeConfig;
    }

    @Override
    public CourseSessionDO getCourseSession(Long id) {
        return courseSessionMapper.selectById(id);
    }

    @Override
    public PageResult<CourseSessionDO> getCourseSessionPage(CourseSessionPageReqVO pageReqVO) {
        return courseSessionMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseSessionStatus(Long id, String sessionStatus) {
        validateCourseSessionExists(id);
        CourseSessionDO updateObj = new CourseSessionDO();
        updateObj.setId(id);
        updateObj.setSessionStatus(sessionStatus);
        courseSessionMapper.updateById(updateObj);
    }

    @Override
    public AppSessionDetailRespVO getCourseSessionForApp(Long id) {
        CourseSessionDO session = courseSessionMapper.selectById(id);
        if (session == null) {
            throw exception(SESSION_NOT_EXISTS);
        }

        SessionFeeConfigDO feeConfig = sessionFeeConfigMapper.selectBySessionId(id);
        List<GrouponRecordDO> activeGroupons = grouponRecordMapper.selectActiveBySessionId(id);
        CourseDO course = courseMapper.selectById(session.getCourseId());
        SessionVenueDO venue = sessionVenueMapper.selectBySessionId(id);

        AppSessionDetailRespVO vo = BeanUtils.toBean(session, AppSessionDetailRespVO.class);
        int enrolled = session.getEnrolledCount() != null ? session.getEnrolledCount() : 0;
        vo.setRemainingCount(session.getMaxStudents() - enrolled);

        // 课程上下文与场地信息都是可选补充，缺失时保持为空即可，不影响班期本身可正常展示
        if (course != null) {
            vo.setCourseName(course.getName());
            vo.setCertificateType(course.getCertificateType());
            vo.setCoverImage(course.getCoverImage());
            vo.setDescription(course.getDescription());
            vo.setEnrollmentConditionText(course.getEnrollmentConditionText());
        }
        if (venue != null) {
            vo.setVenue(BeanUtils.toBean(venue, AppSessionDetailRespVO.AppSessionVenueVO.class));
        }

        if (feeConfig != null) {
            vo.setTuitionAmount(feeConfig.getTuitionAmount());
            vo.setTuitionIncludesAccommodation(feeConfig.getTuitionIncludesAccommodation());
            vo.setExtraFeeItems(feeConfig.getExtraFeeItems());
            vo.setDepositAmount(feeConfig.getDepositAmount());
            vo.setIsGrouponEnabled(feeConfig.getIsGrouponEnabled());
            vo.setGrouponRequiredCount(feeConfig.getGrouponRequiredCount());
            vo.setRefundPolicyText(feeConfig.getRefundPolicyText());
            if (Boolean.TRUE.equals(feeConfig.getIsGrouponEnabled())) {
                vo.setGrouponDiscountAmount(feeConfig.getGrouponDiscountAmount());
            }
        }

        List<AppActiveGrouponVO> activeGrouponVOs = activeGroupons.stream().map(g -> {
            AppActiveGrouponVO gvo = new AppActiveGrouponVO();
            gvo.setId(g.getId());
            gvo.setInviteCode(g.getInviteCode());
            gvo.setCurrentCount(g.getCurrentCount());
            gvo.setRequiredCount(g.getRequiredCount());
            gvo.setRemainingCount(g.getRequiredCount() - g.getCurrentCount());
            gvo.setExpireTime(g.getExpireTime());
            return gvo;
        }).collect(Collectors.toList());
        vo.setActiveGroupons(activeGrouponVOs);

        return vo;
    }

    @Override
    public PageResult<AppSessionRespVO> getCourseSessionPageForApp(AppSessionPageReqVO reqVO) {
        // 1. 证书类型筛选：先单表查课程，拿到 courseId 范围（避免 JOIN）
        Set<Long> filteredCourseIds = null;
        if (StrUtil.isNotBlank(reqVO.getCertificateType())) {
            filteredCourseIds = courseMapper.selectPublishedList().stream()
                    .filter(course -> reqVO.getCertificateType().equals(course.getCertificateType()))
                    .map(CourseDO::getId)
                    .collect(Collectors.toSet());
            if (filteredCourseIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }

        // 2. 城市筛选：城市在场地表上，先按城市查出班期 ID 范围（避免 JOIN）
        Set<Long> citySessionIds = null;
        if (StrUtil.isNotBlank(reqVO.getCity())) {
            citySessionIds = new HashSet<>(sessionVenueMapper.selectSessionIdsByCityLike(reqVO.getCity()));
            if (citySessionIds.isEmpty()) {
                return new PageResult<>(Collections.emptyList(), 0L);
            }
        }

        // 3. 分页查询招生中、未开班的班期
        LambdaQueryWrapperX<CourseSessionDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(CourseSessionDO::getSessionStatus, "OPEN");
        wrapper.ge(CourseSessionDO::getStartDate, LocalDate.now());
        wrapper.inIfPresent(CourseSessionDO::getId, citySessionIds);
        if (StrUtil.isNotBlank(reqVO.getMonth())) {
            YearMonth yearMonth = YearMonth.parse(reqVO.getMonth());
            wrapper.between(CourseSessionDO::getStartDate, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        }
        if (filteredCourseIds != null) {
            wrapper.in(CourseSessionDO::getCourseId, filteredCourseIds);
        }
        wrapper.orderByAsc(CourseSessionDO::getStartDate);
        wrapper.orderByDesc(CourseSessionDO::getId);
        PageResult<CourseSessionDO> page = courseSessionMapper.selectPage(reqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return new PageResult<>(Collections.emptyList(), page.getTotal());
        }

        // 4. 批量查课程、费用配置与场地，内存拼接
        Set<Long> courseIds = page.getList().stream().map(CourseSessionDO::getCourseId).collect(Collectors.toSet());
        Map<Long, CourseDO> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(CourseDO::getId, Function.identity()));
        Set<Long> sessionIds = page.getList().stream().map(CourseSessionDO::getId).collect(Collectors.toSet());
        Map<Long, SessionFeeConfigDO> feeMap = sessionFeeConfigMapper.selectBySessionIds(sessionIds).stream()
                .collect(Collectors.toMap(SessionFeeConfigDO::getSessionId, Function.identity(), (a, b) -> a));
        Map<Long, SessionVenueDO> venueMap = sessionVenueMapper.selectMapBySessionIds(sessionIds);

        // 5. 组装 VO
        List<AppSessionRespVO> voList = new ArrayList<>(page.getList().size());
        for (CourseSessionDO session : page.getList()) {
            AppSessionRespVO vo = BeanUtils.toBean(session, AppSessionRespVO.class);
            int enrolled = session.getEnrolledCount() != null ? session.getEnrolledCount() : 0;
            vo.setRemainingCount(session.getMaxStudents() - enrolled);

            CourseDO course = courseMap.get(session.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
                vo.setCertificateType(course.getCertificateType());
                vo.setCoverImage(course.getCoverImage());
            }
            // 城市取自场地（未选学校的班期没有场地，城市留空）
            SessionVenueDO venue = venueMap.get(session.getId());
            if (venue != null) {
                vo.setCity(venue.getCity());
            }
            SessionFeeConfigDO feeConfig = feeMap.get(session.getId());
            if (feeConfig != null) {
                vo.setTuitionAmount(feeConfig.getTuitionAmount());
                vo.setDepositAmount(feeConfig.getDepositAmount());
                vo.setIsGrouponEnabled(feeConfig.getIsGrouponEnabled());
                if (Boolean.TRUE.equals(feeConfig.getIsGrouponEnabled())) {
                    vo.setGrouponDiscountAmount(feeConfig.getGrouponDiscountAmount());
                }
            }
            voList.add(vo);
        }
        return new PageResult<>(voList, page.getTotal());
    }

    private void validateCourseSessionExists(Long id) {
        if (courseSessionMapper.selectById(id) == null) {
            throw exception(SESSION_NOT_EXISTS);
        }
    }

}
