package cn.iocoder.yudao.module.maritime.service.school;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.SchoolSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.school.SchoolDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 学校库 Service 接口
 *
 * 独立的学校资料；新增班期时选择学校并快照复制到班期场地信息。
 *
 * @author Gene Ye
 */
public interface SchoolService {

    /**
     * 创建学校
     *
     * @param createReqVO 创建信息
     * @return 学校编号
     */
    Long createSchool(@Valid SchoolSaveReqVO createReqVO);

    /**
     * 更新学校（不影响已快照的班期场地信息）
     *
     * @param updateReqVO 更新信息
     */
    void updateSchool(@Valid SchoolSaveReqVO updateReqVO);

    /**
     * 删除学校
     *
     * @param id 编号
     */
    void deleteSchool(Long id);

    /**
     * 批量删除学校
     *
     * @param ids 编号列表
     */
    void deleteSchoolListByIds(List<Long> ids);

    /**
     * 获得学校
     *
     * @param id 编号
     * @return 学校
     */
    SchoolDO getSchool(Long id);

    /**
     * 获得开启状态的学校（新增班期快照用），不存在或已关闭则抛出异常
     *
     * @param id 编号
     * @return 开启中的学校
     */
    SchoolDO getEnabledSchool(Long id);

    /**
     * 获得开启状态的学校列表（班期表单下拉用）
     *
     * @return 学校列表
     */
    List<SchoolDO> getEnabledSchoolList();

    /**
     * 获得学校分页
     *
     * @param pageReqVO 分页查询
     * @return 学校分页
     */
    PageResult<SchoolDO> getSchoolPage(SchoolPageReqVO pageReqVO);

}
