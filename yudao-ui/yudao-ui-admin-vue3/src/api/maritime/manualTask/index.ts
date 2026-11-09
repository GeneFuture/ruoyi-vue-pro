import request from '@/config/axios'

export interface ManualTaskVO {
  id: number
  taskNo: string
  enrollmentId: number
  memberId: number
  enrollmentNo?: string
  realName?: string
  phone?: string
  courseName?: string
  sessionCode?: string
  sessionName?: string
  refundAmount?: number
  applyStatus?: string
  orderId?: number
  sourceType: string
  sourceId?: number
  taskType: string
  status: string
  priority: number
  reason: string
  handlerId?: number
  handleResult?: string
  handleTime?: Date
  createTime: Date
}

export interface ManualTaskPageReqVO extends PageParam {
  enrollmentId?: number
  memberId?: number
  realName?: string
  phone?: string
  taskType?: string
  status?: string
  createTime?: Date[]
}

export interface ManualTaskRefundDetailVO {
  taskId: number
  taskNo: string
  taskType: string
  taskStatus: string
  taskReason?: string
  enrollmentId: number
  memberId: number
  orderId?: number
  orderNo?: string
  orderAmount?: number
  orderStatus?: string
  refundApplyId: number
  applyStatus?: string
  applyReason?: string
  refundAmount?: number
  adminRemark?: string
  approveTime?: Date
  approverId?: number
  refundTime?: Date
  createTime?: Date
}

// 获得人工处理单分页
export const getManualTaskPage = (params: ManualTaskPageReqVO) => {
  return request.get({ url: '/maritime/manual-task/page', params })
}

// 获得人工处理单
export const getManualTask = (id: number) => {
  return request.get({ url: '/maritime/manual-task/get?id=' + id })
}

// 处理人工单
export const resolveManualTask = (data: {
  id: number
  handleResult: string
  targetEnrollmentStatus?: string
  remark?: string
}) => {
  return request.put({ url: '/maritime/manual-task/resolve', data })
}

// 获得退费类人工单的审核详情
export const getManualTaskRefundDetail = (taskId: number) => {
  return request.get({ url: '/maritime/manual-task/refund-detail', params: { taskId } })
}

// 审核退费类人工单
export const resolveManualTaskRefund = (data: {
  id: number
  result: 'APPROVE' | 'REJECT'
  refundAmount?: number
  adminRemark?: string
  rejectReason?: string
}) => {
  return request.put({ url: '/maritime/manual-task/resolve-refund', data })
}
