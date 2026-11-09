<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
      v-loading="formLoading"
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="所属课程" prop="courseId">
            <el-select
              v-model="formData.courseId"
              filterable
              remote
              reserve-keyword
              placeholder="请输入课程名称搜索"
              :remote-method="searchCourse"
              :loading="courseLoading"
              class="!w-full"
            >
              <el-option
                v-for="course in courseList"
                :key="course.id"
                :label="course.name"
                :value="course.id ?? 0"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班期编号">
            <el-input
              :model-value="formType === 'create' ? '' : formData.sessionCode"
              placeholder="保存后由系统自动生成"
              disabled
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班级名称" prop="sessionName">
            <el-input v-model="formData.sessionName" placeholder="选填，如：A班" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="讲师信息" prop="instructorInfo">
            <el-input v-model="formData.instructorInfo" placeholder="选填，如：张船长" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开班日期" prop="startDate">
            <el-date-picker
              v-model="formData.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker
              v-model="formData.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择日期"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="课程天数" prop="durationDays">
            <el-input-number
              :model-value="formData.durationDays"
              :min="1"
              :max="365"
              controls-position="right"
              placeholder="根据开班/结束日期自动计算"
              disabled
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报名截止" prop="enrollmentDeadline">
            <el-date-picker
              v-model="formData.enrollmentDeadline"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选填"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="招生上限" prop="maxStudents">
            <el-input-number
              v-model="formData.maxStudents"
              :min="1"
              :max="9999"
              controls-position="right"
              placeholder="人数"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="班期状态" prop="sessionStatus">
            <el-select v-model="formData.sessionStatus" placeholder="请选择状态" class="!w-full">
              <el-option label="草稿" value="DRAFT" />
              <el-option label="招生中" value="OPEN" />
              <el-option label="待开班" value="PENDING_START" />
              <el-option label="进行中" value="IN_PROGRESS" />
              <el-option label="已完成" value="FINISHED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <Editor v-model="formData.remark" height="200px" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <!-- 费用配置：新增/编辑共用同一面板（新增必填，编辑加载现有配置） -->
    <SessionFeePanel ref="feePanelRef" />
    <!-- 场地信息（学校）：名称/地址/坐标/概况/教学/住宿，同为班期 1:1 附属配置 -->
    <SessionVenuePanel ref="venuePanelRef" />
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import dayjs from 'dayjs'
import { CourseSessionApi } from '@/api/maritime/session'
import { CourseApi, Course } from '@/api/maritime/course'
import SessionFeePanel from './SessionFeePanel.vue'
import SessionVenuePanel from './SessionVenuePanel.vue'

/** 班期管理 表单 */
defineOptions({ name: 'CourseSessionForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
interface CourseSessionFormData {
  id?: number
  courseId?: number
  sessionCode?: string
  sessionName?: string
  durationDays?: number
  instructorInfo?: string
  startDate?: string
  endDate?: string
  enrollmentDeadline?: string
  maxStudents?: number
  sessionStatus?: string
  remark?: string
  enrolledCount?: number
}

const formData = ref<CourseSessionFormData>({
  id: undefined,
  courseId: undefined,
  sessionCode: undefined,
  sessionName: undefined,
  durationDays: undefined,
  instructorInfo: undefined,
  startDate: undefined,
  endDate: undefined,
  enrollmentDeadline: undefined,
  maxStudents: undefined,
  sessionStatus: undefined,
  remark: undefined,
  // 已报名人数为系统维护的缓存值，不在表单展示：新增时固定传 0 兼容旧接口，修改时原值带回
  enrolledCount: undefined
})
const formRules = reactive({
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  durationDays: [{ required: true, message: '请先选择开班日期和结束日期', trigger: 'change' }],
  startDate: [{ required: true, message: '开班日期不能为空', trigger: 'change' }],
  endDate: [
    { required: true, message: '结束日期不能为空', trigger: 'change' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value || !formData.value.startDate) {
          callback()
          return
        }
        if (dayjs(value).isBefore(dayjs(formData.value.startDate), 'day')) {
          callback(new Error('结束日期不能早于开班日期'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  maxStudents: [{ required: true, message: '招生上限不能为空', trigger: 'blur' }],
  sessionStatus: [{ required: true, message: '班期状态不能为空', trigger: 'change' }]
})

/** 课程天数 = 结束日期 - 开班日期 + 1（含首尾两天）；日期不完整或倒挂时置空 */
const syncDurationDays = () => {
  const { startDate, endDate } = formData.value
  if (!startDate || !endDate || dayjs(endDate).isBefore(dayjs(startDate), 'day')) {
    formData.value.durationDays = undefined
    return
  }
  formData.value.durationDays = dayjs(endDate).diff(dayjs(startDate), 'day') + 1
}

// 表单加载、重置以及用户改日期时都重新计算，课程天数不再手工录入
watch([() => formData.value.startDate, () => formData.value.endDate], syncDurationDays)
const formRef = ref() // 表单 Ref
const feePanelRef = ref() // 费用配置面板 Ref
const venuePanelRef = ref() // 场地信息面板 Ref

// 课程搜索相关
const courseLoading = ref(false)
const courseList = ref<Course[]>([])

/** 根据课程名称搜索课程 */
const searchCourse = async (query?: string) => {
  courseLoading.value = true
  try {
    const data = await CourseApi.getCoursePage({
      pageNo: 1,
      pageSize: 20,
      name: query || undefined
    })
    courseList.value = data.list || []
  } finally {
    courseLoading.value = false
  }
}

/** 确保已选中的课程出现在下拉列表中（避免编辑/预填时只显示课程ID） */
const ensureCourseInList = async (courseId: number) => {
  if (courseList.value.some((course) => course.id === courseId)) {
    return
  }
  const course = await CourseApi.getCourse(courseId)
  if (course) {
    courseList.value = [course, ...courseList.value]
  }
}

/** 打开弹窗 */
const open = async (type: string, id?: number, courseId?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  await searchCourse()
  // 面板复用同一实例：先重置，避免残留上一个班期的附属配置数据
  await nextTick()
  feePanelRef.value?.reset()
  venuePanelRef.value?.reset()
  // 新增时，给出合理默认值
  if (type === 'create') {
    formData.value.sessionStatus = 'DRAFT'
    formData.value.enrolledCount = 0
  }
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await CourseSessionApi.getCourseSession(id)
      formData.value = {
        ...data,
        startDate: data.startDate ? dayjs(data.startDate).format('YYYY-MM-DD') : undefined,
        endDate: data.endDate ? dayjs(data.endDate).format('YYYY-MM-DD') : undefined,
        enrollmentDeadline: data.enrollmentDeadline
          ? dayjs(data.enrollmentDeadline).format('YYYY-MM-DD')
          : undefined
      }
      if (formData.value.courseId) {
        await ensureCourseInList(formData.value.courseId)
      }
      // 加载该班期的费用配置与学校信息
      await feePanelRef.value?.load(id)
      await venuePanelRef.value?.load(id)
    } finally {
      formLoading.value = false
    }
  } else if (courseId) {
    // 新增时，预填从课程管理带过来的课程编号
    formData.value.courseId = courseId
    await ensureCourseInList(courseId)
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单（班期字段 + 费用配置 + 学校信息，任一校验失败都中止提交）
  await formRef.value.validate()
  await feePanelRef.value.validate()
  await venuePanelRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      const sessionId = await CourseSessionApi.createCourseSession(formData.value as any)
      // 创建成功后切换为编辑态：若后续附属配置保存失败，重试走更新路径，避免重复创建班期
      formData.value.id = sessionId
      formType.value = 'update'
      dialogTitle.value = t('action.update')
      await feePanelRef.value.save(sessionId)
      await venuePanelRef.value.save(sessionId)
      message.success(t('common.createSuccess'))
    } else {
      await CourseSessionApi.updateCourseSession(formData.value as any)
      await feePanelRef.value.save(formData.value.id!)
      await venuePanelRef.value.save(formData.value.id!)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    courseId: undefined,
    sessionCode: undefined,
    sessionName: undefined,
    durationDays: undefined,
    instructorInfo: undefined,
    startDate: undefined,
    endDate: undefined,
    enrollmentDeadline: undefined,
    maxStudents: undefined,
    sessionStatus: undefined,
    remark: undefined,
    enrolledCount: undefined
  }
  formRef.value?.resetFields()
}
</script>
