package cn.iocoder.yudao.module.maritime.service.studentInfo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.maritime.controller.app.studentInfo.vo.AppStudentInfoSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.studentInfo.StudentInfoDO;
import cn.iocoder.yudao.module.maritime.dal.mysql.studentInfo.StudentInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 学员信息 Service 实现类
 *
 * @author Gene Ye
 */
@Slf4j
@Service
@Validated
public class StudentInfoServiceImpl implements StudentInfoService {

    @Resource
    private StudentInfoMapper studentInfoMapper;

    @Override
    public StudentInfoDO getByMemberId(Long memberId) {
        return studentInfoMapper.selectByMemberId(memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long memberId, AppStudentInfoSaveReqVO reqVO) {
        StudentInfoDO existing = studentInfoMapper.selectByMemberId(memberId);
        if (existing == null) {
            StudentInfoDO studentInfo = StudentInfoDO.builder()
                    .memberId(memberId)
                    .realName(reqVO.getRealName().trim())
                    .idCard(reqVO.getIdCard().trim().toUpperCase())
                    .phone(reqVO.getPhone().trim())
                    .build();
            studentInfoMapper.insert(studentInfo);
            return;
        }
        StudentInfoDO updateObj = new StudentInfoDO();
        updateObj.setId(existing.getId());
        updateObj.setRealName(reqVO.getRealName().trim());
        updateObj.setIdCard(reqVO.getIdCard().trim().toUpperCase());
        updateObj.setPhone(reqVO.getPhone().trim());
        studentInfoMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveIfAbsent(Long memberId, String realName, String idCard, String phone) {
        if (memberId == null || StrUtil.isBlank(realName) || StrUtil.isBlank(idCard)
                || StrUtil.isBlank(phone)) {
            return;
        }
        if (studentInfoMapper.selectByMemberId(memberId) != null) {
            return;
        }
        StudentInfoDO studentInfo = StudentInfoDO.builder()
                .memberId(memberId)
                .realName(realName.trim())
                .idCard(idCard.trim().toUpperCase())
                .phone(phone.trim())
                .build();
        studentInfoMapper.insert(studentInfo);
    }

}
