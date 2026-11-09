<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="820px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-divider content-position="left">学校信息</el-divider>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="学校名称" prop="name">
            <el-input v-model="formData.name" placeholder="如：青岛船员培训基地" />
          </el-form-item>
        </el-col>
        <!-- 坐标拾取放在地址上方：省 / 市 / 区由它自动带出，先选点再核对地址更顺 -->
        <el-col :span="24">
          <el-form-item label="经纬度坐标" prop="longitude">
            <div class="flex items-center gap-2 w-full">
              <el-input v-model="formData.longitude" placeholder="经度" clearable class="flex-1">
                <template #prepend>经度</template>
              </el-input>
              <el-input v-model="formData.latitude" placeholder="纬度" clearable class="flex-1">
                <template #prepend>纬度</template>
              </el-input>
              <el-button type="primary" @click="openMapDialog">
                <Icon icon="ep:location" class="mr-5px" />坐标拾取
              </el-button>
            </div>
            <div class="form-hint">
              必填：省 / 市 / 区由拾取结果自动填充、不可手工修改，详细地址可再补充；
              坐标同时决定小程序端的「导航」入口
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="省" prop="province">
            <el-input v-model="formData.province" disabled placeholder="地图拾取自动填充" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="市" prop="city">
            <el-input v-model="formData.city" disabled placeholder="地图拾取自动填充" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="区" prop="district">
            <el-input v-model="formData.district" disabled placeholder="地图拾取自动填充" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="详细地址" prop="address">
            <el-input v-model="formData.address" placeholder="如：某某路 1 号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">开启</el-radio>
              <el-radio :value="1">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序权重" prop="sortOrder">
            <el-input-number
              v-model="formData.sortOrder"
              :min="0"
              controls-position="right"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="form-hint -mt-10px mb-10px"
      >排序权重越大越靠前；关闭后该学校不再出现在新增班期的下拉中</div
      >

      <el-divider content-position="left">学校概况</el-divider>
      <el-form-item prop="campusIntro">
        <Editor v-model="formData.campusIntro" height="200px" />
      </el-form-item>

      <el-divider content-position="left">教学条件</el-divider>
      <el-form-item prop="classroomIntro">
        <Editor v-model="formData.classroomIntro" height="200px" />
      </el-form-item>

      <el-divider content-position="left">住宿条件</el-divider>
      <el-form-item prop="accommodationIntro">
        <Editor v-model="formData.accommodationIntro" height="200px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 地图选点弹窗 -->
  <MapDialog ref="mapDialogRef" @confirm="handleMapConfirm" />
</template>
<script setup lang="ts">
import { SchoolApi, type School } from '@/api/maritime/school'
import { MapDialog } from '@/components/Map'

/** 学校库 表单 */
defineOptions({ name: 'SchoolForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const mapDialogRef = ref() // 地图选点弹窗 Ref
const formData = ref<Partial<School>>({
  id: undefined,
  name: undefined,
  province: undefined,
  city: undefined,
  district: undefined,
  address: undefined,
  latitude: undefined,
  longitude: undefined,
  // 咨询电话的输入已下线（后续统一设计小程序咨询方式），字段保留原样往返，
  // 否则编辑保存会把各学校已存的电话清掉
  contactPhone: undefined,
  campusIntro: undefined,
  classroomIntro: undefined,
  accommodationIntro: undefined,
  status: 0,
  sortOrder: 0
})

/** 经纬度是否都已填写（输入框清空后是空串，不能只判 undefined） */
const hasCoordinate = () => {
  const filled = (value?: number | string) => value !== undefined && value !== null && value !== ''
  return filled(formData.value.longitude) && filled(formData.value.latitude)
}
const formRules = reactive({
  name: [{ required: true, message: '学校名称不能为空', trigger: 'blur' }],
  // 经纬度是一对，挂在经度上一起校验：没坐标就没有省市区，小程序端也就没有城市标签与「导航」
  longitude: [
    {
      validator: (_rule: any, _value: any, callback: any) => {
        callback(hasCoordinate() ? undefined : new Error('请拾取经纬度坐标'))
      },
      trigger: 'change'
    }
  ],
  province: [{ max: 50, message: '省份长度不能超过 50 个字符', trigger: 'blur' }],
  city: [{ max: 50, message: '城市长度不能超过 50 个字符', trigger: 'blur' }],
  district: [{ max: 50, message: '区县长度不能超过 50 个字符', trigger: 'blur' }],
  address: [{ max: 255, message: '详细地址长度不能超过 255 个字符', trigger: 'blur' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }],
  sortOrder: [{ required: true, message: '排序权重不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开地图选点，带上已填坐标以便定位到当前位置 */
const openMapDialog = () => {
  mapDialogRef.value?.open(
    formData.value.longitude ? Number(formData.value.longitude) : undefined,
    formData.value.latitude ? Number(formData.value.latitude) : undefined
  )
}

/** 地图选点确认：回填坐标 + 省市区 + 详细地址（详细地址为完整地址去掉省市区前缀后的部分） */
const handleMapConfirm = (data: {
  longitude: string
  latitude: string
  province: string
  city: string
  district: string
  address: string
}) => {
  formData.value.longitude = data.longitude
  formData.value.latitude = data.latitude
  formData.value.province = data.province
  formData.value.city = data.city
  formData.value.district = data.district
  formData.value.address = data.address
  // 值是代码赋的，不会触发 el-input 的 change，这里主动复校一次把「请拾取经纬度坐标」消掉
  formRef.value?.validateField('longitude').catch(() => {})
}

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
      const data = await SchoolApi.getSchool(id)
      formData.value = { ...formData.value, ...data }
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于弹窗外部调用

/** 归一化坐标：输入框清空后是空串，直接提交与 BigDecimal 校验都不友好，统一转成 undefined / 数字 */
const normalizeCoordinate = (value?: number | string) => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const num = Number(value)
  return Number.isFinite(num) ? num : undefined
}

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const payload = {
      ...formData.value,
      latitude: normalizeCoordinate(formData.value.latitude),
      longitude: normalizeCoordinate(formData.value.longitude)
    }
    if (formType.value === 'create') {
      await SchoolApi.createSchool(payload as any)
      message.success(t('common.createSuccess'))
    } else {
      await SchoolApi.updateSchool(payload as any)
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
    province: undefined,
    city: undefined,
    district: undefined,
    address: undefined,
    latitude: undefined,
    longitude: undefined,
    contactPhone: undefined,
    campusIntro: undefined,
    classroomIntro: undefined,
    accommodationIntro: undefined,
    status: 0,
    sortOrder: 0
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
