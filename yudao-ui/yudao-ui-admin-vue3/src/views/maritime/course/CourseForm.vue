<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      v-loading="formLoading"
    >
      <el-form-item label="课程名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入课程名称" />
      </el-form-item>
      <el-form-item label="证书类型" prop="certificateType">
        <el-input v-model="formData.certificateType" placeholder="如 STCW / STCW-AT / 内河 等" />
      </el-form-item>
      <el-form-item label="封面图片" prop="coverImage">
        <div>
          <div class="flex flex-wrap items-start gap-16px">
            <!-- 预览框按详情页头图比例（750:420 ≈ 1.79:1），而不是默认的 150×150 方形 -->
            <UploadImg v-model="formData.coverImage" width="320px" height="180px" />
            <!-- 课程列表拿这张图按中心裁成方形缩略图，这里用 object-cover 还原同样的裁法 -->
            <div v-if="formData.coverImage" class="flex flex-col items-center gap-4px">
              <img
                :src="formData.coverImage"
                alt="列表缩略图预览"
                class="w-96px h-96px object-cover rounded-8px border border-solid border-gray-300"
              />
              <span class="text-12px text-gray-500">列表缩略图</span>
            </div>
          </div>
          <div class="mt-8px text-12px leading-relaxed text-gray-500">
            建议 1280 × 720（16:9），主体放中间：详情页头图按宽幅裁，首页课程列表按中心裁成正方形。
          </div>
        </div>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">上架</el-radio>
          <el-radio :value="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序权重" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          controls-position="right"
          placeholder="数值越大越靠前"
          class="!w-200px"
        />
      </el-form-item>
      <el-form-item label="报名条件说明" prop="enrollmentConditionText">
        <el-input
          v-model="formData.enrollmentConditionText"
          type="textarea"
          :rows="3"
          placeholder="请输入报名条件说明"
        />
      </el-form-item>
      <el-form-item label="课程详情" prop="description">
        <Editor v-model="formData.description" height="300px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { CourseApi, Course } from '@/api/maritime/course'

/** 课程管理 表单 */
defineOptions({ name: 'CourseForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗是否展示
const dialogTitle = ref('') // 弹窗标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单类型：create - 新增；update - 修改
const formData = ref<Course>({
  name: '',
  certificateType: '',
  coverImage: undefined,
  description: undefined,
  enrollmentConditionText: undefined,
  status: 1,
  sortOrder: 0
})
const formRules = reactive({
  name: [{ required: true, message: '课程名称不能为空', trigger: 'blur' }],
  certificateType: [{ required: true, message: '证书类型不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  formData.value = {
    name: '',
    certificateType: '',
    coverImage: undefined,
    description: undefined,
    enrollmentConditionText: undefined,
    status: 1,
    sortOrder: 0
  }
  dialogTitle.value = t('action.' + type)
  formType.value = type
  dialogVisible.value = true
  await nextTick()
  formRef.value?.resetFields()
  // 修改时，加载数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await CourseApi.getCourse(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await CourseApi.createCourse(formData.value)
      message.success(t('common.createSuccess'))
    } else {
      await CourseApi.updateCourse(formData.value)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

</script>
