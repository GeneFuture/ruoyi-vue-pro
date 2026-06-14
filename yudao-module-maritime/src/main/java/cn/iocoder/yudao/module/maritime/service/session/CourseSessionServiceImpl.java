package cn.iocoder.yudao.module.maritime.service.session;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.CourseSessionSaveReqVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppActiveGrouponVO;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionDetailRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourseSession(CourseSessionSaveReqVO createReqVO) {
        CourseSessionDO courseSession = BeanUtils.toBean(createReqVO, CourseSessionDO.class);
        courseSessionMapper.insert(courseSession);

        // 自动创建空的费用配置，提示运营人员后续补充
        SessionFeeConfigDO feeConfig = new SessionFeeConfigDO();
        feeConfig.setSessionId(courseSession.getId());
        sessionFeeConfigMapper.insert(feeConfig);
        log.info("[createCourseSession] 班期 {} 已创建，已自动生成空费用配置，请在费用配置页面完善后再开放报名",
                courseSession.getId());

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
    }

    @Override
    public void deleteCourseSessionListByIds(List<Long> ids) {
        courseSessionMapper.deleteByIds(ids);
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
