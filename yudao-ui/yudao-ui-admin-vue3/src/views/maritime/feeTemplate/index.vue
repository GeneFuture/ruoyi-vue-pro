<template>
  <ContentWrap>
    <!-- 搜索工作栏：常用条件默认展示，次要条件收起 -->
    <el-form
      class="-mb-15px template-search"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="模板名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="开启拼团" prop="isGrouponEnabled">
        <el-select
          v-model="queryParams.isGrouponEnabled"
          placeholder="全部"
          clearable
          class="!w-140px"
        >
          <el-option label="已开启" :value="true" />
          <el-option label="未开启" :value="false" />
        </el-select>
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
          v-hasPermi="['maritime:fee-template:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
        <el-button
          type="danger"
          plain
          :disabled="isEmpty(checkedIds)"
          @click="handleDeleteBatch"
          v-hasPermi="['maritime:fee-template:delete']"
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
      <el-table-column label="模板名称" min-width="160px">
        <template #default="{ row }">
          <div class="cell-main">{{ row.name }}</div>
        </template>
      </el-table-column>
      <el-table-column label="学费 / 定金" align="center" min-width="120px">
        <template #default="{ row }">
          <div class="cell-main">{{ fmtMoney(row.tuitionAmount) }}</div>
          <div class="cell-sub">定金 {{ fmtMoney(row.depositAmount) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="另行缴费" min-width="160px">
        <template #default="{ row }">{{ extraFeeSummary(row) || '—' }}</template>
      </el-table-column>
      <el-table-column label="拼团方案" min-width="200px">
        <template #default="{ row }">
          <template v-if="row.isGrouponEnabled">
            <div class="cell-main">
              减 {{ fmtMoney(row.grouponDiscountAmount) }} · {{ row.grouponRequiredCount }}人成团
            </div>
            <div class="cell-sub">{{ row.grouponExpireHours }}h 有效 · 未成团按原价</div>
          </template>
          <el-tag v-else type="info">未开启</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="定金可退" align="center" width="90px">
        <template #default="{ row }">
          <el-tag :type="row.isDepositRefundable ? 'success' : 'info'">
            {{ row.isDepositRefundable ? '可退' : '不可退' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="尾款截止" align="center" width="100px">
        <template #default="{ row }">开班前 {{ row.balanceDueDaysBeforeStart }} 天</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80px">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120px" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['maritime:fee-template:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['maritime:fee-template:delete']"
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
  <FeeTemplateForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { isEmpty } from '@/utils/is'
import { FeeTemplateApi, FeeTemplate } from '@/api/maritime/feeTemplate'
import FeeTemplateForm from './FeeTemplateForm.vue'

/** 费用模板 列表 */
defineOptions({ name: 'FeeTemplate' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<FeeTemplate[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined,
  isGrouponEnabled: undefined,
  createTime: []
})
const queryFormRef = ref() // 搜索的表单
const searchExpanded = ref(false) // 次要搜索条件是否展开

/** 收起状态下，次要条件是否仍有生效值（圆点提示用户当前结果被过滤） */
const hasAdvancedFilters = computed(
  () => !searchExpanded.value && (queryParams.createTime?.length ?? 0) > 0
)

/** 金额展示 */
const fmtMoney = (value?: number) => (value == null ? '—' : `¥${Number(value).toFixed(2)}`)

/** 另行缴费项目 → 摘要文本（如：教材费 ¥200.00（必须）） */
const extraFeeSummary = (row: FeeTemplate): string => {
  const items = row.extraFeeItems ?? []
  return items
    .map((item) => {
      const amount = item.amount == null ? '' : ` ¥${Number(item.amount).toFixed(2)}`
      return `${item.name}${amount}${item.required ? '（必须）' : ''}`
    })
    .join('，')
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await FeeTemplateApi.getFeeTemplatePage(queryParams)
    list.value = data.list
    total.value = data.total
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
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await FeeTemplateApi.deleteFeeTemplate(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 批量删除费用模板 */
const handleDeleteBatch = async () => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    await FeeTemplateApi.deleteFeeTemplateList(checkedIds.value)
    checkedIds.value = []
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const checkedIds = ref<number[]>([])
const handleRowCheckboxChange = (records: FeeTemplate[]) => {
  checkedIds.value = records.map((item) => item.id!)
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 搜索栏：flex 布局让操作按钮右对齐，展开的次要条件自然换行 */
.template-search {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

.template-search .search-actions {
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
