import { http } from '@/utils/request'

/** 专升本报名审核状态 */
export type UpgradeApplyStatus = '待审核' | '已通过' | '已驳回'

/** 专升本报名记录 */
export interface UpgradeApply {
  id: number
  studentName: string
  majorName: string
  targetSchool: string
  /** 成绩排名 */
  rank: number
  status: UpgradeApplyStatus
  createTime: string
}

/** 专升本报名审核列表（GET /upgrade-applies/report） */
export function getUpgradeAuditList(params: {
  studentName?: string
  status?: '' | UpgradeApplyStatus
}) {
  return http.get<UpgradeApply[]>('/upgrade-applies/report', params)
}

/** 专升本报名审核：通过（PUT /upgrade-applies/{id}/audit?status=PASS） */
export function approveUpgradeApply(id: number) {
  return http.put<{ id: number; status: UpgradeApplyStatus }>(`/upgrade-applies/${id}/audit`, undefined, {
    params: { status: 'PASS' },
  })
}

/** 专升本报名审核：驳回（PUT /upgrade-applies/{id}/audit?status=FAIL） */
export function rejectUpgradeApply(id: number) {
  return http.put<{ id: number; status: UpgradeApplyStatus }>(`/upgrade-applies/${id}/audit`, undefined, {
    params: { status: 'FAIL' },
  })
}