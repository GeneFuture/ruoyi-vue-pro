package cn.iocoder.yudao.module.maritime.service.session;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.maritime.controller.admin.session.vo.*;
import cn.iocoder.yudao.module.maritime.controller.app.session.vo.AppSessionDetailRespVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 班期管理 Service 接口
 *
 * @author Gene Ye
 */
public interface CourseSessionService {

    /**
     * 创建班期管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCourseSession(@Valid CourseSessionSaveReqVO createReqVO);

    /**
     * 更新班期管理
     *
     * @param updateReqVO 更新信息
     */
    void updateCourseSession(@Valid CourseSessionSaveReqVO updateReqVO);

    /**
     * 删除班期管理
     *
     * @param id 编号
     */
    void deleteCourseSession(Long id);

    /**
    * 批量删除班期管理
    *
    * @param ids 编号
    */
    void deleteCourseSessionListByIds(List<Long> ids);

    /**
     * 获得班期管理
     *
     * @param id 编号
     * @return 班期管理
     */
    CourseSessionDO getCourseSession(Long id);

    /**
     * 获得班期管理分页
     *
     * @param pageReqVO 分页查询
     * @return 班期管理分页
     */
    PageResult<CourseSessionDO> getCourseSessionPage(CourseSessionPageReqVO pageReqVO);

    /**
     * 获得班期详情（小程序端，含费用配置和进行中的拼团）
     *
     * @param id 班期编号
     * @return 班期详情
     */
    AppSessionDetailRespVO getCourseSessionForApp(Long id);

    /**
     * 修改班期状态
     *
     * @param id            班期编号
     * @param sessionStatus 目标状态（DRAFT/OPEN/PENDING_START/IN_PROGRESS/FINISHED/CANCELLED）
     */
    void updateCourseSessionStatus(Long id, String sessionStatus);

}