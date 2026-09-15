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
  graduateYear: string
  name?: string
  studentNo?: string
  status?: '' | GraduationAuditStatus
}

/** 毕业生列表（GET /graduate-students/year/{graduateYear}） */
export function getGraduationList(params: GraduationQuery) {
  const { graduateYear, ...query } = params
  return http.get<GraduationStudent[]>(`/graduate-students/year/${encodeURIComponent(graduateYear)}`, query)
}

/** 毕业审核：通过（POST /graduate-checks/audit） */
export function approveGraduation(studentId: number) {
  return http.post<{ studentId: number; checkStatus: 'PASS' }>('/graduate-checks/audit', {
    studentId,
    checkStatus: 'PASS',
  })
}

/** 毕业审核：驳回（POST /graduate-checks/audit） */
export function rejectGraduation(studentId: number) {
  return http.post<{ studentId: number; checkStatus: 'FAIL' }>('/graduate-checks/audit', {
    studentId,
    checkStatus: 'FAIL',
  })
}
