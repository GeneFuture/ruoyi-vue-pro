<template>
  <ContentWrap>
    <!-- 搜索：只保留支付排查常用条件 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="82px"
      class="order-query-form"
    >
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="订单号" prop="orderNo">
            <el-input
              v-model="queryParams.orderNo"
              placeholder="请输入订单号"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="学员姓名" prop="realName">
            <el-input
              v-model="queryParams.realName"
              placeholder="请输入学员姓名"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="queryParams.phone"
              placeholder="请输入手机号"
              clearable
              maxlength="11"
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="订单类型" prop="orderType">
            <el-select v-model="queryParams.orderType" placeholder="全部类型" clearable class="!w-full">
              <el-option label="定金" value="DEPOSIT" />
              <el-option label="尾款" value="BALANCE" />
              <el-option label="全款" value="FULL" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="订单状态" prop="orderStatus">
            <el-select v-model="queryParams.orderStatus" placeholder="全部状态" clearable class="!w-full">
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已关闭" value="CLOSED" />
              <el-option label="已退款" value="REFUNDED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="支付时间" prop="payTime">
            <el-date-picker
              v-model="queryParams.payTime"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="创建时间" prop="createTime">
            <el-date-picker
              v-model="queryParams.createTime"
              type="daterange"
              value-format="YYYY-MM-DD HH:mm:ss"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="search-action-row">
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
        <div class="search-action-right">
          <el-button
            type="success"
            plain
            :loading="exportLoading"
            @click="handleExport"
            v-hasPermi="['maritime:enrollment-order:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <!-- 列表：不再展示报名ID / 支付单ID 等内部 ID -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="订单号" prop="orderNo" width="200" />
      <el-table-column label="学员 / 报名" min-width="170">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.realName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ row.phone || '-' }}</div>
          <div class="text-12px text-gray-400">{{ row.enrollmentNo || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="课程 / 班期" min-width="220">
        <template #default="{ row }">
          <div class="text-14px">{{ row.courseName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ sessionDisplay(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="订单类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="orderTypeTagType(row.orderType)">
            {{ orderTypeText(row.orderType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额（元）" width="110" align="right">
        <template #default="{ row }">{{ money(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="orderStatusTagType(row.orderStatus)">
            {{ orderStatusText(row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付渠道" width="120" align="center">
        <template #default="{ row }">{{ channelText(row.payChannel) }}</template>
      </el-table-column>
      <el-table-column
        label="支付时间"
        prop="payTime"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column label="操作" fixed="right" width="170">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            v-if="canManualConfirm(row)"
            link
            type="warning"
            @click="handleManualConfirm(row)"
            v-hasPermi="['maritime:enrollment-order:update']"
          >
            手动确认收款
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 支付订单详情 -->
  <Dialog v-model="detailVisible" title="支付订单详情" width="720px">
    <el-descriptions v-if="currentDetail" :column="2" border>
      <el-descriptions-item label="订单号" :span="2">
        {{ currentDetail.orderNo || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="订单类型">
        {{ orderTypeText(currentDetail.orderType) }}
      </el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="orderStatusTagType(currentDetail.orderStatus)">
          {{ orderStatusText(currentDetail.orderStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="学员姓名">{{ currentDetail.realName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ currentDetail.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="报名单号" :span="2">
        {{ currentDetail.enrollmentNo || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="课程名称" :span="2">
        {{ currentDetail.courseName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="班期" :span="2">
        {{ sessionDisplay(currentDetail) }}
      </el-descriptions-item>
      <el-descriptions-item label="应付金额">{{ money(currentDetail.amount) }}</el-descriptions-item>
      <el-descriptions-item label="支付渠道">{{ channelText(currentDetail.payChannel) }}</el-descriptions-item>
      <el-descriptions-item label="支付时间">
        {{ currentDetail.payTime ? formatDate(currentDetail.payTime as string) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="订单过期时间" :span="2">
        {{ currentDetail.expireTime ? formatDate(currentDetail.expireTime as string) : '-' }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="detailVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { dateFormatter, formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { EnrollmentOrderApi, type EnrollmentOrder } from '@/api/maritime/enrollmentOrder'

/** 支付订单 列表 */
defineOptions({ name: 'EnrollmentOrder' })

const message = useMessage()

const loading = ref(true)
const list = ref<EnrollmentOrder[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: undefined,
  realName: undefined,
  phone: undefined,
  orderType: undefined,
  orderStatus: undefined,
  payTime: [],
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'

const orderTypeText = (type?: string) => {
  const map: Record<string, string> = { DEPOSIT: '定金', BALANCE: '尾款', FULL: '全款' }
  return map[type || ''] || type || '-'
}
const orderTypeTagType = (type?: string): TagType => {
  const map: Record<string, TagType> = { DEPOSIT: 'warning', BALANCE: 'primary', FULL: 'success' }
  return map[type || ''] || 'info'
}
const orderStatusText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    CLOSED: '已关闭',
    REFUNDED: '已退款'
  }
  return map[status || ''] || status || '-'
}
const orderStatusTagType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    PENDING: 'warning',
    PAID: 'success',
    CLOSED: 'info',
    REFUNDED: 'danger'
  }
  return map[status || ''] || 'info'
}
const channelText = (channel?: string) => {
  const map: Record<string, string> = {
    wx_lite: '微信小程序',
    wx_app: '微信 App',
    wx_pub: '微信公众号',
    alipay_wap: '支付宝'
  }
  return map[channel || ''] || channel || '-'
}
const money = (value?: number | string) => {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  return Number.isFinite(number) ? `¥ ${number.toFixed(2)}` : `¥ ${value}`
}

const sessionDisplay = (row: EnrollmentOrder) => {
  const main = row.sessionName || row.sessionCode || '-'
  const code = row.sessionName && row.sessionCode ? `（${row.sessionCode}）` : ''
  const date = row.sessionStartDate ? ` · ${row.sessionStartDate} 开班` : ''
  return `${main}${code}${date}`
}
// 定金 / 尾款 / 全款订单都支持线下收款后手动确认
const canManualConfirm = (row: EnrollmentOrder) => row.orderStatus === 'PENDING'

const getList = async () => {
  loading.value = true
  try {
    const data = await EnrollmentOrderApi.getEnrollmentOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const detailVisible = ref(false)
const currentDetail = ref<EnrollmentOrder>()
const openDetail = (row: EnrollmentOrder) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleManualConfirm = async (row: EnrollmentOrder) => {
  try {
    await message.confirm(
      `确认订单「${row.orderNo}」（${orderTypeText(row.orderType)}）已收到款项？确认后会按已支付处理报名。`,
      '手动确认收款'
    )
    await EnrollmentOrderApi.manualConfirmDeposit(row.id)
    message.success('已确认收款')
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await EnrollmentOrderApi.exportEnrollmentOrder(queryParams)
    download.excel(data, '支付订单.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.order-query-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.search-action-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-action-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
</style>
