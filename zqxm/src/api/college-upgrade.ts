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

/** 专升本报名审核列表（GET /college-upgrade/audit/list） */
export function getUpgradeAuditList(params: {
  studentName?: string
  status?: '' | UpgradeApplyStatus
}) {
  return http.get<UpgradeApply[]>('/college-upgrade/audit/list', params)
}

/** 专升本报名审核：通过（PUT /college-upgrade/approve/{id}） */
export function approveUpgradeApply(id: number) {
  return http.put<{ id: number; status: UpgradeApplyStatus }>(`/college-upgrade/approve/${id}`)
}

/** 专升本报名审核：驳回（PUT /college-upgrade/reject/{id}） */
export function rejectUpgradeApply(id: number) {
  return http.put<{ id: number; status: UpgradeApplyStatus }>(`/college-upgrade/reject/${id}`)
}