import request from '@/config/axios'

/** 另行缴费项目（到校报到时缴纳） */
export interface ExtraFeeItem {
  name: string // 项目名称（如：教材费）
  amount?: number // 金额（元）
  required?: boolean // 是否必须缴纳（true 必须 false 可选）
}

/** 班期费用配置信息 */
export interface SessionFeeConfig {
  id?: number // 费用配置ID
  sessionId?: number // 班期ID（唯一）
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
}

/**
 * 班期费用配置 API
 *
 * 费用配置已并入班期编辑表单（创建班期时由费用模板快照生成），
 * 独立管理页面已删除，因此仅保留按班期读取/保存两个接口。
 */
export const SessionFeeConfigApi = {
  // 按班期ID查询费用配置
  getSessionFeeConfigBySession: async (sessionId: number) => {
    return await request.get({
      url: `/maritime/session-fee-config/get-by-session?sessionId=` + sessionId
    })
  },

  // 保存费用配置（按班期存在则更新，不存在则创建）
  saveSessionFeeConfig: async (data: SessionFeeConfig) => {
    return await request.put({ url: `/maritime/session-fee-config/save`, data })
  }
}
