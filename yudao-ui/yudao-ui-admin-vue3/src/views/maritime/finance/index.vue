<template>
  <ContentWrap>
    <div class="finance-filter">
      <el-radio-group v-model="activePreset" @change="handlePreset">
        <el-radio-button value="today">今日</el-radio-button>
        <el-radio-button value="last7">近 7 天</el-radio-button>
        <el-radio-button value="last30">近 30 天</el-radio-button>
        <el-radio-button value="thisMonth">本月</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        value-format="YYYY-MM-DD"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        :clearable="false"
        class="!w-260px"
        @change="handleRangeChange"
      />
      <el-button type="primary" :loading="loading" @click="loadData">
        <Icon icon="ep:search" class="mr-5px" /> 查询
      </el-button>
    </div>
  </ContentWrap>

  <!-- 资金汇总 -->
  <el-row :gutter="16" class="mb-16px">
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="总流入" :value="summary.totalInflow" :precision="2" prefix="¥" />
        <div class="summary-sub">
          定金 {{ money(summary.depositAmount) }} · 尾款 {{ money(summary.balanceAmount) }} ·
          全款 {{ money(summary.fullAmount) }}
        </div>
      </el-card>
    </el-col>
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="退款支出" :value="summary.totalRefund" :precision="2" prefix="¥" />
        <div class="summary-sub">退款 {{ summary.refundCount || 0 }} 笔</div>
      </el-card>
    </el-col>
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="净流入" :value="summary.netFlow" :precision="2" prefix="¥" />
        <div class="summary-sub">总流入 − 退款支出</div>
      </el-card>
    </el-col>
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="日均净流入" :value="summary.avgDailyNet" :precision="2" prefix="¥" />
        <div class="summary-sub">按所选日期范围计算</div>
      </el-card>
    </el-col>
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="支付订单数" :value="summary.paidOrderCount || 0" suffix="笔" />
        <div class="summary-sub">含定金 / 尾款 / 全款</div>
      </el-card>
    </el-col>
    <el-col :xs="12" :sm="8" :md="6" :lg="4">
      <el-card shadow="hover" class="summary-card">
        <el-statistic title="退款笔数" :value="summary.refundCount || 0" suffix="笔" />
        <div class="summary-sub">已退款到账</div>
      </el-card>
    </el-col>
  </el-row>

  <!-- 趋势图 -->
  <ContentWrap>
    <div class="section-title">每日资金流</div>
    <Echart :height="340" :options="chartOptions" />
  </ContentWrap>

  <!-- 明细 -->
  <ContentWrap>
    <div class="section-title">每日明细</div>
    <el-table
      v-loading="loading"
      :data="dailyList"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="日期" prop="date" width="120" />
      <el-table-column label="定金（元）" align="right" width="120">
        <template #default="{ row }">{{ money(row.depositAmount) }}</template>
      </el-table-column>
      <el-table-column label="尾款（元）" align="right" width="120">
        <template #default="{ row }">{{ money(row.balanceAmount) }}</template>
      </el-table-column>
      <el-table-column label="全款（元）" align="right" width="120">
        <template #default="{ row }">{{ money(row.fullAmount) }}</template>
      </el-table-column>
      <el-table-column label="总流入（元）" align="right" width="130">
        <template #default="{ row }">
          <span class="text-success font-medium">{{ money(row.totalInflow) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款（元）" align="right" width="120">
        <template #default="{ row }">
          <span class="text-danger">{{ money(row.refundAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="净流入（元）" align="right" width="130">
        <template #default="{ row }">
          <span class="font-medium">{{ money(row.netFlow) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付笔数" align="center" width="100" prop="paidOrderCount" />
      <el-table-column label="退款笔数" align="center" width="100" prop="refundCount" />
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts'
import { FinanceApi, type FinanceDailyFlowItem, type FinanceSummary } from '@/api/maritime/finance'

defineOptions({ name: 'MaritimeFinance' })

const loading = ref(false)
const activePreset = ref<string | undefined>('last30')
const dateRange = ref<[string, string]>(buildPresetRange('last30'))
const dailyList = ref<FinanceDailyFlowItem[]>([])
const summary = ref<FinanceSummary>({
  totalInflow: 0,
  depositAmount: 0,
  balanceAmount: 0,
  fullAmount: 0,
  totalRefund: 0,
  netFlow: 0,
  avgDailyNet: 0,
  paidOrderCount: 0,
  refundCount: 0
})

const chartOptions = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['资金流入', '退款支出', '净流入'], top: 0 },
  grid: { left: 16, right: 24, top: 48, bottom: 16, containLabel: true },
  xAxis: {
    type: 'category',
    data: dailyList.value.map((item) => item.date.slice(5)),
    axisLabel: { color: '#6b7280' }
  },
  yAxis: {
    type: 'value',
    name: '元',
    axisLabel: { color: '#6b7280' },
    splitLine: { lineStyle: { color: '#f0f0f0' } }
  },
  series: [
    {
      name: '资金流入',
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: { color: '#409eff' },
      data: dailyList.value.map((item) => item.totalInflow)
    },
    {
      name: '退款支出',
      type: 'bar',
      barMaxWidth: 22,
      itemStyle: { color: '#f56c6c' },
      data: dailyList.value.map((item) => item.refundAmount)
    },
    {
      name: '净流入',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      itemStyle: { color: '#67c23a' },
      lineStyle: { width: 2 },
      data: dailyList.value.map((item) => item.netFlow)
    }
  ]
}))

const money = (value?: number) => {
  const number = Number(value || 0)
  return `¥ ${number.toFixed(2)}`
}

function pad(value: number) {
  return String(value).padStart(2, '0')
}

function toDateString(date: Date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function buildPresetRange(preset: string): [string, string] {
  const today = new Date()
  const end = toDateString(today)
  if (preset === 'today') {
    return [end, end]
  }
  if (preset === 'last7') {
    const start = new Date(today)
    start.setDate(start.getDate() - 6)
    return [toDateString(start), end]
  }
  if (preset === 'thisMonth') {
    return [toDateString(new Date(today.getFullYear(), today.getMonth(), 1)), end]
  }
  const start = new Date(today)
  start.setDate(start.getDate() - 29)
  return [toDateString(start), end]
}

const handlePreset = (preset: string) => {
  dateRange.value = buildPresetRange(preset)
  loadData()
}

const handleRangeChange = () => {
  activePreset.value = undefined
  loadData()
}

const loadData = async () => {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) return
  loading.value = true
  try {
    const data = (await FinanceApi.getDailyFlow({
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })) as unknown as { summary: FinanceSummary; dailyList: FinanceDailyFlowItem[] }
    summary.value = data.summary || summary.value
    dailyList.value = data.dailyList || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.finance-filter {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.summary-card {
  height: 100%;
}
.summary-sub {
  margin-top: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
.section-title {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.text-success {
  color: var(--el-color-success);
}
.text-danger {
  color: var(--el-color-danger);
}
</style>
