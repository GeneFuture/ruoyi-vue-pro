<template>
  <ContentWrap>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- ===== Tab 1: 数据看板 ===== -->
      <el-tab-pane label="数据看板" name="dashboard">
        <div v-loading="dashboardLoading">
          <!-- KPI 卡片 -->
          <el-row :gutter="16" class="mb-20px">
            <el-col :span="24">
              <div class="text-14px text-gray-500 mb-12px font-semibold">今日数据</div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <el-card shadow="hover" class="kpi-card">
                <el-statistic
                  title="今日新报名"
                  :value="dashboard?.todayNewEnrollments ?? 0"
                  suffix="人"
                />
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <el-card shadow="hover" class="kpi-card">
                <el-statistic
                  title="今日定金收款"
                  :value="dashboard?.todayDepositAmount ?? 0"
                  :precision="2"
                  prefix="¥"
                />
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <el-card shadow="hover" class="kpi-card">
                <el-statistic
                  title="今日尾款收款"
                  :value="dashboard?.todayBalanceAmount ?? 0"
                  :precision="2"
                  prefix="¥"
                />
              </el-card>
            </el-col>
            <el-col :span="24" class="mt-16px">
              <div class="text-14px text-gray-500 mb-12px font-semibold">本月数据</div>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <el-card shadow="hover" class="kpi-card">
                <el-statistic
                  title="本月总收款"
                  :value="dashboard?.monthTotalRevenue ?? 0"
                  :precision="2"
                  prefix="¥"
                />
              </el-card>
            </el-col>
            <el-col :xs="12" :sm="8" :md="6" :lg="4">
              <el-card shadow="hover" class="kpi-card">
                <el-statistic
                  title="本月成团数"
                  :value="dashboard?.monthSuccessGroupons ?? 0"
                  suffix="个"
                />
              </el-card>
            </el-col>
          </el-row>

          <!-- 快速待办 -->
          <el-row :gutter="16" class="mb-20px" v-if="dashboard">
            <el-col :span="24">
              <div class="text-14px text-gray-500 mb-12px font-semibold">快速待办</div>
            </el-col>
            <el-col :xs="12" :sm="24">
              <el-card shadow="hover" class="kpi-card todo-card">
                <el-statistic
                  title="待处理退费"
                  :value="dashboard.pendingRefundApplies ?? 0"
                  suffix="笔"
                />
                <div class="todo-tip text-warning">需处理</div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <!-- 近7天报名趋势 -->
            <el-col :xs="24" :md="12">
              <div class="text-14px text-gray-500 mb-12px font-semibold">近7天报名趋势</div>
              <el-table
                :data="dashboard?.enrollmentTrend ?? []"
                border
                size="small"
                style="width: 100%"
              >
                <el-table-column label="日期" prop="date" align="center" />
                <el-table-column label="新报名数" prop="count" align="center" />
              </el-table>
            </el-col>

            <!-- 开放班期进度 -->
            <el-col :xs="24" :md="12">
              <div class="text-14px text-gray-500 mb-12px font-semibold">开放班期进度</div>
              <el-table
                :data="dashboard?.openSessionProgress ?? []"
                border
                size="small"
                style="width: 100%"
              >
                <el-table-column label="班期名称" prop="sessionName" align="center" min-width="120px" />
                <el-table-column label="已报名" prop="enrolledCount" align="center" width="80px" />
                <el-table-column label="名额上限" prop="maxStudents" align="center" width="80px" />
                <el-table-column label="开课日期" prop="startDate" align="center" width="110px" />
                <el-table-column label="进度" align="center" width="120px">
                  <template #default="scope">
                    <el-progress
                      :percentage="calcProgress(scope.row.enrolledCount, scope.row.maxStudents)"
                      :stroke-width="8"
                      :show-text="false"
                    />
                    <span class="text-12px text-gray-500">
                      {{ scope.row.enrolledCount }}/{{ scope.row.maxStudents }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>

          <!-- 导出工具区 -->
          <el-divider content-position="left" class="mt-20px">导出工具</el-divider>
          <el-row :gutter="24" class="mt-8px">
            <el-col :xs="24" :md="12">
              <div class="text-13px text-gray-500 mb-8px">导出报名名单（按班期）</div>
              <el-space>
                <el-input-number
                  v-model="exportSessionId"
                  :min="1"
                  placeholder="输入班期ID"
                  controls-position="right"
                  class="!w-160px"
                />
                <el-button
                  type="primary"
                  :loading="exportEnrollmentLoading"
                  v-hasPermi="['maritime:enrollment:export']"
                  @click="handleExportEnrollment"
                >
                  <Icon icon="ep:download" class="mr-5px" /> 导出报名名单
                </el-button>
              </el-space>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>

      <!-- ===== Tab 2: 财务汇总 ===== -->
      <el-tab-pane label="财务汇总" name="finance">
        <div v-loading="financeLoading">
          <el-space class="mb-20px">
            <el-date-picker
              v-model="financeMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择月份"
              class="!w-160px"
            />
            <el-button type="primary" @click="loadFinanceSummary">查询</el-button>
          </el-space>

          <template v-if="financeSummary">
            <!-- 月度汇总 -->
            <div class="text-14px text-gray-500 mb-12px font-semibold">
              {{ financeMonth }} 财务汇总
            </div>
            <el-row :gutter="16" class="mb-20px">
              <el-col :xs="12" :sm="8" :md="4">
                <el-card shadow="hover" class="kpi-card">
                  <el-statistic
                    title="定金收款(元)"
                    :value="financeSummary.totalDeposit ?? 0"
                    :precision="2"
                  />
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="8" :md="4">
                <el-card shadow="hover" class="kpi-card">
                  <el-statistic
                    title="尾款收款(元)"
                    :value="financeSummary.totalBalance ?? 0"
                    :precision="2"
                  />
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="8" :md="4">
                <el-card shadow="hover" class="kpi-card">
                  <el-statistic
                    title="退款金额(元)"
                    :value="financeSummary.totalRefund ?? 0"
                    :precision="2"
                  />
                </el-card>
              </el-col>
              <el-col :xs="12" :sm="8" :md="4">
                <el-card shadow="hover" class="kpi-card">
                  <el-statistic
                    title="净收款(元)"
                    :value="financeSummary.netRevenue ?? 0"
                    :precision="2"
                  />
                </el-card>
              </el-col>
            </el-row>

            <!-- 班期明细 -->
            <div class="text-14px text-gray-500 mb-12px font-semibold">班期收款明细</div>
            <el-table
              :data="financeSummary.sessionDetails ?? []"
              border
              style="width: 100%"
            >
              <el-table-column label="班期" prop="sessionName" align="center" min-width="160px">
                <template #default="scope">
                  <div>{{ scope.row.sessionName || '-' }}</div>
                  <div class="text-12px text-gray-500">{{ scope.row.sessionCode || '-' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="报名人数" prop="enrollmentCount" align="center" width="90px" />
              <el-table-column label="定金收款(元)" prop="depositAmount" align="center" width="120px" />
              <el-table-column label="尾款收款(元)" prop="balanceAmount" align="center" width="120px" />
              <el-table-column label="全款收款(元)" prop="fullAmount" align="center" width="120px" />
              <el-table-column label="退款(元)" prop="refundAmount" align="center" width="100px" />
              <el-table-column label="净收款(元)" prop="netAmount" align="center" width="110px" />
            </el-table>
          </template>
          <el-empty v-else description="请选择月份后查询" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script setup lang="ts">
import { StatsApi } from '@/api/maritime/stats'
import download from '@/utils/download'

/** 数据统计看板 */
defineOptions({ name: 'MaritimeStats' })

const message = useMessage() // 消息弹窗

const activeTab = ref('dashboard')

// ===== 看板 =====
const dashboard = ref<any>(null)
const dashboardLoading = ref(false)

// ===== 财务汇总 =====
const financeMonth = ref('')
const financeSummary = ref<any>(null)
const financeLoading = ref(false)

// ===== 导出报名名单 =====
const exportSessionId = ref<number | undefined>()
const exportEnrollmentLoading = ref(false)

/** 加载看板数据 */
const loadDashboard = async () => {
  dashboardLoading.value = true
  try {
    dashboard.value = await StatsApi.getDashboard()
  } finally {
    dashboardLoading.value = false
  }
}

/** 加载财务汇总 */
const loadFinanceSummary = async () => {
  if (!financeMonth.value) {
    message.warning('请先选择月份')
    return
  }
  financeLoading.value = true
  try {
    financeSummary.value = await StatsApi.getFinanceSummary(financeMonth.value)
  } finally {
    financeLoading.value = false
  }
}

/** 导出报名名单 */
const handleExportEnrollment = async () => {
  if (!exportSessionId.value) {
    message.warning('请输入班期ID')
    return
  }
  exportEnrollmentLoading.value = true
  try {
    const data = await StatsApi.exportEnrollment(exportSessionId.value)
    download.excel(data, '报名名单.xls')
  } catch {
    message.error('导出失败，请稍后重试')
  } finally {
    exportEnrollmentLoading.value = false
  }
}

/** 计算进度百分比 */
const calcProgress = (enrolled: number, max: number): number => {
  if (!max || max <= 0) return 0
  return Math.min(Math.round((enrolled / max) * 100), 100)
}

/** 初始化 */
onMounted(() => {
  loadDashboard()
  const now = new Date()
  financeMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
})
</script>

<style scoped>
.kpi-card {
  text-align: center;
  margin-bottom: 12px;
}

.todo-card {
  position: relative;
}

.todo-tip {
  font-size: 12px;
  margin-top: 4px;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.rank-gold {
  font-size: 20px;
}

.rank-silver {
  font-size: 20px;
}

.rank-bronze {
  font-size: 20px;
}
</style>
