import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 拼团记录信息 */
export interface GrouponRecord {
  id: number // 拼团记录ID
  sessionId?: number // 班期ID
  courseName?: string // 课程名称
  sessionCode?: string // 班期编号
  sessionName?: string // 班级名称
  sessionStartDate?: string // 开班日期
  inviteCode?: string // 拼团邀请码
  initiatorEnrollmentId?: number // 发起人报名ID
  initiatorEnrollmentNo?: string // 发起人报名单号
  initiatorRealName?: string // 发起人姓名
  initiatorPhone?: string // 发起人手机号
  requiredCount?: number // 需要人数
  currentCount?: number // 当前人数
  grouponStatus?: string // 拼团状态（IN_PROGRESS/SUCCESS/DEGRADED）
  expireTime?: string | Dayjs // 拼团截止时间
  successTime?: string | Dayjs // 拼团成功时间
  degradeReason?: string // 结束原因
  createTime?: string | Dayjs // 创建时间
}

// 拼团记录 API
export const GrouponRecordApi = {
  // 查询拼团记录分页
  getGrouponRecordPage: async (params: any) => {
    return await request.get({ url: `/maritime/groupon-record/page`, params })
  },

  // 查询拼团记录详情
  getGrouponRecord: async (id: number) => {
    return await request.get({ url: `/maritime/groupon-record/get?id=` + id })
  },

  // 新增拼团记录
  createGrouponRecord: async (data: GrouponRecord) => {
    return await request.post({ url: `/maritime/groupon-record/create`, data })
  },

  // 修改拼团记录
  updateGrouponRecord: async (data: GrouponRecord) => {
    return await request.put({ url: `/maritime/groupon-record/update`, data })
  },

  // 删除拼团记录
  deleteGrouponRecord: async (id: number) => {
    return await request.delete({ url: `/maritime/groupon-record/delete?id=` + id })
  },

  /** 批量删除拼团记录 */
  deleteGrouponRecordList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/groupon-record/delete-list?ids=${ids.join(',')}` })
  },

  // 导出拼团记录 Excel
  exportGrouponRecord: async (params) => {
    return await request.download({ url: `/maritime/groupon-record/export-excel`, params })
  },

  // 强制成团（管理员特批）
  forceSucceedGroupon: async (grouponRecordId: number) => {
    return await request.put({
      url: `/maritime/groupon-record/force-succeed?grouponRecordId=${grouponRecordId}`
    })
  },

  // 关闭拼团（降级处理）
  closeGroupon: async (grouponRecordId: number) => {
    return await request.put({
      url: `/maritime/groupon-record/close?grouponRecordId=${grouponRecordId}`
    })
  }
}