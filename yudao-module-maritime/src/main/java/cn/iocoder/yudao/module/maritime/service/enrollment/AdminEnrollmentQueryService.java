package cn.iocoder.yudao.module.maritime.service.enrollment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentPageReqVO;
import cn.iocoder.yudao.module.maritime.controller.admin.enrollment.vo.EnrollmentRespVO;

/**
 * 管理后台 - 报名记录查询 Service 接口
 *
 * 负责把报名记录关联的课程、班期、拼团信息组装成人工可读的 VO，
 * 避免后台页面直接展示 memberId / sessionId / grouponRecordId 等内部 ID。
 *
 * @author Gene Ye
 */
public interface AdminEnrollmentQueryService {

    /**
     * 获得报名记录详情（含课程/班期/拼团名称）
     *
     * @param id 报名记录ID
     * @return 报名记录
     */
    EnrollmentRespVO getEnrollment(Long id);

    /**
     * 获得报名记录分页（含课程/班期/拼团名称）
     *
     * @param pageReqVO 分页查询
     * @return 报名记录分页
     */
    PageResult<EnrollmentRespVO> getEnrollmentPage(EnrollmentPageReqVO pageReqVO);

}
