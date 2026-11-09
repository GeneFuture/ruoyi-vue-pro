import request from '@/config/axios'
import type { Dayjs } from 'dayjs'

/** 班期管理信息 */
export interface CourseSession {
  id: number // 班期ID
  courseId?: number // 课程ID
  sessionCode?: string // 班期编号（系统自动生成，创建后不可修改）
  sessionName: string // 班级名称（可选，如：A班）
  durationDays?: number // 本期课程天数
  instructorInfo?: string // 本期讲师信息
  startDate?: string | Dayjs // 开班日期
  endDate?: string | Dayjs // 结束日期
  enrollmentDeadline?: string | Dayjs // 报名截止日期
  maxStudents?: number // 最大招生人数
  enrolledCount?: number // 已报名人数
  sessionStatus?: string // 班期状态（DRAFT草稿/OPEN招生中/PENDING_START待开班/IN_PROGRESS进行中/FINISHED已完成/CANCELLED已取消）
  remark?: string // 班期备注
}

// 班期管理 API
export const CourseSessionApi = {
  // 查询班期管理分页
  getCourseSessionPage: async (params: any) => {
    return await request.get({ url: `/maritime/course-session/page`, params })
  },

  // 查询班期管理详情
  getCourseSession: async (id: number) => {
    return await request.get({ url: `/maritime/course-session/get?id=` + id })
  },

  // 新增班期管理
  createCourseSession: async (data: CourseSession) => {
    return await request.post({ url: `/maritime/course-session/create`, data })
  },

  // 修改班期管理
  updateCourseSession: async (data: CourseSession) => {
    return await request.put({ url: `/maritime/course-session/update`, data })
  },

  // 删除班期管理
  deleteCourseSession: async (id: number) => {
    return await request.delete({ url: `/maritime/course-session/delete?id=` + id })
  },

  /** 批量删除班期管理 */
  deleteCourseSessionList: async (ids: number[]) => {
    return await request.delete({
      url: `/maritime/course-session/delete-list?ids=${ids.join(',')}`
    })
  },

  // 导出班期管理 Excel
  exportCourseSession: async (params) => {
    return await request.download({ url: `/maritime/course-session/export-excel`, params })
  }
}
