package cn.iocoder.yudao.module.maritime.service.enrollment;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 报名管理 Service 接口
 *
 * @author Gene Ye
 */
public interface EnrollmentService {

    /**
     * 创建报名管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEnrollment(@Valid EnrollmentSaveReqVO createReqVO);

    /**
     * 更新报名管理
     *
     * @param updateReqVO 更新信息
     */
    void updateEnrollment(@Valid EnrollmentSaveReqVO updateReqVO);

    /**
     * 删除报名管理
     *
     * @param id 编号
     */
    void deleteEnrollment(Long id);

    /**
    * 批量删除报名管理
    *
    * @param ids 编号
    */
    void deleteEnrollmentListByIds(List<Long> ids);

    /**
     * 获得报名管理
     *
     * @param id 编号
     * @return 报名管理
     */
    EnrollmentDO getEnrollment(Long id);

    /**
     * 获得报名管理分页
     *
     * @param pageReqVO 分页查询
     * @return 报名管理分页
     */
    PageResult<EnrollmentDO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO);

}