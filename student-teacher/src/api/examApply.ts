import { http, normalizeList } from '@/utils/request'

/** 考核方式申报（exam_apply.status：WAIT/PASS/FAIL；apply_type：闭卷/开卷/机考…） */
export interface ExamApply {
  id?: number
  courseId: number
  teacherId: number
  /** 考核方式，如 闭卷 / 开卷 / 机考 */
  applyType: string
  /** 申请理由 */
  reason?: string
  status?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  courseName?: string
  teacherName?: string
  createTime?: string
}

/**
 * 教师：提交考核方式申报
 * 后端 POST /api/exam-apply/apply，body 为 ExamApply
 */
export function submitExamApply(payload: ExamApply) {
  return http.post<null>('/exam-apply/apply', payload)
}

/**
 * 按 ID 查询申报详情
 * 后端 GET /api/exam-apply/{id}
 */
export function getExamApply(id: number) {
  return http.get<ExamApply>(`/exam-apply/${id}`)
}

/**
 * 教师：我的申报记录
 * ⚠️ 缺口：后端只有 /export-list（导出总表）与 /{id}，没有"按教师查我的申报"，
 * 当前由 Mock 提供 GET /api/exam-apply/my/list?teacherId，待后端补。
 */
export async function listMyExamApplies(teacherId: number): Promise<ExamApply[]> {
  const data = await http.get<unknown>('/exam-apply/my/list', { teacherId })
  return normalizeList<ExamApply>(data)
}
