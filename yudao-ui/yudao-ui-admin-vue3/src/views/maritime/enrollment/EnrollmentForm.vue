<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="报名状态" prop="enrollmentStatus">
        <el-select v-model="formData.enrollmentStatus" placeholder="请选择报名状态" class="w-full">
          <el-option label="待支付定金" value="PENDING_DEPOSIT" />
          <el-option label="已付定金" value="DEPOSITED" />
          <el-option label="已付清/待开班" value="PAID_OFF" />
          <el-option label="人工处理" value="MANUAL_PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="尾款截止日期" prop="balanceDueDate">
        <el-date-picker
          v-model="formData.balanceDueDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择尾款截止缴纳日期"
        />
      </el-form-item>
      <el-form-item label="取消原因" prop="cancelReason">
        <el-input v-model="formData.cancelReason" placeholder="请输入取消原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { EnrollmentApi, type EnrollmentUpdateReqVO } from '@/api/maritime/enrollment'

/** 报名记录 表单（仅管理员可编辑字段） */
defineOptions({ name: 'EnrollmentForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined as number | undefined,
  enrollmentStatus: undefined as string | undefined,
  balanceDueDate: undefined as string | undefined,
  cancelReason: undefined as string | undefined
})
const formRules = reactive({
  enrollmentStatus: [{ required: true, message: '报名状态不能为空', trigger: 'change' }]
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      const data = await EnrollmentApi.getEnrollment(id)
      formData.value = {
        id: data.id,
        enrollmentStatus: data.enrollmentStatus,
        balanceDueDate: data.balanceDueDate as string | undefined,
        cancelReason: data.cancelReason
      }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  await formRef.value.validate()
  formLoading.value = true
  try {
    await EnrollmentApi.updateEnrollment(formData.value as EnrollmentUpdateReqVO)
    message.success(t('common.updateSuccess'))
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    enrollmentStatus: undefined,
    balanceDueDate: undefined,
    cancelReason: undefined
  }
  formRef.value?.resetFields()
}
</script>
