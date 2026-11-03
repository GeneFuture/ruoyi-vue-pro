package cn.iocoder.yudao.module.maritime.controller.admin.school;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import java.util.List;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.maritime.controller.admin.school.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.school.SchoolDO;
import cn.iocoder.yudao.module.maritime.service.school.SchoolService;

@Tag(name = "管理后台 - 学校库")
@RestController
@RequestMapping("/maritime/school")
@Validated
public class AdminSchoolController {

    @Resource
    private SchoolService schoolService;

    @PostMapping("/create")
    @Operation(summary = "创建学校")
    @PreAuthorize("@ss.hasPermission('maritime:school:create')")
    public CommonResult<Long> createSchool(@Valid @RequestBody SchoolSaveReqVO createReqVO) {
        return success(schoolService.createSchool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新学校")
    @PreAuthorize("@ss.hasPermission('maritime:school:update')")
    public CommonResult<Boolean> updateSchool(@Valid @RequestBody SchoolSaveReqVO updateReqVO) {
        schoolService.updateSchool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除学校")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:school:delete')")
    public CommonResult<Boolean> deleteSchool(@RequestParam("id") Long id) {
        schoolService.deleteSchool(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除学校")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:school:delete')")
    public CommonResult<Boolean> deleteSchoolList(@RequestParam("ids") List<Long> ids) {
        schoolService.deleteSchoolListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得学校")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('maritime:school:query')")
    public CommonResult<SchoolRespVO> getSchool(@RequestParam("id") Long id) {
        SchoolDO school = schoolService.getSchool(id);
        return success(BeanUtils.toBean(school, SchoolRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得学校分页")
    @PreAuthorize("@ss.hasPermission('maritime:school:query')")
    public CommonResult<PageResult<SchoolRespVO>> getSchoolPage(@Valid SchoolPageReqVO pageReqVO) {
        PageResult<SchoolDO> pageResult = schoolService.getSchoolPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, SchoolRespVO.class));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得开启状态的学校列表", description = "用于班期表单的学校下拉")
    @PreAuthorize("@ss.hasPermission('maritime:school:query')")
    public CommonResult<List<SchoolRespVO>> getEnabledSchoolList() {
        List<SchoolDO> list = schoolService.getEnabledSchoolList();
        return success(BeanUtils.toBean(list, SchoolRespVO.class));
    }

}
