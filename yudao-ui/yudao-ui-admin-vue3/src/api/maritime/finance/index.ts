import request from '@/config/axios'

/** 资金汇总 */
export interface FinanceSummary {
  totalInflow: number
  depositAmount: number
  balanceAmount: number
  fullAmount: number
  totalRefund: number
  netFlow: number
  avgDailyNet: number
  paidOrderCount: number
  refundCount: number
}

/** 每日资金流 */
export interface FinanceDailyFlowItem {
  date: string
  depositAmount: number
  balanceAmount: number
  fullAmount: number
  totalInflow: number
  refundAmount: number
  netFlow: number
  paidOrderCount: number
  refundCount: number
}

export interface FinanceDailyFlow {
  summary: FinanceSummary
  dailyList: FinanceDailyFlowItem[]
}

export const FinanceApi = {
  // 获得每日资金流与日期范围汇总
  getDailyFlow: async (params: { startDate: string; endDate: string }) => {
    return await request.get({ url: `/maritime/finance/daily-flow`, params })
  }
}
