<template>
  <ContentWrap>
    <!-- 搜索 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="82px"
      class="groupon-query-form"
    >
      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="拼团邀请码" prop="inviteCode">
            <el-input
              v-model="queryParams.inviteCode"
              placeholder="请输入拼团邀请码"
              clearable
              class="!w-full"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="拼团状态" prop="grouponStatus">
            <el-select v-model="queryParams.grouponStatus" placeholder="全部状态" clearable class="!w-full">
              <el-option label="拼团中" value="IN_PROGRESS" />
              <el-option label="已成团" value="SUCCESS" />
              <el-option label="已降级" value="DEGRADED" />
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
          <el-form-item label="截止时间" prop="expireTime">
            <el-date-picker
              v-model="queryParams.expireTime"
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
            v-hasPermi="['maritime:groupon-record:export']"
          >
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
      </div>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="拼团邀请码" prop="inviteCode" width="150" />
      <el-table-column label="课程 / 班期" min-width="220">
        <template #default="{ row }">
          <div class="text-14px">{{ row.courseName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ sessionDisplay(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="发起人" min-width="160">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.initiatorRealName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ row.initiatorPhone || '-' }}</div>
          <div class="text-12px text-gray-400">{{ row.initiatorEnrollmentNo || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="人数" width="100" align="center">
        <template #default="{ row }">
          <span class="font-medium">{{ row.currentCount || 0 }}</span>
          <span class="text-gray-400"> / {{ row.requiredCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="拼团状态" width="110" align="center">
        <template #default="{ row }">
          <el-tooltip v-if="row.degradeReason" :content="row.degradeReason" placement="top">
            <el-tag :type="statusTagType(row.grouponStatus)">
              {{ statusText(row.grouponStatus) }}
            </el-tag>
          </el-tooltip>
          <el-tag v-else :type="statusTagType(row.grouponStatus)">
            {{ statusText(row.grouponStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="截止时间"
        prop="expireTime"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column
        label="成团时间"
        prop="successTime"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{ row }">
          <template v-if="row.grouponStatus === 'IN_PROGRESS'">
            <el-button
              link
              type="success"
              @click="handleForceSucceed(row)"
              v-hasPermi="['maritime:groupon-record:update']"
            >
              强制成团
            </el-button>
            <el-button
              link
              type="warning"
              @click="handleClose(row)"
              v-hasPermi="['maritime:groupon-record:update']"
            >
              关闭拼团
            </el-button>
          </template>
          <span v-else class="text-gray-400">已结束</span>
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
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { GrouponRecordApi, type GrouponRecord } from '@/api/maritime/grouponRecord'
import { CourseSessionApi, type CourseSession } from '@/api/maritime/session'

/** 拼团记录 列表 */
defineOptions({ name: 'GrouponRecord' })

const message = useMessage()

const loading = ref(true)
const list = ref<GrouponRecord[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  inviteCode: undefined,
  grouponStatus: undefined,
  sessionId: undefined,
  expireTime: [],
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)
const sessionOptions = ref<CourseSession[]>([])

const statusText = (status?: string) => {
  const map: Record<string, string> = {
    IN_PROGRESS: '拼团中',
    SUCCESS: '已成团',
    DEGRADED: '已降级'
  }
  return map[status || ''] || status || '-'
}
const statusTagType = (status?: string) => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    IN_PROGRESS: 'warning',
    SUCCESS: 'success',
    DEGRADED: 'info'
  }
  return map[status || ''] || 'info'
}
const sessionDisplay = (row: GrouponRecord) => {
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

const getList = async () => {
  loading.value = true
  try {
    const data = await GrouponRecordApi.getGrouponRecordPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadSessionOptions = async () => {
  try {
    const data = await CourseSessionApi.getCourseSessionPage({ pageNo: 1, pageSize: 100 })
    sessionOptions.value = data.list || []
  } catch {
    sessionOptions.value = []
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

const handleForceSucceed = async (row: GrouponRecord) => {
  try {
    await message.confirm(`确认强制让邀请码「${row.inviteCode}」的拼团成团吗？`, '强制成团')
    await GrouponRecordApi.forceSucceedGroupon(row.id)
    message.success('已强制成团')
    await getList()
  } catch {}
}

const handleClose = async (row: GrouponRecord) => {
  try {
    await message.confirm(
      `确认关闭邀请码「${row.inviteCode}」的拼团吗？关闭后成员按降级规则处理。`,
      '关闭拼团'
    )
    await GrouponRecordApi.closeGroupon(row.id)
    message.success('已关闭拼团')
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await GrouponRecordApi.exportGrouponRecord(queryParams)
    download.excel(data, '拼团记录.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  getList()
  loadSessionOptions()
})
</script>

<style scoped>
.groupon-query-form :deep(.el-form-item) {
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
