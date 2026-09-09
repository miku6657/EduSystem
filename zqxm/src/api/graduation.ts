import { http } from '@/utils/request'

/** 毕业生审核状态（毕业审核：待审核 / 通过 / 未通过） */
export type GraduationAuditStatus = '待审核' | '通过' | '未通过'

/** 毕业生（毕业审核管理端） */
export interface GraduationStudent {
  id: number
  /** 学号 */
  studentNo: string
  /** 姓名 */
  name: string
  /** 专业 */
  major: string
  /** 毕业年份 */
  graduateYear: string
  status: GraduationAuditStatus
  createTime: string
}

/** 毕业生列表查询参数（type 别名以获得索引签名兼容 http.get 参数） */
export type GraduationQuery = {
  name?: string
  studentNo?: string
  status?: '' | GraduationAuditStatus
}

/** 毕业生列表（GET /graduation/list） */
export function getGraduationList(params: GraduationQuery) {
  return http.get<GraduationStudent[]>('/graduation/list', params)
}

/** 毕业审核：通过（PUT /graduation/approve/{id}） */
export function approveGraduation(id: number) {
  return http.put<{ id: number; status: GraduationAuditStatus }>(`/graduation/approve/${id}`)
}

/** 毕业审核：驳回（PUT /graduation/reject/{id}） */
export function rejectGraduation(id: number) {
  return http.put<{ id: number; status: GraduationAuditStatus }>(`/graduation/reject/${id}`)
}
