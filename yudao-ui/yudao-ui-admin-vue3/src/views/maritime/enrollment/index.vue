<template>
  <ContentWrap>
    <!-- 搜索：只保留人工排查真正会用到的条件，按栅格排列 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="82px"
      class="enrollment-query-form"
    >
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="报名单号" prop="enrollmentNo">
            <el-input
              v-model="queryParams.enrollmentNo"
              placeholder="请输入报名单号"
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
          <el-form-item label="报名状态" prop="enrollmentStatus">
            <el-select
              v-model="queryParams.enrollmentStatus"
              placeholder="全部状态"
              clearable
              class="!w-full"
            >
              <el-option label="待支付定金" value="PENDING_DEPOSIT" />
              <el-option label="已付定金" value="DEPOSITED" />
              <el-option label="已付清/待开班" value="PAID_OFF" />
              <el-option label="人工处理" value="MANUAL_PROCESSING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="班期" prop="sessionId">
            <el-select
              v-model="queryParams.sessionId"
              placeholder="全部班期"
              clearable
              filterable
              class="!w-full"
            >
              <el-option
                v-for="item in sessionOptions"
                :key="item.id"
                :label="sessionOptionLabel(item)"
                :value="item.id"
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
        <div class="search-action-right">
          <el-button
            type="success"
            plain
            :loading="exportLoading"
            @click="handleExport"
            v-hasPermi="['maritime:enrollment:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <!-- 列表：不再直接展示 memberId / sessionId / grouponRecordId 等内部 ID -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="报名单号" prop="enrollmentNo" width="180" />
      <el-table-column label="学员" min-width="150">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.realName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ row.phone || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="课程 / 班期" min-width="240">
        <template #default="{ row }">
          <div class="text-14px">{{ row.courseName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ sessionDisplay(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="金额（元）" width="150" align="right">
        <template #default="{ row }">
          <div>应缴 {{ money(row.totalAmount) }}</div>
          <div class="text-12px text-gray-500">已缴 {{ money(row.paidAmount) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="报名状态" width="110" align="center">
        <template #default="{ row }">
          <el-tooltip v-if="row.cancelReason" :content="row.cancelReason" placement="top">
            <el-tag :type="statusTagType(row.enrollmentStatus)">
              {{ statusText(row.enrollmentStatus) }}
            </el-tag>
          </el-tooltip>
          <el-tag v-else :type="statusTagType(row.enrollmentStatus)">
            {{ statusText(row.enrollmentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="拼团" width="150" align="center">
        <template #default="{ row }">
          <template v-if="row.grouponInviteCode">
            <div class="text-13px">{{ row.grouponInviteCode }}</div>
            <el-tag v-if="row.grouponStatus" size="small" :type="grouponTagType(row.grouponStatus)">
              {{ grouponStatusText(row.grouponStatus) }}
            </el-tag>
          </template>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      <el-table-column
        label="报名时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
          <el-button
            link
            type="primary"
            @click="openForm(row.id)"
            v-hasPermi="['maritime:enrollment:update']"
          >
            编辑
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

  <!-- 报名详情：表格里省略的金额明细、身份证脱敏、取消原因放这里 -->
  <Dialog v-model="detailVisible" title="报名详情" width="720px">
    <el-descriptions v-if="currentDetail" :column="2" border>
      <el-descriptions-item label="报名单号">
        {{ currentDetail.enrollmentNo || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="报名状态">
        <el-tag :type="statusTagType(currentDetail.enrollmentStatus)">
          {{ statusText(currentDetail.enrollmentStatus) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="学员姓名">
        {{ currentDetail.realName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="手机号">
        {{ currentDetail.phone || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="身份证号">
        {{ currentDetail.idCardMasked || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="报名时间">
        {{ currentDetail.createTime ? formatDate(currentDetail.createTime) : '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="课程名称" :span="2">
        {{ currentDetail.courseName || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="班期" :span="2">
        {{ sessionDisplay(currentDetail) }}
      </el-descriptions-item>
      <el-descriptions-item label="应缴总金额">
        {{ money(currentDetail.totalAmount) }}
      </el-descriptions-item>
      <el-descriptions-item label="已支付">
        {{ money(currentDetail.paidAmount) }}
      </el-descriptions-item>
      <el-descriptions-item label="定金">
        {{ money(currentDetail.depositAmountSnapshot) }}
      </el-descriptions-item>
      <el-descriptions-item label="尾款">
        {{ money(currentDetail.balanceAmount) }}
      </el-descriptions-item>
      <el-descriptions-item label="尾款截止日期">
        {{ currentDetail.balanceDueDate || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="拼团">
        {{ grouponDisplay(currentDetail) }}
      </el-descriptions-item>
      <el-descriptions-item v-if="currentDetail.cancelReason" label="取消原因" :span="2">
        {{ currentDetail.cancelReason }}
      </el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="detailVisible = false">关 闭</el-button>
    </template>
  </Dialog>

  <!-- 编辑弹窗 -->
  <EnrollmentForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter, formatDate } from '@/utils/formatTime'
import download from '@/utils/download'
import { EnrollmentApi, type Enrollment } from '@/api/maritime/enrollment'
import { CourseSessionApi, type CourseSession } from '@/api/maritime/session'
import EnrollmentForm from './EnrollmentForm.vue'

/** 报名记录 列表 */
defineOptions({ name: 'Enrollment' })

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref<Enrollment[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  enrollmentNo: undefined,
  realName: undefined,
  phone: undefined,
  enrollmentStatus: undefined,
  sessionId: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const sessionOptions = ref<CourseSession[]>([])

const statusText = (status?: string) => {
  const map: Record<string, string> = {
    PENDING_DEPOSIT: '待支付定金',
    DEPOSITED: '已付定金',
    PAID_OFF: '已付清/待开班',
    MANUAL_PROCESSING: '人工处理',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status || ''] || status || '-'
}
type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
const statusTagType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    PENDING_DEPOSIT: 'info',
    DEPOSITED: 'warning',
    PAID_OFF: 'success',
    MANUAL_PROCESSING: 'danger',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status || ''] || 'info'
}
const grouponStatusText = (status?: string) => {
  const map: Record<string, string> = {
    IN_PROGRESS: '进行中',
    SUCCESS: '已成团',
    DEGRADED: '已降级'
  }
  return map[status || ''] || status || '-'
}
const grouponTagType = (status?: string): TagType => {
  const map: Record<string, TagType> = {
    IN_PROGRESS: 'warning',
    SUCCESS: 'success',
    DEGRADED: 'info'
  }
  return map[status || ''] || 'info'
}
const money = (value?: string | number) => {
  if (value === null || value === undefined || value === '') return '-'
  const number = Number(value)
  return Number.isFinite(number) ? `¥ ${number.toFixed(2)}` : `¥ ${value}`
}

/** 班期展示：班级名称优先，没有名称时用班期编号 */
const sessionDisplay = (row: Enrollment) => {
  const main = row.sessionName || row.sessionCode || '-'
  const code = row.sessionName && row.sessionCode ? `（${row.sessionCode}）` : ''
  const date = row.sessionStartDate ? ` · ${row.sessionStartDate} 开班` : ''
  return `${main}${code}${date}`
}

const sessionOptionLabel = (session: CourseSession) => {
  const name = [session.sessionCode, session.sessionName].filter(Boolean).join(' · ')
  const date = session.startDate ? String(session.startDate).slice(0, 10) : ''
  return [name || `班期 ${session.id}`, date].filter(Boolean).join(' · ')
}

const grouponDisplay = (row: Enrollment) => {
  if (!row.grouponInviteCode) return '-'
  return row.grouponStatus
    ? `${row.grouponInviteCode}（${grouponStatusText(row.grouponStatus)}）`
    : row.grouponInviteCode
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await EnrollmentApi.getEnrollmentPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 加载班期下拉选项 */
const loadSessionOptions = async () => {
  try {
    const data = await CourseSessionApi.getCourseSessionPage({ pageNo: 1, pageSize: 100 })
    sessionOptions.value = data.list || []
  } catch {
    sessionOptions.value = []
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 编辑操作 */
const formRef = ref()
const openForm = (id: number) => {
  formRef.value.open('update', id)
}

/** 详情弹窗 */
const detailVisible = ref(false)
const currentDetail = ref<Enrollment>()
const openDetail = (row: Enrollment) => {
  currentDetail.value = row
  detailVisible.value = true
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await EnrollmentApi.exportEnrollment(queryParams)
    download.excel(data, '报名记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  getList()
  loadSessionOptions()
})
</script>

<style scoped>
.enrollment-query-form :deep(.el-form-item) {
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
