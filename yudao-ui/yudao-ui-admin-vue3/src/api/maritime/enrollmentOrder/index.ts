import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 支付订单信息 */
export interface EnrollmentOrder {
  id: number // 订单ID
  enrollmentId?: number // 报名ID
  enrollmentNo?: string // 报名单号
  realName?: string // 学员姓名
  phone?: string // 学员手机号
  courseName?: string // 课程名称
  sessionCode?: string // 班期编号
  sessionName?: string // 班级名称
  sessionStartDate?: string // 开班日期
  orderNo?: string // 订单号
  orderType?: string // 订单类型（DEPOSIT定金/BALANCE尾款/FULL全款一次付）
  amount?: number // 应付金额（元）
  payOrderId?: number // ruoyi pay模块订单ID
  payChannel?: string // 支付渠道
  orderStatus?: string // 订单状态（PENDING/PAID/CLOSED/REFUNDED）
  payTime?: string | Dayjs // 支付时间
  expireTime?: string | Dayjs // 订单过期时间
  createTime?: string | Dayjs // 创建时间
}

// 支付订单 API
export const EnrollmentOrderApi = {
  // 查询支付订单分页
  getEnrollmentOrderPage: async (params: any) => {
    return await request.get({ url: `/maritime/enrollment-order/page`, params })
  },

  // 查询支付订单详情
  getEnrollmentOrder: async (id: number) => {
    return await request.get({ url: `/maritime/enrollment-order/get?id=` + id })
  },

  // 新增支付订单
  createEnrollmentOrder: async (data: EnrollmentOrder) => {
    return await request.post({ url: `/maritime/enrollment-order/create`, data })
  },

  // 修改支付订单
  updateEnrollmentOrder: async (data: EnrollmentOrder) => {
    return await request.put({ url: `/maritime/enrollment-order/update`, data })
  },

  // 删除支付订单
  deleteEnrollmentOrder: async (id: number) => {
    return await request.delete({ url: `/maritime/enrollment-order/delete?id=` + id })
  },

  /** 批量删除支付订单 */
  deleteEnrollmentOrderList: async (ids: number[]) => {
    return await request.delete({ url: `/maritime/enrollment-order/delete-list?ids=${ids.join(',')}` })
  },

  // 导出支付订单 Excel
  exportEnrollmentOrder: async (params) => {
    return await request.download({ url: `/maritime/enrollment-order/export-excel`, params })
  },

  // 手动确认定金支付（回调异常但实际已付款时使用）
  manualConfirmDeposit: async (orderId: number) => {
    return await request.put({
      url: `/maritime/enrollment-order/manual-confirm?orderId=${orderId}`
    })
  }
}