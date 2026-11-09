import request from '@/config/axios'

/** 统计数据 API */
export const StatsApi = {
  // 获取数据看板（今日/本月 KPI、待办事项、近7天趋势、开放班期进度）
  getDashboard: async (date?: string) => {
    return await request.get({ url: `/maritime/stats/dashboard`, params: date ? { date } : {} })
  },

  // 导出报名名单 Excel（按班期）
  exportEnrollment: async (sessionId: number) => {
    return await request.download({
      url: `/maritime/stats/export-enrollment`,
      params: { sessionId }
    })
  },

  // 获取财务汇总（按月）
  getFinanceSummary: async (month: string) => {
    return await request.get({ url: `/maritime/stats/finance-summary`, params: { month } })
  }
}
