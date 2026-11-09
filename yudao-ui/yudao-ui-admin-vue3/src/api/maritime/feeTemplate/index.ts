import request from '@/config/axios'

/** 另行缴费项目（到校报到时缴纳） */
export interface ExtraFeeItem {
  name: string // 项目名称（如：教材费）
  amount?: number // 金额（元）
  required?: boolean // 是否必须缴纳（true 必须 false 可选）
}

/** 费用模板信息 */
export interface FeeTemplate {
  id: number // 模板ID
  name: string // 模板名称
  tuitionAmount?: number // 学费总额（元）
  tuitionIncludesAccommodation?: boolean // 学费是否已包含住宿费
  extraFeeItems?: ExtraFeeItem[] // 另行缴费项目
  depositAmount?: number // 定金金额（元）
  isGrouponEnabled?: boolean // 是否开启拼团
  grouponDiscountAmount?: number // 拼团优惠减免金额（元）
  grouponRequiredCount?: number // 拼团所需人数
  grouponExpireHours?: number // 拼团有效时间（小时）
  grouponFailDiscountAmount?: number // 拼团失败降级优惠金额（元）
  isDepositRefundable?: boolean // 定金是否可退
  balanceDueDaysBeforeStart?: number // 尾款截止：开班前N天
  refundPolicyText?: string // 退款政策说明文本
  status?: number // 状态（0启用 1停用）
}

// 费用模板 API
export const FeeTemplateApi = {
  // 查询费用模板分页
  getFeeTemplatePage: async (params: any) => {
    return await request.get({ url: `/maritime/fee-template/page`, params })
  },

  // 查询费用模板详情
  getFeeTemplate: async (id: number) => {
    return await request.get({ url: `/maritime/fee-template/get?id=` + id })
  },

  // 新增费用模板
  createFeeTemplate: async (data: FeeTemplate) => {
    return await request.post({ url: `/maritime/fee-template/create`, data })
  },

  // 修改费用模板
  updateFeeTemplate: async (data: FeeTemplate) => {
    return await request.put({ url: `/maritime/fee-template/update`, data })
  },

  // 删除费用模板
  deleteFeeTemplate: async (id: number) => {
    return await request.delete({ url: `/maritime/fee-template/delete?id=` + id })
  },

  // 批量删除费用模板
  deleteFeeTemplateList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/fee-template/delete-list?ids=${ids.join(',')}` })
  }
}
