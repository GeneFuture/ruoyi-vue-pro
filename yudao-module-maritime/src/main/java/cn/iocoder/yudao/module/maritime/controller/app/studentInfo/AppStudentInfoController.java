package cn.iocoder.yudao.module.maritime.controller.app.studentInfo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.maritime.controller.app.studentInfo.vo.AppStudentInfoRespVO;
import cn.iocoder.yudao.module.maritime.controller.app.studentInfo.vo.AppStudentInfoSaveReqVO;
import cn.iocoder.yudao.module.maritime.dal.dataobject.studentInfo.StudentInfoDO;
import cn.iocoder.yudao.module.maritime.service.studentInfo.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 学员信息")
@RestController
@RequestMapping("/maritime/student-info")
@Validated
public class AppStudentInfoController {

    @Resource
    private StudentInfoService studentInfoService;

    @GetMapping("/get")
    @Operation(summary = "获得我的学员信息")
    public CommonResult<AppStudentInfoRespVO> getStudentInfo() {
        StudentInfoDO studentInfo = studentInfoService.getByMemberId(getLoginUserId());
        return success(BeanUtils.toBean(studentInfo, AppStudentInfoRespVO.class));
    }

    @PutMapping("/save")
    @Operation(summary = "保存我的学员信息")
    public CommonResult<Boolean> saveStudentInfo(@Valid @RequestBody AppStudentInfoSaveReqVO reqVO) {
        studentInfoService.save(getLoginUserId(), reqVO);
        return success(true);
    }

}
