import { http, normalizeList } from '@/utils/request'

/** 专升本报名（upgrade_apply.apply_status：WAIT/PASS/FAIL） */
export interface UpgradeApply {
  id?: number
  studentId: number
  /** 报考院校 */
  schoolName: string
  /** 报考专业 */
  majorName: string
  applyStatus?: string
  remark?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  studentName?: string
  studentNo?: string
  createTime?: string
}

/**
 * 学生：提交专升本报名
 * 后端 POST /api/upgrade-apply/apply，body 为 UpgradeApply
 */
export function submitUpgradeApply(payload: UpgradeApply) {
  return http.post<null>('/upgrade-apply/apply', payload)
}

/**
 * 按 ID 查询报名详情
 * 后端 GET /api/upgrade-apply/{id}
 */
export function getUpgradeApply(id: number) {
  return http.get<UpgradeApply>(`/upgrade-apply/${id}`)
}

/**
 * 学生：我的报名记录
 * ⚠️ 缺口：后端只有 /report-list（报名报表，管理端用）与 /{id}，没有"按学生查我的报名"，
 * 当前由 Mock 提供 GET /api/upgrade-apply/my/list?studentId，待后端补。
 */
export async function listMyUpgradeApplies(studentId: number): Promise<UpgradeApply[]> {
  const data = await http.get<unknown>('/upgrade-apply/my/list', { studentId })
  return normalizeList<UpgradeApply>(data)
}
