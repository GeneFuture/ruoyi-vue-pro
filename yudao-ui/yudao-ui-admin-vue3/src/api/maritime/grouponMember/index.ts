import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 拼团成员信息 */
export interface GrouponMember {
  id: number // 拼团成员ID
  grouponRecordId?: number // 拼团记录ID
  inviteCode?: string // 拼团邀请码
  grouponStatus?: string // 拼团状态
  enrollmentId?: number // 报名ID
  enrollmentNo?: string // 报名单号
  memberId?: number // 学员ID
  realName?: string // 学员姓名
  phone?: string // 学员手机号
  courseName?: string // 课程名称
  sessionCode?: string // 班期编号
  sessionName?: string // 班级名称
  sessionStartDate?: string // 开班日期
  joinTime?: string | Dayjs // 加入时间
  memberStatus?: string // 成员状态（ACTIVE正常/CANCELLED已退出）
  depositPaidAt?: string | Dayjs // 定金支付时间
  createTime?: string | Dayjs // 创建时间
}

// 拼团成员 API
export const GrouponMemberApi = {
  // 查询拼团成员分页
  getGrouponMemberPage: async (params: any) => {
    return await request.get({ url: `/maritime/groupon-member/page`, params })
  },

  // 查询拼团成员详情
  getGrouponMember: async (id: number) => {
    return await request.get({ url: `/maritime/groupon-member/get?id=` + id })
  },

  // 新增拼团成员
  createGrouponMember: async (data: GrouponMember) => {
    return await request.post({ url: `/maritime/groupon-member/create`, data })
  },

  // 修改拼团成员
  updateGrouponMember: async (data: GrouponMember) => {
    return await request.put({ url: `/maritime/groupon-member/update`, data })
  },

  // 删除拼团成员
  deleteGrouponMember: async (id: number) => {
    return await request.delete({ url: `/maritime/groupon-member/delete?id=` + id })
  },

  /** 批量删除拼团成员 */
  deleteGrouponMemberList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/groupon-member/delete-list?ids=${ids.join(',')}` })
  },

  // 导出拼团成员 Excel
  exportGrouponMember: async (params) => {
    return await request.download({ url: `/maritime/groupon-member/export-excel`, params })
  }
}