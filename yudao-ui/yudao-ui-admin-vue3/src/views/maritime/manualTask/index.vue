<template>
  <ContentWrap>
    <!-- 搜索：按学员和任务状态排查，不再输入报名ID/会员ID -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="82px"
      class="manual-task-query-form"
    >
      <el-row :gutter="16">
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
          <el-form-item label="任务类型" prop="taskType">
            <el-select v-model="queryParams.taskType" placeholder="全部类型" clearable class="!w-full">
              <el-option
                v-for="item in taskTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="!w-full">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
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
      </div>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="人工单号" prop="taskNo" width="190" />
      <el-table-column label="学员" min-width="150">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.realName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ row.phone || '-' }}</div>
          <div class="text-12px text-gray-400">{{ row.enrollmentNo || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="课程 / 班期" min-width="200">
        <template #default="{ row }">
          <div class="text-14px">{{ row.courseName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ sessionDisplay(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="任务类型" width="130" align="center">
        <template #default="{ row }">{{ taskTypeText(row.taskType) }}</template>
      </el-table-column>
      <el-table-column label="退费信息" width="140" align="center">
        <template #default="{ row }">
          <template v-if="isRefundTask(row)">
            <div class="text-13px">{{ money(row.refundAmount) }}</div>
            <el-tag v-if="row.applyStatus" size="small" :type="applyStatusTagType(row.applyStatus)">
              {{ applyStatusText(row.applyStatus) }}
            </el-tag>
          </template>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tooltip v-if="row.handleResult" :content="row.handleResult" placement="top">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </el-tooltip>
          <el-tag v-else :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="人工原因" prop="reason" min-width="220" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" :formatter="dateFormatter" width="170" />
      <el-table-column label="操作" fixed="right" width="170">
        <template #default="{ row }">
          <el-button
            v-if="isOpen(row.status)"
            v-hasPermi="['maritime:manual-task:update']"
            link
            type="primary"
            @click="openResolve(row)"
          >
            {{ isRefundTask(row) ? '审核退费' : '处理' }}
          </el-button>
          <el-button
            v-else-if="isRefundTask(row)"
            v-hasPermi="['maritime:manual-task:query']"
            link
            type="primary"
            @click="openRefund(row)"
          >
            查看
          </el-button>
          <span v-else class="text-gray-400">已关闭</span>
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

  <!-- 普通人工单处理弹窗 -->
  <Dialog v-model="resolveVisible" title="处理人工单" width="560px">
    <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="110px">
      <el-form-item label="人工单号">
        <span>{{ currentTask?.taskNo }}</span>
      </el-form-item>
      <el-form-item label="学员">
        <span>{{ currentTask?.realName || '-' }} {{ currentTask?.phone || '' }}</span>
      </el-form-item>
      <el-form-item label="人工原因">
        <span>{{ currentTask?.reason }}</span>
      </el-form-item>
      <el-form-item label="处理结果" prop="targetEnrollmentStatus">
        <el-select v-model="resolveForm.targetEnrollmentStatus" class="w-full">
          <el-option label="已付清/待开班" value="PAID_OFF" />
          <el-option label="恢复为已付定金" value="DEPOSITED" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="取消报名" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理说明" prop="handleResult">
        <el-input
          v-model="resolveForm.handleResult"
          :rows="3"
          placeholder="请填写处理说明，例如：学员已补缴尾款"
          type="textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="submitting" @click="resolveVisible = false">取 消</el-button>
      <el-button :loading="submitting" type="primary" @click="submitResolve">确 定</el-button>
    </template>
  </Dialog>

  <!-- 退费审核弹窗（退费申请已合并到人工处理） -->
  <Dialog v-model="refundVisible" :title="refundDialogTitle" width="620px">
    <el-alert
      title="退款需先在微信商户平台人工完成，再在此登记审核结果；审核通过后会自动取消报名并归还名额"
      type="warning"
      :closable="false"
      show-icon
      class="mb-20px"
    />
    <el-descriptions v-if="currentRefund" :column="2" border class="mb-20px">
      <el-descriptions-item label="人工单号">{{ currentRefund.taskNo }}</el-descriptions-item>
      <el-descriptions-item label="学员">
        {{ currentTask?.realName || '-' }} {{ currentTask?.phone || '' }}
      </el-descriptions-item>
      <el-descriptions-item label="报名单号">{{ currentTask?.enrollmentNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="关联订单">
        {{ currentRefund.orderNo || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="订单金额">
        {{ currentRefund.orderAmount != null ? `¥ ${currentRefund.orderAmount}` : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="申请退款金额">
        {{ currentRefund.refundAmount != null ? `¥ ${currentRefund.refundAmount}` : '未填写' }}
      </el-descriptions-item>
      <el-descriptions-item label="退费原因" :span="2">
        {{ currentRefund.applyReason || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="申请状态" :span="2">
        <el-tag :type="currentRefund.applyStatus === 'PENDING' ? 'warning' : 'info'">
          {{ applyStatusText(currentRefund.applyStatus) }}
        </el-tag>
      </el-descriptions-item>
    </el-descriptions>

    <el-form ref="refundFormRef" :model="refundForm" label-width="110px">
      <el-form-item label="审核结果" required>
        <el-radio-group v-model="refundForm.result" :disabled="!refundEditable">
          <el-radio value="APPROVE">通过并登记退款</el-radio>
          <el-radio value="REJECT">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="refundForm.result === 'APPROVE'" label="实际退款金额" required>
        <el-input-number
          v-model="refundForm.refundAmount"
          :min="0.01"
          :precision="2"
          :disabled="!refundEditable"
          class="!w-200px"
        />
        <span class="ml-10px text-12px text-gray-500">单位：元</span>
      </el-form-item>
      <el-form-item v-if="refundForm.result === 'APPROVE'" label="管理员备注">
        <el-input
          v-model="refundForm.adminRemark"
          :rows="3"
          :disabled="!refundEditable"
          placeholder="选填，例如：同意退款，原路退回"
          type="textarea"
        />
      </el-form-item>
      <el-form-item v-if="refundForm.result === 'REJECT'" label="拒绝原因" required>
        <el-input
          v-model="refundForm.rejectReason"
          :rows="3"
          :disabled="!refundEditable"
          placeholder="请填写拒绝原因"
          type="textarea"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button :disabled="refundSubmitting" @click="refundVisible = false">取 消</el-button>
      <el-button
        :loading="refundSubmitting"
        :disabled="!refundEditable"
        type="primary"
        @click="submitRefund"
      >
        确 定
      </el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import {
  getManualTaskPage,
  getManualTaskRefundDetail,
  resolveManualTask,
  resolveManualTaskRefund,
  type ManualTaskRefundDetailVO,
  type ManualTaskVO
} from '@/api/maritime/manualTask'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'MaritimeManualTask' })

const message = useMessage()

const loading = ref(false)
const list = ref<ManualTaskVO[]>([])
const total = ref(0)
const queryFormRef = ref()
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  realName: undefined,
  phone: undefined,
  taskType: undefined,
  status: undefined,
  createTime: undefined
})

const taskTypeOptions = [
  { label: '尾款逾期', value: 'BALANCE_OVERDUE' },
  { label: '退费申请', value: 'REFUND_APPLY' },
  { label: '退款金额确认', value: 'REFUND_AMOUNT_CONFIRM' },
  { label: '其他', value: 'OTHER' }
]
const statusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'DONE' },
  { label: '已取消', value: 'CANCELLED' }
]

const taskTypeText = (type: string) =>
  taskTypeOptions.find((item) => item.value === type)?.label || type
const statusText = (status: string) =>
  statusOptions.find((item) => item.value === status)?.label || status
const statusTagType = (status: string) => {
  if (status === 'PENDING') return 'warning'
  if (status === 'PROCESSING') return 'primary'
  if (status === 'DONE') return 'success'
  return 'info'
}
const isOpen = (status: string) => ['PENDING', 'PROCESSING'].includes(status)
const isRefundTask = (row: ManualTaskVO) =>
  ['REFUND_APPLY', 'REFUND_AMOUNT_CONFIRM'].includes(row.taskType)
const applyStatusText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    REFUNDED: '已退款'
  }
  return map[status || ''] || status || '-'
}
const applyStatusTagType = (status?: string) => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    PENDING: 'warning',
    APPROVED: 'primary',
    REJECTED: 'info',
    REFUNDED: 'success'
  }
  return map[status || ''] || 'info'
}
const money = (value?: number | string) => {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  return Number.isFinite(number) ? `¥ ${number.toFixed(2)}` : `¥ ${value}`
}
const sessionDisplay = (row: ManualTaskVO) => {
  const main = row.sessionName || row.sessionCode || '-'
  const code = row.sessionName && row.sessionCode ? `（${row.sessionCode}）` : ''
  return `${main}${code}`
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getManualTaskPage(queryParams)
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

const resolveVisible = ref(false)
const currentTask = ref<ManualTaskVO>()
const resolveForm = reactive({
  targetEnrollmentStatus: '',
  handleResult: ''
})
const resolveRules = {
  targetEnrollmentStatus: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  handleResult: [{ required: true, message: '请填写处理说明', trigger: 'blur' }]
}
const resolveFormRef = ref()
const submitting = ref(false)

// 退费审核弹窗
const refundVisible = ref(false)
const currentRefund = ref<ManualTaskRefundDetailVO>()
const refundForm = reactive({
  result: 'APPROVE' as 'APPROVE' | 'REJECT',
  refundAmount: undefined as number | undefined,
  adminRemark: '',
  rejectReason: ''
})
const refundSubmitting = ref(false)

const refundEditable = computed(
  () =>
    currentRefund.value?.applyStatus === 'PENDING' &&
    ['PENDING', 'PROCESSING'].includes(currentRefund.value?.taskStatus || '')
)
const refundDialogTitle = computed(() => (refundEditable.value ? '退费审核' : '退费详情'))

const openResolve = async (row: ManualTaskVO) => {
  if (isRefundTask(row)) {
    await openRefund(row)
    return
  }
  currentTask.value = row
  resolveForm.targetEnrollmentStatus = row.taskType === 'BALANCE_OVERDUE' ? 'PAID_OFF' : ''
  resolveForm.handleResult = ''
  resolveVisible.value = true
}

const openRefund = async (row: ManualTaskVO) => {
  try {
    const detail = await getManualTaskRefundDetail(row.id)
    currentTask.value = row
    currentRefund.value = detail
    refundForm.result = 'APPROVE'
    refundForm.refundAmount = detail.refundAmount || undefined
    refundForm.adminRemark = ''
    refundForm.rejectReason = ''
    refundVisible.value = true
  } catch {
    message.error('退费申请加载失败，请稍后重试')
  }
}

const submitRefund = async () => {
  if (refundForm.result === 'APPROVE' && !refundForm.refundAmount) {
    message.warning('请输入实际退款金额')
    return
  }
  if (refundForm.result === 'REJECT' && !refundForm.rejectReason.trim()) {
    message.warning('请输入拒绝原因')
    return
  }
  refundSubmitting.value = true
  try {
    await resolveManualTaskRefund({
      id: currentRefund.value!.taskId,
      result: refundForm.result,
      refundAmount: refundForm.result === 'APPROVE' ? refundForm.refundAmount : undefined,
      adminRemark: refundForm.result === 'APPROVE' ? refundForm.adminRemark : undefined,
      rejectReason: refundForm.result === 'REJECT' ? refundForm.rejectReason : undefined
    })
    message.success('审核成功')
    refundVisible.value = false
    await getList()
  } finally {
    refundSubmitting.value = false
  }
}

const submitResolve = async () => {
  await resolveFormRef.value.validate()
  submitting.value = true
  try {
    await resolveManualTask({
      id: currentTask.value!.id,
      targetEnrollmentStatus: resolveForm.targetEnrollmentStatus,
      handleResult: resolveForm.handleResult
    })
    message.success('处理成功')
    resolveVisible.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

onMounted(() => getList())
</script>

<style scoped>
.manual-task-query-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.search-action-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
