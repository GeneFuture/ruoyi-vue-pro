<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    label-width="100px"
    v-loading="loading"
    class="session-venue-panel"
  >
    <el-divider content-position="left">学校信息</el-divider>
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="学校名称" prop="campusName">
          <el-select
            v-model="formData.campusName"
            placeholder="从学校库选择，自动带出省市区与坐标"
            clearable
            filterable
            class="!w-full"
            @change="onSchoolChange"
          >
            <el-option
              v-for="(item, index) in schoolOptions"
              :key="item.id ?? `${item.name}-${index}`"
              :label="item.name || ''"
              :value="item.name || ''"
            />
          </el-select>
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
            必填：省 / 市 / 区由选择的学校或拾取结果自动填充、不可手工修改，详细地址可再补充；
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
          <el-input v-model="formData.address" placeholder="如：某某路 1 号" clearable />
        </el-form-item>
      </el-col>
    </el-row>

    <el-divider content-position="left">学校概况</el-divider>
    <el-form-item prop="campusIntro">
      <Editor v-model="formData.campusIntro" height="180px" />
    </el-form-item>

    <el-divider content-position="left">教学条件</el-divider>
    <el-form-item prop="classroomIntro">
      <Editor v-model="formData.classroomIntro" height="180px" />
    </el-form-item>

    <el-divider content-position="left">住宿条件</el-divider>
    <el-form-item prop="accommodationIntro">
      <Editor v-model="formData.accommodationIntro" height="180px" />
    </el-form-item>
  </el-form>

  <!-- 地图选点弹窗 -->
  <MapDialog ref="mapDialogRef" @confirm="handleMapConfirm" />
</template>

<script setup lang="ts">
import { SessionVenueApi, SessionVenue } from '@/api/maritime/sessionVenue'
import { SchoolApi, School } from '@/api/maritime/school'
import { MapDialog } from '@/components/Map'

/**
 * 班期场地面板：内嵌于班期编辑表单，按班期加载/保存学校名称、地址、坐标与概况/教学/住宿信息
 *
 * 咨询电话的输入已下线（后续统一设计小程序咨询方式），字段仍随表单原样往返，
 * 否则保存会把各学校已存的电话清掉
 */
defineOptions({ name: 'SessionVenuePanel' })

const message = useMessage() // 消息弹窗

const loading = ref(false) // 加载场地信息中
const formRef = ref() // 表单 Ref
const mapDialogRef = ref() // 地图选点弹窗 Ref

/** 场地信息默认值：班期无场地信息时供补录 */
const DEFAULT_VENUE: SessionVenue = {
  campusName: undefined,
  province: undefined,
  city: undefined,
  district: undefined,
  address: undefined,
  latitude: undefined,
  longitude: undefined,
  contactPhone: undefined,
  campusIntro: undefined,
  accommodationIntro: undefined,
  classroomIntro: undefined
}

const formData = ref<SessionVenue>({ ...DEFAULT_VENUE })

/** 经纬度是否都已填写（输入框清空后是空串，不能只判 undefined） */
const hasCoordinate = () => {
  const filled = (value?: number | string) => value !== undefined && value !== null && value !== ''
  return filled(formData.value.longitude) && filled(formData.value.latitude)
}

/** 除学校名称外，学校信息是否已有内容（地址、坐标、三段概况） */
const hasSchoolInfoContent = () => {
  const venue = formData.value as Record<string, unknown>
  return [
    'province',
    'city',
    'district',
    'address',
    'latitude',
    'longitude',
    'campusIntro',
    'classroomIntro',
    'accommodationIntro'
  ].some((key) => String(venue[key] ?? '').trim() !== '')
}

/**
 * 场地是否整块留空：没选学校、也没填任何学校信息。
 * 班期的「学校」是选填的（不选则班期无学校信息），所以场地允许整体不填；
 * 但只要填了内容，就必须有坐标。
 */
const isVenueBlank = () =>
  String(formData.value.campusName ?? '').trim() === '' && !hasSchoolInfoContent()

/**
 * 校验规则：整体留空时全部选填；填了内容则经纬度必填。
 * 经纬度是一对，挂在经度上一起校验——没坐标就没有省市区，小程序端也就没有城市标签与「导航」。
 */
const formRules = reactive({
  longitude: [
    {
      validator: (_rule: any, _value: any, callback: any) => {
        if (isVenueBlank() || hasCoordinate()) {
          callback()
          return
        }
        callback(new Error('请拾取经纬度坐标'))
      },
      trigger: 'change'
    }
  ]
})

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

/**
 * 归一化坐标：输入框清空后是空串，直接提交与 BigDecimal 校验都不友好，统一转成 undefined / 数字
 */
const normalizeCoordinate = (value?: number | string) => {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  const num = Number(value)
  return Number.isFinite(num) ? num : undefined
}

/** 学校库（仅启用状态），学校名称下拉的选项来源 */
const schools = ref<School[]>([])
const schoolsLoaded = ref(false)

/** 打开面板时拉一次即可：新增走 reset、编辑走 load，两条路径都会调，用标记避免重复请求 */
const loadSchools = async () => {
  if (schoolsLoaded.value) {
    return
  }
  schoolsLoaded.value = true
  try {
    schools.value = (await SchoolApi.getEnabledSchoolList()) || []
  } catch {
    // 拉取失败不置位，下次打开面板再试
    schoolsLoaded.value = false
  }
}

/**
 * 下拉选项：学校库的启用学校。
 * 快照里的学校名可能已被改名或关闭，补一条同名选项，避免这类班期回显时名称显示不出来。
 */
const schoolOptions = computed(() => {
  const current = String(formData.value.campusName ?? '').trim()
  const stale = current && !schools.value.some((item) => item.name === current)
  return stale ? [{ name: current } as School, ...schools.value] : schools.value
})

/** 上一次选中的学校名：取消覆盖时把下拉的选择退回原值 */
const lastSchoolName = ref<string>()

/** 选学校：把学校库的地址、坐标与概况整套回填到表单（仍是本班期的快照，学校库后续改动不影响已保存的班期） */
const onSchoolChange = async (name?: string) => {
  const school = schools.value.find((item) => item.name === name)
  if (!school) {
    // 清空选择：只清学校名称，地址等内容保持用户自己维护的样子
    lastSchoolName.value = undefined
    return
  }
  // 已经填过内容时先确认，避免误点一下就把本班期改过的概况 / 教学 / 住宿整段覆盖掉。
  // 注意判的是「名称以外」的内容：此时 v-model 已经把名称改成新学校了
  if (hasSchoolInfoContent()) {
    try {
      await message.confirm(
        `确认用「${school.name}」的学校信息覆盖当前已填写的内容？未保存的修改会丢失。`
      )
    } catch {
      formData.value.campusName = lastSchoolName.value
      return
    }
  }
  applySchool(school)
  lastSchoolName.value = school.name
}

/** 按班期加载场地信息；不存在时用默认值供补录 */
const load = async (sessionId: number) => {
  loading.value = true
  try {
    const data = await SessionVenueApi.getSessionVenueBySession(sessionId)
    formData.value = data ? { ...DEFAULT_VENUE, ...data } : { ...DEFAULT_VENUE }
    lastSchoolName.value = formData.value.campusName
    await loadSchools()
  } finally {
    loading.value = false
  }
}

/** 校验场地表单：经纬度必填，其余字段选填 */
const validate = async () => {
  await formRef.value?.validate()
}

/** 保存场地信息：按班期存在则更新，不存在则创建 */
const save = async (sessionId: number) => {
  await SessionVenueApi.saveSessionVenue({
    ...formData.value,
    latitude: normalizeCoordinate(formData.value.latitude),
    longitude: normalizeCoordinate(formData.value.longitude),
    sessionId
  })
}

/**
 * 套用学校：按学校库的值填充学校信息（坐标缺省兜底成 undefined，避免空串触发校验）
 *
 * 注意：这里只是把值填进表单，最终仍以本班期保存的快照为准（学校库后续修改不影响已创建的班期）
 */
const applySchool = (school: School) => {
  formData.value = {
    ...formData.value,
    campusName: school.name,
    province: school.province,
    city: school.city,
    district: school.district,
    address: school.address,
    latitude: school.latitude,
    longitude: school.longitude,
    contactPhone: school.contactPhone,
    campusIntro: school.campusIntro,
    classroomIntro: school.classroomIntro,
    accommodationIntro: school.accommodationIntro
  }
  formRef.value?.clearValidate()
}

/** 重置为默认值（弹窗重新打开时清空上一个班期的数据） */
const reset = async () => {
  formData.value = { ...DEFAULT_VENUE }
  lastSchoolName.value = undefined
  formRef.value?.clearValidate()
  await loadSchools()
}

defineExpose({ load, validate, save, reset })
</script>

<style scoped>
.form-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
}
</style>
