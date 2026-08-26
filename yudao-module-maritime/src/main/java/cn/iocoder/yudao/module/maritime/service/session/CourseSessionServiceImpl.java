package cn.iocoder.yudao.module.maritime.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionDetailRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.feeTemplate.FeeTemplateDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import cn.iocoder.yudao.module.maritime.service.feeTemplate.FeeTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.SESSION_NOT_EXISTS;

/**
 * 班期管理 Service 实现类
 */
@Slf4j
@Service
@Validated
public class CourseSessionServiceImpl implements CourseSessionService {

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private FeeTemplateService feeTemplateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourseSession(CourseSessionSaveReqVO createReqVO) {
        CourseSessionDO courseSession = BeanUtils.toBean(createReqVO, CourseSessionDO.class);
        // 已报名人数为系统维护的缓存值，新建班期一律从 0 起，不采信前端传值
        courseSession.setEnrolledCount(0);
        courseSessionMapper.insert(courseSession);

        // 费用配置：选择费用模板时快照复制模板内容；否则创建零值占位配置，运营后续在费用配置页面补充
        SessionFeeConfigDO feeConfig = buildFeeConfig(createReqVO.getFeeTemplateId(), courseSession.getId());
        sessionFeeConfigMapper.insert(feeConfig);
        log.info("[createCourseSession] 班期 {} 已创建，费用配置来源：{}",
                courseSession.getId(), createReqVO.getFeeTemplateId() != null ? "模板快照" : "零值占位");

        return courseSession.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseSession(CourseSessionSaveReqVO updateReqVO) {
        validateCourseSessionExists(updateReqVO.getId());
        CourseSessionDO updateObj = BeanUtils.toBean(updateReqVO, CourseSessionDO.class);
        courseSessionMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSession(Long id) {
        validateCourseSessionExists(id);
        courseSessionMapper.deleteById(id);
        // 级联软删除班期的费用配置，避免孤儿数据
        deleteFeeConfigsBySessionIds(List.of(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSessionListByIds(List<Long> ids) {
        courseSessionMapper.deleteByIds(ids);
        // 级联软删除班期的费用配置，避免孤儿数据
        deleteFeeConfigsBySessionIds(ids);
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
        feeConfig.setTuitionDescription(template.getTuitionDescription());
        feeConfig.setDepositAmount(template.getDepositAmount());
        feeConfig.setIsGrouponEnabled(template.getIsGrouponEnabled());
        feeConfig.setGrouponDiscountAmount(template.getGrouponDiscountAmount());
        feeConfig.setGrouponRequiredCount(template.getGrouponRequiredCount());
        feeConfig.setGrouponExpireHours(template.getGrouponExpireHours());
        feeConfig.setGrouponFailDiscountAmount(template.getGrouponFailDiscountAmount());
        feeConfig.setReferralCommissionAmount(template.getReferralCommissionAmount());
        feeConfig.setIsReferralCommissionEnabled(template.getIsReferralCommissionEnabled());
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

        AppSessionDetailRespVO vo = BeanUtils.toBean(session, AppSessionDetailRespVO.class);
        int enrolled = session.getEnrolledCount() != null ? session.getEnrolledCount() : 0;
        vo.setRemainingCount(session.getMaxStudents() - enrolled);

        if (feeConfig != null) {
            vo.setTuitionAmount(feeConfig.getTuitionAmount());
            vo.setTuitionDescription(feeConfig.getTuitionDescription());
            vo.setDepositAmount(feeConfig.getDepositAmount());
            vo.setIsGrouponEnabled(feeConfig.getIsGrouponEnabled());
            vo.setGrouponRequiredCount(feeConfig.getGrouponRequiredCount());
            vo.setReferralCommissionAmount(feeConfig.getReferralCommissionAmount());
            vo.setIsReferralCommissionEnabled(feeConfig.getIsReferralCommissionEnabled());
            vo.setRefundPolicyText(feeConfig.getRefundPolicyText());
            if (Boolean.TRUE.equals(feeConfig.getIsGrouponEnabled())
                    && feeConfig.getDepositAmount() != null
                    && feeConfig.getGrouponDiscountAmount() != null) {
                vo.setGrouponDepositAmount(
                        feeConfig.getDepositAmount().subtract(feeConfig.getGrouponDiscountAmount()));
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

    private void validateCourseSessionExists(Long id) {
        if (courseSessionMapper.selectById(id) == null) {
            throw exception(SESSION_NOT_EXISTS);
        }
    }

}
