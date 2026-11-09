<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="100px"
    v-loading="loading"
    class="session-fee-panel"
  >
    <el-divider content-position="left">费用方案</el-divider>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="费用模板">
          <el-select
            v-model="feeTemplateId"
            clearable
            placeholder="选填：选择后填充下方费用，可再调整；套用会覆盖当前已填费用"
            class="!w-full"
            @change="onTemplateChange"
          >
            <el-option
              v-for="template in feeTemplates"
              :key="template.id"
              :label="template.name"
              :value="template.id"
            />
          </el-select>
        </el-form-item>
      </el-col>
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
        <div class="form-hint"
        >另行缴费：需要到校报到时缴纳的项目；必须项学员必缴，可选项按需选择</div
        >
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
        :rows="4"
        maxlength="2000"
        show-word-limit
        placeholder="如：开班前 7 天可全额退款；开班后按已上课时比例扣除…"
      />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { SessionFeeConfigApi, SessionFeeConfig } from '@/api/maritime/sessionFeeConfig'
import { FeeTemplateApi, FeeTemplate } from '@/api/maritime/feeTemplate'
import ExtraFeeEditor from '../components/ExtraFeeEditor.vue'

/** 班期费用配置面板：内嵌于班期编辑表单，按班期加载/保存费用配置 */
defineOptions({ name: 'SessionFeePanel' })

const loading = ref(false) // 加载费用配置中
const formRef = ref() // 表单 Ref

/** 费用配置默认值：班期无费用配置时供补录 */
const DEFAULT_FEE_CONFIG: SessionFeeConfig = {
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
  refundPolicyText: undefined
}

const formData = ref<SessionFeeConfig>({ ...DEFAULT_FEE_CONFIG })

/** 费用模板：快捷套用控件，选中后只填充下方费用字段，自身不参与费用配置的提交 */
const feeTemplateId = ref<number>()
const feeTemplates = ref<FeeTemplate[]>([])
const feeTemplatesLoaded = ref(false)

/** 打开面板时拉一次即可：新增走 reset、编辑走 load，两条路径都会调，用标记避免重复请求 */
const loadFeeTemplates = async () => {
  if (feeTemplatesLoaded.value) {
    return
  }
  feeTemplatesLoaded.value = true
  try {
    const data = await FeeTemplateApi.getFeeTemplatePage({ pageNo: 1, pageSize: 50, status: 0 })
    feeTemplates.value = data.list || []
  } catch {
    // 拉取失败不置位，下次打开面板再试
    feeTemplatesLoaded.value = false
  }
}

/** 选中费用模板：把模板值套用到下方费用字段（未选/清空时保留当前已填费用） */
const onTemplateChange = (templateId?: number) => {
  const template = feeTemplates.value.find((item) => item.id === templateId)
  if (template) {
    applyTemplate(template)
  }
}

const formRules = reactive({
  tuitionAmount: [{ required: true, message: '学费总额不能为空', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '定金金额不能为空', trigger: 'blur' }],
  isDepositRefundable: [{ required: true, message: '请选择定金是否可退', trigger: 'change' }],
  balanceDueDaysBeforeStart: [{ required: true, message: '尾款截止天数不能为空', trigger: 'blur' }],
  isGrouponEnabled: [{ required: true, message: '请选择是否开启拼团', trigger: 'change' }],
  grouponDiscountAmount: [{ required: true, message: '拼团减免金额不能为空', trigger: 'blur' }],
  grouponRequiredCount: [{ required: true, message: '成团人数不能为空', trigger: 'blur' }],
  grouponExpireHours: [{ required: true, message: '拼团有效时间不能为空', trigger: 'blur' }]
})

/** 按班期加载费用配置；不存在时用默认值供补录 */
const load = async (sessionId: number) => {
  loading.value = true
  try {
    const data = await SessionFeeConfigApi.getSessionFeeConfigBySession(sessionId)
    formData.value = data ? { ...DEFAULT_FEE_CONFIG, ...data } : { ...DEFAULT_FEE_CONFIG }
    await loadFeeTemplates()
  } finally {
    loading.value = false
  }
}

/** 校验费用表单 */
const validate = async () => {
  await formRef.value.validate()
}

/** 保存费用配置：按班期存在则更新，不存在则创建 */
const save = async (sessionId: number) => {
  await formRef.value.validate()
  await SessionFeeConfigApi.saveSessionFeeConfig({ ...formData.value, sessionId })
}

/** 重置为默认值（弹窗重新打开时清空上一个班期的数据） */
const reset = async () => {
  formData.value = { ...DEFAULT_FEE_CONFIG }
  feeTemplateId.value = undefined
  formRef.value?.clearValidate()
  await loadFeeTemplates()
}

/** 套用费用模板：按模板值填充全部费用字段（数值缺省兜底，避免空值触发必填校验） */
const applyTemplate = (template: FeeTemplate) => {
  formData.value = {
    ...formData.value,
    tuitionAmount: template.tuitionAmount,
    tuitionIncludesAccommodation: template.tuitionIncludesAccommodation ?? false,
    extraFeeItems: template.extraFeeItems ?? [],
    depositAmount: template.depositAmount,
    isGrouponEnabled: template.isGrouponEnabled ?? false,
    grouponDiscountAmount: template.grouponDiscountAmount ?? 0,
    grouponRequiredCount: template.grouponRequiredCount ?? 3,
    grouponExpireHours: template.grouponExpireHours ?? 24,
    isDepositRefundable: template.isDepositRefundable ?? true,
    balanceDueDaysBeforeStart: template.balanceDueDaysBeforeStart ?? 7,
    refundPolicyText: template.refundPolicyText
  }
  formRef.value?.clearValidate()
}

defineExpose({ load, validate, save, reset })
</script>

<style scoped>
.session-fee-panel {
  margin-top: 10px;
}

.form-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
</style>
