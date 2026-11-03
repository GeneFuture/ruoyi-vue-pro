package cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.module.maritime.controller.admin.sessionVenue.vo.*;
import cn.iocoder.yudao.module.maritime.dal.dataobject.sessionVenue.SessionVenueDO;
import cn.iocoder.yudao.module.maritime.service.sessionVenue.SessionVenueService;

/**
 * 班期场地信息：班期的 1:1 附属配置，只在「班期管理」表单内嵌面板中编辑，
 * 因此不提供独立的增删改查与后台菜单；权限复用 maritime:course-session
 * （与「班期费用配置」同一处理，参见 11-maritime-remove-fee-config-page.sql）
 */
@Tag(name = "管理后台 - 班期场地信息")
@RestController
@RequestMapping("/maritime/session-venue")
@Validated
public class AdminSessionVenueController {

    @Resource
    private SessionVenueService sessionVenueService;

    @PutMapping("/save")
    @Operation(summary = "保存场地信息（存在则更新，不存在则创建）")
    @PreAuthorize("@ss.hasPermission('maritime:course-session:update')")
    public CommonResult<Boolean> saveSessionVenue(@Valid @RequestBody SessionVenueSaveReqVO saveReqVO) {
        sessionVenueService.saveSessionVenue(saveReqVO);
        return success(true);
    }

    @GetMapping("/get-by-session")
    @Operation(summary = "按班期ID查询场地信息")
    @Parameter(name = "sessionId", description = "班期ID", required = true)
    @PreAuthorize("@ss.hasPermission('maritime:course-session:query')")
    public CommonResult<SessionVenueRespVO> getSessionVenueBySession(@RequestParam("sessionId") Long sessionId) {
        SessionVenueDO venue = sessionVenueService.getSessionVenueBySessionId(sessionId);
        return success(BeanUtils.toBean(venue, SessionVenueRespVO.class));
    }

}
