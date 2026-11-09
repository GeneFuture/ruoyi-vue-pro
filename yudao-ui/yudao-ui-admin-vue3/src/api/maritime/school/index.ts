import request from '@/config/axios'

/** 学校库信息 */
export interface School {
  id?: number // 学校ID
  name?: string // 学校名称
  province?: string // 省
  city?: string // 市
  district?: string // 区
  address?: string // 详细地址（省市区之后的部分，如：某某路 1 号）
  latitude?: number | string // 纬度（-90 ~ 90），未配置坐标时为空
  longitude?: number | string // 经度（-180 ~ 180），未配置坐标时为空
  contactPhone?: string // 咨询电话
  campusIntro?: string // 学校概况（富文本HTML）
  classroomIntro?: string // 教学条件（富文本HTML）
  accommodationIntro?: string // 住宿条件（富文本HTML，可空）
  status?: number // 状态（0开启 1关闭）
  sortOrder?: number // 排序权重（越大越靠前）
}

// 学校库 API
export const SchoolApi = {
  // 查询学校分页
  getSchoolPage: async (params: any) => {
    return await request.get({ url: `/maritime/school/page`, params })
  },

  // 查询学校详情
  getSchool: async (id: number) => {
    return await request.get({ url: `/maritime/school/get?id=` + id })
  },

  // 查询开启状态的学校列表（班期表单下拉用）
  getEnabledSchoolList: async () => {
    return await request.get({ url: `/maritime/school/list-all-simple` })
  },

  // 新增学校
  createSchool: async (data: School) => {
    return await request.post({ url: `/maritime/school/create`, data })
  },

  // 修改学校
  updateSchool: async (data: School) => {
    return await request.put({ url: `/maritime/school/update`, data })
  },

  // 删除学校
  deleteSchool: async (id: number) => {
    return await request.delete({ url: `/maritime/school/delete?id=` + id })
  },

  // 批量删除学校
  deleteSchoolList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/school/delete-list?ids=${ids.join(',')}` })
  }
}
