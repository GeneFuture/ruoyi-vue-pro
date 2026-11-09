import request from '@/config/axios'

/** 课程管理信息 */
export interface Course {
  id?: number
  name: string
  certificateType: string
  coverImage?: string
  description?: string
  enrollmentConditionText?: string
  status: number
  sortOrder?: number
  createTime?: string
}

// 课程管理 API
export const CourseApi = {
  // 查询课程分页
  getCoursePage: async (params: any) => {
    return await request.get({ url: `/maritime/course/page`, params })
  },

  // 查询课程详情
  getCourse: async (id: number) => {
    return await request.get({ url: `/maritime/course/get?id=` + id })
  },

  // 新增课程
  createCourse: async (data: Course) => {
    return await request.post({ url: `/maritime/course/create`, data })
  },

  // 修改课程
  updateCourse: async (data: Course) => {
    return await request.put({ url: `/maritime/course/update`, data })
  },

  // 删除课程
  deleteCourse: async (id: number) => {
    return await request.delete({ url: `/maritime/course/delete?id=` + id })
  },

  // 修改课程状态（上架/下架）
  updateCourseStatus: async (id: number, status: number) => {
    return await request.put({
      url: `/maritime/course/update-status?id=${id}&status=${status}`
    })
  }
}
