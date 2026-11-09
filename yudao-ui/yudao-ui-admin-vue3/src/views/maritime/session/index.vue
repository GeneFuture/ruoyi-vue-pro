<template>
  <ContentWrap>
    <!-- 搜索工作栏：常用条件默认展示，次要条件收起 -->
    <el-form
      class="-mb-15px session-search"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="班期编号" prop="sessionCode">
        <el-input
          v-model="queryParams.sessionCode"
          placeholder="请输入班期编号"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="班级名称" prop="sessionName">
        <el-input
          v-model="queryParams.sessionName"
          placeholder="请输入班级名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="班期状态" prop="sessionStatus">
        <el-select
          v-model="queryParams.sessionStatus"
          placeholder="请选择状态"
          clearable
          class="!w-200px"
        >
          <el-option label="草稿" value="DRAFT" />
          <el-option label="招生中" value="OPEN" />
          <el-option label="待开班" value="PENDING_START" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="FINISHED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="开班日期" prop="startDate" v-show="searchExpanded">
        <el-date-picker
          v-model="queryParams.startDate"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime" v-show="searchExpanded">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date(2000, 0, 1, 0, 0, 0), new Date(2000, 0, 1, 23, 59, 59)]"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label-width="0">
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button link type="primary" @click="searchExpanded = !searchExpanded">
          {{ searchExpanded ? '收起' : '展开' }}
          <Icon :icon="searchExpanded ? 'ep:arrow-up' : 'ep:arrow-down'" class="ml-2px" />
          <span v-if="hasAdvancedFilters" class="filter-dot" title="次要条件已生效"></span>
        </el-button>
      </el-form-item>
      <el-form-item label-width="0" class="search-actions">
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['maritime:course-session:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['maritime:course-session:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['maritime:course-session:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
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
      @selection-change="handleRowCheckboxChange"
    >
      <el-table-column type="selection" width="45" />
      <el-table-column label="班级 / 班期编号" min-width="180px">
        <template #default="{ row }">
          <div class="cell-main">{{ row.sessionName || '未命名班级' }}</div>
          <div class="cell-sub">{{ row.sessionCode }}</div>
        </template>
      </el-table-column>
      <el-table-column label="课程" min-width="160px">
        <template #default="{ row }">
          {{ courseNameMap[row.courseId] || `课程#${row.courseId}` }}
        </template>
      </el-table-column>
      <el-table-column label="班期时间" min-width="170px">
        <template #default="{ row }">
          <div class="cell-main">{{ row.startDate }} ~ {{ row.endDate }}</div>
          <div class="cell-sub">共 {{ row.durationDays }} 天</div>
        </template>
      </el-table-column>
      <el-table-column label="讲师" min-width="140px">
        <template #default="{ row }">{{ row.instructorInfo || '—' }}</template>
      </el-table-column>
      <el-table-column label="报名截止" align="center" width="110px">
        <template #default="{ row }">{{ row.enrollmentDeadline || '—' }}</template>
      </el-table-column>
      <el-table-column label="报名进度" align="center" width="120px">
        <template #default="{ row }">
          <div class="cell-main">{{ row.enrolledCount ?? 0 }} / {{ row.maxStudents }}</div>
          <el-progress
            :percentage="enrollPercentage(row)"
            :status="enrollPercentage(row) >= 100 ? 'exception' : undefined"
            :stroke-width="4"
            :show-text="false"
          />
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100px">
        <template #default="{ row }">
          <el-tag :type="SESSION_STATUS_TAG[row.sessionStatus]?.type || 'info'">
            {{ SESSION_STATUS_TAG[row.sessionStatus]?.label || row.sessionStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['maritime:course-session:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['maritime:course-session:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <CourseSessionForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import download from '@/utils/download'
import { CourseSessionApi, CourseSession } from '@/api/maritime/session'
import { CourseApi } from '@/api/maritime/course'
import CourseSessionForm from './CourseSessionForm.vue'

/** 班期管理 列表 */
defineOptions({ name: 'CourseSession' })

/** 班期状态 → 标签文案/颜色 */
type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const SESSION_STATUS_TAG: Record<string, { label: string; type: TagType }> = {
  DRAFT: { label: '草稿', type: 'info' },
  OPEN: { label: '招生中', type: 'success' },
  PENDING_START: { label: '待开班', type: 'warning' },
  IN_PROGRESS: { label: '进行中', type: 'primary' },
  FINISHED: { label: '已完成', type: 'info' },
  CANCELLED: { label: '已取消', type: 'danger' }
}

/** 报名进度百分比（0-100） */
const enrollPercentage = (row: CourseSession): number => {
  const max = row.maxStudents ?? 0
  if (max <= 0) return 0
  return Math.min(100, Math.round(((row.enrolledCount ?? 0) / max) * 100))
}

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const route = useRoute() // 路由参数

const loading = ref(true) // 列表的加载中
const list = ref<CourseSession[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  courseId: undefined as number | undefined, // 从课程管理跳转时预填，不在搜索栏展示
  sessionCode: undefined,
  sessionName: undefined,
  startDate: [],
  sessionStatus: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const searchExpanded = ref(false) // 次要搜索条件是否展开
const exportLoading = ref(false) // 导出的加载中

/** 收起状态下，次要条件是否仍有生效值（圆点提示用户当前结果被过滤） */
const hasAdvancedFilters = computed(
  () =>
    !searchExpanded.value &&
    ((queryParams.startDate?.length ?? 0) > 0 || (queryParams.createTime?.length ?? 0) > 0)
)

const courseNameMap = ref<Record<number, string>>({}) // 课程ID -> 课程名称 缓存

/** 加载当页数据涉及课程名称（增量缓存，避免重复请求） */
const loadCourseNames = async (rows: CourseSession[]) => {
  const ids = [...new Set(rows.map((row) => row.courseId).filter((id): id is number => !!id))]
  const missing = ids.filter((id) => !(id in courseNameMap.value))
  if (missing.length === 0) {
    return
  }
  const fetched = await Promise.all(missing.map((id) => CourseApi.getCourse(id).catch(() => null)))
  const next = { ...courseNameMap.value }
  fetched.forEach((course, index) => {
    if (course) {
      next[missing[index]] = course.name
    }
  })
  courseNameMap.value = next
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await CourseSessionApi.getCourseSessionPage(queryParams)
    list.value = data.list
    total.value = data.total
    await loadCourseNames(data.list)
  } finally {
    loading.value = false
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

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id, queryParams.courseId)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await CourseSessionApi.deleteCourseSession(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除班期管理 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await CourseSessionApi.deleteCourseSessionList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: CourseSession[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await CourseSessionApi.exportCourseSession(queryParams)
    download.excel(data, '班期管理.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(() => {
  // 从课程管理页「添加班期」跳转过来时，带上课程编号：预筛选列表，并自动打开新增表单
  const courseId = route.query.courseId ? Number(route.query.courseId) : undefined
  if (courseId) {
    queryParams.courseId = courseId
    openForm('create')
  }
  getList()
})
</script>

<style scoped>
/* 搜索栏：flex 布局让操作按钮右对齐，展开的次要条件自然换行 */
.session-search {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.session-search .search-actions {
  margin-left: auto;
  margin-right: 0;
}

.filter-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-left: 4px;
  border-radius: 50%;
  background: var(--el-color-warning);
  vertical-align: 2px;
}

.cell-main {
  font-weight: 500;
  line-height: 1.4;
}

.cell-sub {
  margin-top: 2px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
}
</style>
