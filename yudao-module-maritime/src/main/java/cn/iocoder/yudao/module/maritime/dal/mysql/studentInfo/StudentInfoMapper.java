package cn.iocoder.yudao.module.maritime.dal.mysql.studentInfo;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.maritime.dal.dataobject.studentInfo.StudentInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学员信息 Mapper
 *
 * @author Gene Ye
 */
@Mapper
public interface StudentInfoMapper extends BaseMapperX<StudentInfoDO> {

    default StudentInfoDO selectByMemberId(Long memberId) {
        return selectOne(StudentInfoDO::getMemberId, memberId);
    }

}
