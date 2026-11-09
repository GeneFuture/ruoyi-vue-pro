import request from '@/config/axios'

/** 班期场地信息（即学校信息） */
export interface SessionVenue {
  id?: number // 班期场地ID
  sessionId?: number // 班期ID（唯一）
  campusName?: string // 学校名称
  province?: string // 省
  city?: string // 市
  district?: string // 区
  address?: string // 详细地址（省市区之后的部分，如：某某路 1 号）
  latitude?: number | string // 纬度（-90 ~ 90），未配置坐标时为空
  longitude?: number | string // 经度（-180 ~ 180），未配置坐标时为空
  contactPhone?: string // 咨询电话
  campusIntro?: string // 学校概况（富文本HTML）
  accommodationIntro?: string // 住宿条件（富文本HTML，可空）
  classroomIntro?: string // 教学条件（富文本HTML）
}

/**
 * 班期场地信息 API
 *
 * 场地信息是班期的 1:1 附属配置，已并入班期编辑表单（内嵌场地面板），
 * 与「班期费用配置」同一处理方式，因此不提供独立管理页面，
 * 仅保留按班期读取/保存两个接口。
 */
export const SessionVenueApi = {
  // 按班期ID查询场地信息
  getSessionVenueBySession: async (sessionId: number) => {
    return await request.get({
      url: `/maritime/session-venue/get-by-session?sessionId=` + sessionId
    })
  },

  // 保存场地信息（按班期存在则更新，不存在则创建）
  saveSessionVenue: async (data: SessionVenue) => {
    return await request.put({ url: `/maritime/session-venue/save`, data })
  }
}
