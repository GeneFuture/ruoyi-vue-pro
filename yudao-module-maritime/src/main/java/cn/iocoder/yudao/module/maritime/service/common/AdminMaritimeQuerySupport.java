package cn.iocoder.yudao.module.maritime.service.common;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.course.CourseDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.enrollment.EnrollmentDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.grouponRecord.GrouponRecordDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.refundApply.RefundApplyDO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.session.CourseSessionDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.course.CourseMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.enrollment.EnrollmentMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.grouponRecord.GrouponRecordMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.refundApply.RefundApplyMapper;
import cn.iocoder.yudao.module.maritime.dal.mysql.session.CourseSessionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理后台列表展示辅助组件
 *
 * 统一批量加载报名、班期、课程、拼团、退费申请，避免各后台页面重复写
 * N+1 查询和 Map 组装逻辑。
 *
 * @author Gene Ye
 */
@Component
public class AdminMaritimeQuerySupport {

    @Resource
    private EnrollmentMapper enrollmentMapper;

    @Resource
    private CourseSessionMapper courseSessionMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private GrouponRecordMapper grouponRecordMapper;

    @Resource
    private RefundApplyMapper refundApplyMapper;

    public Map<Long, EnrollmentDO> getEnrollmentMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return enrollmentMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(EnrollmentDO::getId, Function.identity(), (a, b) -> a));
    }

    public Map<Long, CourseSessionDO> getSessionMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return courseSessionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CourseSessionDO::getId, Function.identity(), (a, b) -> a));
    }

    public Map<Long, CourseDO> getCourseMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CourseDO::getId, Function.identity(), (a, b) -> a));
    }

    public Map<Long, GrouponRecordDO> getGrouponMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return grouponRecordMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(GrouponRecordDO::getId, Function.identity(), (a, b) -> a));
    }

    public Map<Long, RefundApplyDO> getRefundApplyMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return refundApplyMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(RefundApplyDO::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 按学员姓名/手机号查找报名ID，供没有学员字段的表做筛选
     *
     * @return 匹配的报名ID；姓名和手机号都为空时返回空集合
     */
    public List<Long> findEnrollmentIds(String realName, String phone) {
        if (StrUtil.isBlank(realName) && StrUtil.isBlank(phone)) {
            return Collections.emptyList();
        }
        return enrollmentMapper.selectList(new LambdaQueryWrapperX<EnrollmentDO>()
                        .likeIfPresent(EnrollmentDO::getRealName, realName)
                        .eqIfPresent(EnrollmentDO::getPhone, phone)).stream()
                .map(EnrollmentDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
