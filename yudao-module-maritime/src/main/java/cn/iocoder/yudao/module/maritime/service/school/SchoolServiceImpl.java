package cn.iocoder.yudao.module.maritime.service.school;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.school.SchoolMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.maritime.enums.ErrorCodeConstants.*;

/**
 * 学校库 Service 实现类
 *
 * @author Gene Ye
 */
@Service
@Validated
public class SchoolServiceImpl implements SchoolService {

    @Resource
    private SchoolMapper schoolMapper;

    @Override
    public Long createSchool(SchoolSaveReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        SchoolDO school = BeanUtils.toBean(createReqVO, SchoolDO.class);
        schoolMapper.insert(school);
        return school.getId();
    }

    @Override
    public void updateSchool(SchoolSaveReqVO updateReqVO) {
        // 校验存在
        validateSchoolExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新（已快照到班期的场地信息不受影响）
        SchoolDO updateObj = BeanUtils.toBean(updateReqVO, SchoolDO.class);
        schoolMapper.updateById(updateObj);
    }

    @Override
    public void deleteSchool(Long id) {
        // 校验存在
        validateSchoolExists(id);
        // 删除
        schoolMapper.deleteById(id);
    }

    @Override
    public void deleteSchoolListByIds(List<Long> ids) {
        schoolMapper.deleteByIds(ids);
    }

    private void validateSchoolExists(Long id) {
        if (schoolMapper.selectById(id) == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
    }

    private void validateNameUnique(Long id, String name) {
        SchoolDO school = schoolMapper.selectByName(name);
        if (school == null || Objects.equals(school.getId(), id)) {
            return;
        }
        throw exception(SCHOOL_NAME_DUPLICATE);
    }

    @Override
    public SchoolDO getSchool(Long id) {
        return schoolMapper.selectById(id);
    }

    @Override
    public SchoolDO getEnabledSchool(Long id) {
        SchoolDO school = schoolMapper.selectById(id);
        if (school == null) {
            throw exception(SCHOOL_NOT_EXISTS);
        }
        if (!CommonStatusEnum.ENABLE.getStatus().equals(school.getStatus())) {
            throw exception(SCHOOL_DISABLED);
        }
        return school;
    }

    @Override
    public List<SchoolDO> getEnabledSchoolList() {
        return schoolMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Override
    public PageResult<SchoolDO> getSchoolPage(SchoolPageReqVO pageReqVO) {
        return schoolMapper.selectPage(pageReqVO);
    }

}
