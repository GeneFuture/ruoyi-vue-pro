<template>
  <ContentWrap>
    <!-- 搜索 -->
    <el-form
      ref="queryFormRef"
      :model="queryParams"
      label-width="82px"
      class="groupon-member-query-form"
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
          <el-form-item label="成员状态" prop="memberStatus">
            <el-select v-model="queryParams.memberStatus" placeholder="全部状态" clearable class="!w-full">
              <el-option label="正常" value="ACTIVE" />
              <el-option label="已退出" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-form-item label="加入时间" prop="joinTime">
            <el-date-picker
              v-model="queryParams.joinTime"
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
            v-hasPermi="['maritime:groupon-member:export']"
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
      <el-table-column label="拼团邀请码" width="160">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.inviteCode || '-' }}</div>
          <el-tag v-if="row.grouponStatus" size="small" :type="grouponStatusTagType(row.grouponStatus)">
            {{ grouponStatusText(row.grouponStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="课程 / 班期" min-width="220">
        <template #default="{ row }">
          <div class="text-14px">{{ row.courseName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ sessionDisplay(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="学员" min-width="160">
        <template #default="{ row }">
          <div class="font-medium text-14px">{{ row.realName || '-' }}</div>
          <div class="text-12px text-gray-500">{{ row.phone || '-' }}</div>
          <div class="text-12px text-gray-400">{{ row.enrollmentNo || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="成员状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.memberStatus === 'ACTIVE' ? 'success' : 'info'">
            {{ row.memberStatus === 'ACTIVE' ? '正常' : '已退出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="加入时间"
        prop="joinTime"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column
        label="定金支付时间"
        prop="depositPaidAt"
        :formatter="dateFormatter"
        width="170"
      />
      <el-table-column
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="170"
      />
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
import { GrouponMemberApi, type GrouponMember } from '@/api/maritime/grouponMember'

/** 拼团成员 列表 */
defineOptions({ name: 'GrouponMember' })

const message = useMessage()

const loading = ref(true)
const list = ref<GrouponMember[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  inviteCode: undefined,
  realName: undefined,
  phone: undefined,
  memberStatus: undefined,
  joinTime: [],
  createTime: []
})
const queryFormRef = ref()
const exportLoading = ref(false)

const grouponStatusText = (status?: string) => {
  const map: Record<string, string> = {
    IN_PROGRESS: '拼团中',
    SUCCESS: '已成团',
    DEGRADED: '已降级'
  }
  return map[status || ''] || status || '-'
}
const grouponStatusTagType = (status?: string) => {
  const map: Record<string, 'primary' | 'success' | 'info' | 'warning' | 'danger'> = {
    IN_PROGRESS: 'warning',
    SUCCESS: 'success',
    DEGRADED: 'info'
  }
  return map[status || ''] || 'info'
}
const sessionDisplay = (row: GrouponMember) => {
  const main = row.sessionName || row.sessionCode || '-'
  const code = row.sessionName && row.sessionCode ? `（${row.sessionCode}）` : ''
  const date = row.sessionStartDate ? ` · ${row.sessionStartDate} 开班` : ''
  return `${main}${code}${date}`
}

const getList = async () => {
  loading.value = true
  try {
    const data = await GrouponMemberApi.getGrouponMemberPage(queryParams)
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

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await GrouponMemberApi.exportGrouponMember(queryParams)
    download.excel(data, '拼团成员.xls')
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
.groupon-member-query-form :deep(.el-form-item) {
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
