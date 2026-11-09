# 管理后台（Vue3）开发规范

> 本文件只在 Claude 处理 `yudao-ui/yudao-ui-admin-vue3/` 下的文件时加载；通用的后端约定见仓库根 `CLAUDE.md`。

**项目路径**：`yudao-ui/yudao-ui-admin-vue3/src/`

### API 文件规范（TypeScript）

```ts
// src/api/maritime/course.ts
import request from '@/config/axios'

export interface CourseVO {
  id: number
  name: string
  certificateType: number
  price: number       // 单位：分
  status: number
  createTime: Date
}

export interface CoursePageReqVO extends PageParam {
  name?: string
  certificateType?: number
  status?: number
  createTime?: Date[]
}

// 查询课程分页
export const getCoursePage = (params: CoursePageReqVO) => {
  return request.get({ url: '/admin-api/maritime/course/page', params })
}

// 查询课程详情
export const getCourse = (id: number) => {
  return request.get({ url: '/admin-api/maritime/course/get?id=' + id })
}

// 新增课程
export const createCourse = (data: CourseVO) => {
  return request.post({ url: '/admin-api/maritime/course/create', data })
}

// 修改课程
export const updateCourse = (data: CourseVO) => {
  return request.put({ url: '/admin-api/maritime/course/update', data })
}

// 删除课程
export const deleteCourse = (id: number) => {
  return request.delete({ url: '/admin-api/maritime/course/delete?id=' + id })
}
```

**关键约定：**
- 接口 URL 前缀：Admin 端用 `/admin-api/`
- 每个接口函数单独导出（named export），不用默认导出对象
- VO 类型声明在同一文件顶部
- 分页参数继承 `PageParam`（框架全局类型）

### 列表页（index.vue）规范

```vue
<template>
  <!-- 搜索栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="queryParams.name" clearable placeholder="请输入课程名称" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon class="mr-5px" icon="ep:search" />搜索</el-button>
        <el-button @click="resetQuery"><Icon class="mr-5px" icon="ep:refresh" />重置</el-button>
        <el-button v-hasPermi="['maritime:course:create']" type="primary" @click="openForm(undefined)">
          <Icon class="mr-5px" icon="ep:plus" />新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="课程名称" prop="name" />
      <el-table-column label="价格" prop="price">
        <template #default="{ row }">¥ {{ fenToYuan(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="{ row }">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-hasPermi="['maritime:course:update']" link type="primary" @click="openForm(row.id)">编辑</el-button>
          <el-button v-hasPermi="['maritime:course:delete']" link type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 新增/编辑弹窗 -->
  <CourseForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { getCoursePage, deleteCourse } from '@/api/maritime/course'
  import { DICT_TYPE } from '@/utils/dict'
  import { fenToYuan } from '@/utils'
  import CourseForm from './CourseForm.vue'
  import { ElMessage, ElMessageBox } from 'element-plus'

  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const queryParams = reactive({ pageNo: 1, pageSize: 10, name: '' })
  const queryFormRef = ref()
  const formRef = ref()

  const getList = async () => {
    loading.value = true
    try {
      const data = await getCoursePage(queryParams)
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  const handleQuery = () => { queryParams.pageNo = 1; getList() }
  const resetQuery = () => { queryFormRef.value.resetFields(); handleQuery() }
  const openForm = (id?: number) => formRef.value.open(id)

  const handleDelete = async (id: number) => {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await deleteCourse(id)
    ElMessage.success('删除成功')
    await getList()
  }

  onMounted(() => getList())
</script>
```

### 弹窗表单（XxxForm.vue）规范

```vue
<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="80px">
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)" :key="dict.value" :value="dict.value">
            {{ dict.label }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { createCourse, updateCourse, getCourse } from '@/api/maritime/course'
  import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
  import { ElMessage } from 'element-plus'

  const emit = defineEmits(['success'])
  const dialogVisible = ref(false)
  const dialogTitle = ref('新增课程')
  const formLoading = ref(false)
  const formData = reactive({ id: undefined, name: '', status: 0 })
  const formRules = { name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }] }
  const formRef = ref()

  const open = async (id?: number) => {
    dialogVisible.value = true
    dialogTitle.value = id ? '修改课程' : '新增课程'
    if (id) {
      formLoading.value = true
      try {
        Object.assign(formData, await getCourse(id))
      } finally {
        formLoading.value = false
      }
    }
  }
  defineExpose({ open })

  const submitForm = async () => {
    await formRef.value.validate()
    formLoading.value = true
    try {
      formData.id ? await updateCourse(formData) : await createCourse(formData)
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      emit('success')
    } finally {
      formLoading.value = false
    }
  }
</script>
```

**关键约定：**
- 弹窗用框架封装的 `<Dialog>` 组件（不是 el-dialog）
- 通过 `defineExpose({ open })` 暴露给父组件调用
- 权限控制用 `v-hasPermi="['maritime:course:create']"` 指令
- 字典渲染用 `<dict-tag>` 或 `getIntDictOptions(DICT_TYPE.xxx)`
- 价格显示用工具函数 `fenToYuan()`（分转元）
- 表格分页用 `<Pagination>` 组件（框架封装）
