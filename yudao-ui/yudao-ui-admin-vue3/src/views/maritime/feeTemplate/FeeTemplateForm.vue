<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="720px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-divider content-position="left">模板信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="模板名称" prop="name">
            <el-input v-model="formData.name" placeholder="如：STCW 标准收费" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">启用</el-radio>
              <el-radio :value="1">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">费用方案</el-divider>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="学费总额" prop="tuitionAmount">
            <el-input-number
              v-model="formData.tuitionAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              placeholder="元"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="含住宿费" prop="tuitionIncludesAccommodation">
            <el-radio-group v-model="formData.tuitionIncludesAccommodation">
              <el-radio :value="true">含住宿</el-radio>
              <el-radio :value="false">不含住宿</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="定金金额" prop="depositAmount">
            <el-input-number
              v-model="formData.depositAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              placeholder="元"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="定金可退" prop="isDepositRefundable">
            <el-radio-group v-model="formData.isDepositRefundable">
              <el-radio :value="true">可退</el-radio>
              <el-radio :value="false">不可退</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="尾款截止" prop="balanceDueDaysBeforeStart">
            <el-input-number
              v-model="formData.balanceDueDaysBeforeStart"
              :min="0"
              :max="365"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-hint -mt-10px mb-10px">
        尾款截止：开班前 N 天需付清尾款；定金为 0 或等于学费时按全款报名，全款报名不支持拼团
      </div>

      <el-divider content-position="left">另行缴费</el-divider>
      <el-form-item label="缴费项目">
        <div class="w-full">
          <ExtraFeeEditor v-model="formData.extraFeeItems" />
          <div class="form-hint">
            另行缴费：需要到校报到时缴纳的项目；必须项学员必缴，可选项按需选择
          </div>
        </div>
      </el-form-item>

      <el-divider content-position="left">拼团方案</el-divider>
      <el-form-item label="开启拼团" prop="isGrouponEnabled">
        <el-switch v-model="formData.isGrouponEnabled" />
      </el-form-item>
      <el-row :gutter="20" v-if="formData.isGrouponEnabled">
        <el-col :span="12">
          <el-form-item label="拼团减免" prop="grouponDiscountAmount">
            <el-input-number
              v-model="formData.grouponDiscountAmount"
              :min="0"
              :precision="2"
              controls-position="right"
              placeholder="元"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成团人数" prop="grouponRequiredCount">
            <el-input-number
              v-model="formData.grouponRequiredCount"
              :min="2"
              :max="99"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有效时间" prop="grouponExpireHours">
            <el-input-number
              v-model="formData.grouponExpireHours"
              :min="1"
              :max="720"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-hint -mt-10px mb-10px" v-if="formData.isGrouponEnabled">
        有效时间单位：小时；未成团按原价，不享受拼团优惠
      </div>

      <el-divider content-position="left">退款政策</el-divider>
      <el-form-item label="退费说明" prop="refundPolicyText">
        <el-input
          v-model="formData.refundPolicyText"
          type="textarea"
          :rows="6"
          maxlength="2000"
          show-word-limit
          placeholder="如：开班前 7 天可全额退款；开班后按已上课时比例扣除…"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { FeeTemplateApi } from '@/api/maritime/feeTemplate'
import ExtraFeeEditor from '../components/ExtraFeeEditor.vue'

/** 费用模板 表单 */
defineOptions({ name: 'FeeTemplateForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  name: undefined,
  tuitionAmount: undefined,
  tuitionIncludesAccommodation: false,
  extraFeeItems: [],
  depositAmount: undefined,
  isGrouponEnabled: false,
  grouponDiscountAmount: 0,
  grouponRequiredCount: 3,
  grouponExpireHours: 24,
  grouponFailDiscountAmount: 0,
  isDepositRefundable: true,
  balanceDueDaysBeforeStart: 7,
  refundPolicyText: undefined,
  status: 0
})
const formRules = reactive({
  name: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  tuitionAmount: [{ required: true, message: '学费总额不能为空', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '定金金额不能为空', trigger: 'blur' }],
  isDepositRefundable: [{ required: true, message: '请选择定金是否可退', trigger: 'change' }],
  balanceDueDaysBeforeStart: [{ required: true, message: '尾款截止天数不能为空', trigger: 'blur' }],
  isGrouponEnabled: [{ required: true, message: '请选择是否开启拼团', trigger: 'change' }],
  grouponDiscountAmount: [{ required: true, message: '拼团减免金额不能为空', trigger: 'blur' }],
  grouponRequiredCount: [{ required: true, message: '成团人数不能为空', trigger: 'blur' }],
  grouponExpireHours: [{ required: true, message: '拼团有效时间不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      const data = await FeeTemplateApi.getFeeTemplate(id)
      formData.value = { ...formData.value, ...data }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于弹窗外部调用

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const payload = { ...formData.value }
    if (formType.value === 'create') {
      await FeeTemplateApi.createFeeTemplate(payload as any)
      message.success(t('common.createSuccess'))
    } else {
      await FeeTemplateApi.updateFeeTemplate(payload as any)
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
    name: undefined,
    tuitionAmount: undefined,
    tuitionIncludesAccommodation: false,
    extraFeeItems: [],
    depositAmount: undefined,
    isGrouponEnabled: false,
    grouponDiscountAmount: 0,
    grouponRequiredCount: 3,
    grouponExpireHours: 24,
    grouponFailDiscountAmount: 0,
    isDepositRefundable: true,
    balanceDueDaysBeforeStart: 7,
    refundPolicyText: undefined,
    status: 0
  }
  formRef.value?.resetFields()
}
</script>

<style scoped>
.form-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
</style>
