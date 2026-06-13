package cn.iocoder.yudao.module.maritime.service.sessionFeeConfig;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.maritime.controller.admin.sessionFeeConfig.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionFeeConfig.SessionFeeConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.maritime.dal.mysql.sessionFeeConfig.SessionFeeConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 班期费用配置 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class SessionFeeConfigServiceImpl implements SessionFeeConfigService {

    @Resource
    private SessionFeeConfigMapper sessionFeeConfigMapper;

    @Override
    public Long createSessionFeeConfig(SessionFeeConfigSaveReqVO createReqVO) {
        // 插入
        SessionFeeConfigDO sessionFeeConfig = BeanUtils.toBean(createReqVO, SessionFeeConfigDO.class);
        sessionFeeConfigMapper.insert(sessionFeeConfig);

        // 返回
        return sessionFeeConfig.getId();
    }

    @Override
    public void updateSessionFeeConfig(SessionFeeConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateSessionFeeConfigExists(updateReqVO.getId());
        // 更新
        SessionFeeConfigDO updateObj = BeanUtils.toBean(updateReqVO, SessionFeeConfigDO.class);
        sessionFeeConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteSessionFeeConfig(Long id) {
        // 校验存在
        validateSessionFeeConfigExists(id);
        // 删除
        sessionFeeConfigMapper.deleteById(id);
    }

    @Override
    public void deleteSessionFeeConfigListByIds(List<Long> ids) {
        // 删除
        sessionFeeConfigMapper.deleteByIds(ids);
    }


    private void validateSessionFeeConfigExists(Long id) {
        if (sessionFeeConfigMapper.selectById(id) == null) {
            throw exception(SESSION_FEE_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public SessionFeeConfigDO getSessionFeeConfig(Long id) {
        return sessionFeeConfigMapper.selectById(id);
    }

    @Override
    public PageResult<SessionFeeConfigDO> getSessionFeeConfigPage(SessionFeeConfigPageReqVO pageReqVO) {
        return sessionFeeConfigMapper.selectPage(pageReqVO);
    }

}