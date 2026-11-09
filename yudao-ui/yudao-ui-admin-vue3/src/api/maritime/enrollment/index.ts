import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 报名记录信息 */
export interface Enrollment {
  id: number
  enrollmentNo: string
  memberId?: number
  sessionId?: number
  courseName?: string
  sessionCode?: string
  sessionName?: string
  sessionStartDate?: string
  grouponRecordId?: number
  grouponInviteCode?: string
  grouponStatus?: string
  realName?: string
  idCardMasked?: string
  phone?: string
  totalAmount?: string
  depositAmountSnapshot?: string
  balanceAmount?: string
  paidAmount?: string
  balanceDueDate?: string | Dayjs
  enrollmentStatus: string
  cancelReason?: string
  createTime?: string
}

/** 报名记录更新：只允许人工修改状态、尾款截止日期、取消原因 */
export interface EnrollmentUpdateReqVO {
  id: number
  enrollmentStatus: string
  balanceDueDate?: string
  cancelReason?: string
}

// 报名记录 API
export const EnrollmentApi = {
  // 查询报名记录分页
  getEnrollmentPage: async (params: any) => {
    return await request.get({ url: `/maritime/enrollment/page`, params })
  },

  // 查询报名记录详情
  getEnrollment: async (id: number) => {
    return await request.get({ url: `/maritime/enrollment/get?id=` + id })
  },

  // 新增报名记录
  createEnrollment: async (data: Enrollment) => {
    return await request.post({ url: `/maritime/enrollment/create`, data })
  },

  // 修改报名记录
  updateEnrollment: async (data: EnrollmentUpdateReqVO) => {
    return await request.put({ url: `/maritime/enrollment/update`, data })
  },

  // 删除报名记录
  deleteEnrollment: async (id: number) => {
    return await request.delete({ url: `/maritime/enrollment/delete?id=` + id })
  },

  /** 批量删除报名记录 */
  deleteEnrollmentList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/enrollment/delete-list?ids=${ids.join(',')}` })
  },

  // 导出报名记录 Excel
  exportEnrollment: async (params) => {
    return await request.download({ url: `/maritime/enrollment/export-excel`, params })
  }
}
