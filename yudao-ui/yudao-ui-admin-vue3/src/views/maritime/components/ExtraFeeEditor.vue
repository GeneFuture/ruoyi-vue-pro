<template>
  <div class="w-full">
    <div v-for="(item, index) in items" :key="index" class="extra-fee-row">
      <el-input v-model="item.name" placeholder="项目，如：教材费" @input="emitChange" />
      <el-input-number
        v-model="item.amount"
        :min="0"
        :precision="2"
        controls-position="right"
        placeholder="金额（元）"
        @change="emitChange"
      />
      <el-switch
        v-model="item.required"
        inline-prompt
        width="56"
        active-text="必须"
        inactive-text="可选"
        @change="emitChange"
      />
      <el-button link type="danger" @click="removeItem(index)">
        <Icon icon="ep:delete" />
      </el-button>
    </div>
    <el-button link type="primary" @click="addItem">
      <Icon icon="ep:plus" class="mr-5px" />添加项目
    </el-button>
  </div>
</template>

<script setup lang="ts">
/** 另行缴费编辑器：数组形式维护 [{name, amount, required}]，v-model 为对象数组，金额单位元 */
defineOptions({ name: 'ExtraFeeEditor' })

/** 可编辑行：金额未填为 undefined，required 为 true 表示学员必缴 */
interface ExtraFeeRow {
  name: string
  amount: number | undefined
  required: boolean
}

/** 后端可能返回数组或 JSON 字符串，元素金额为 BigDecimal（数字或字符串） */
type ExtraFeeValue =
  | { name?: string; amount?: number | string; required?: boolean }[]
  | string
  | null

const props = defineProps<{
  modelValue?: ExtraFeeValue
}>()
const emit = defineEmits(['update:modelValue'])

const items = ref<ExtraFeeRow[]>([])
// 最近一次向外 emit 的值：用于区分"外部赋值"与"自身编辑回流"，避免编辑时重解析丢焦点
const lastEmitted = ref<unknown>(undefined)

/** 金额解析：空值 / 非法值视为未填 */
const toAmount = (value?: number | string): number | undefined => {
  if (value == null || value === '') {
    return undefined
  }
  const amount = Number(value)
  return Number.isNaN(amount) ? undefined : amount
}

/** 将后端返回的另行缴费项目（数组或 JSON 字符串）解析为可编辑行 */
const parse = (value?: ExtraFeeValue): ExtraFeeRow[] => {
  if (!value) {
    return []
  }
  let list: unknown
  if (typeof value === 'string') {
    try {
      list = JSON.parse(value)
    } catch {
      return []
    }
  } else {
    list = value
  }
  if (!Array.isArray(list)) {
    return []
  }
  return list.map((item) => ({
    name: item?.name == null ? '' : String(item.name),
    amount: toAmount(item?.amount),
    required: item?.required === true
  }))
}

/** 将可编辑行收集为提交值（项目名为空的行忽略；空数组表示没有另行缴费项目） */
const build = (): ExtraFeeRow[] =>
  items.value
    .filter((item) => item.name.trim())
    .map((item) => ({
      name: item.name.trim(),
      amount: item.amount ?? undefined,
      required: item.required === true
    }))

watch(
  () => props.modelValue,
  (value) => {
    if (JSON.stringify(value ?? undefined) === JSON.stringify(lastEmitted.value)) {
      return
    }
    items.value = parse(value)
  },
  { immediate: true, deep: true }
)

const emitChange = () => {
  const built = build()
  lastEmitted.value = built
  emit('update:modelValue', built)
}

const addItem = () => {
  items.value = [...items.value, { name: '', amount: undefined, required: false }]
}

const removeItem = (index: number) => {
  items.value = items.value.filter((_, i) => i !== index)
  emitChange()
}
</script>

<style scoped>
.extra-fee-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.extra-fee-row :deep(> .el-input) {
  flex: 1;
}

.extra-fee-row :deep(.el-input-number) {
  width: 160px;
}
</style>
