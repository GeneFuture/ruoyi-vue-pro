package cn.iocoder.yudao.module.maritime.service.studentInfo;

import cn.iocoder.yudao.module.maritime.controller.app.studentInfo.vo.AppStudentInfoSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.studentInfo.StudentInfoDO;

/**
 * 学员信息 Service 接口
 *
 * @author Gene Ye
 */
public interface StudentInfoService {

    /**
     * 获得会员的学员信息
     *
     * @param memberId 会员编号
     * @return 学员信息
     */
    StudentInfoDO getByMemberId(Long memberId);

    /**
     * 保存会员的学员信息（存在则更新，不存在则创建）
     *
     * @param memberId 会员编号
     * @param reqVO    学员信息
     */
    void save(Long memberId, AppStudentInfoSaveReqVO reqVO);

    /**
     * 报名时补充学员信息：仅在会员还没有学员信息时写入，避免覆盖用户维护的资料
     *
     * @param memberId 会员编号
     * @param realName 真实姓名
     * @param idCard   身份证号（明文）
     * @param phone    手机号
     */
    void saveIfAbsent(Long memberId, String realName, String idCard, String phone);

}
