import { http } from '@/utils/request'

/** 自动排考入参 */
export interface AutoArrangeParams {
  /** 学期，如 2026-2027学年第二学期 */
  term: string
  /** 年级，如 2024级 */
  grade: string
}

/** 单条排考结果 */
export interface ExamArrangeItem {
  id: number
  courseName: string
  className: string
  studentCount: number
  /** 考试日期 YYYY-MM-DD */
  examDate: string
  /** 场次文案，如 第1场 08:30~10:10 */
  session: string
  examRoom: string
  invigilators: string[]
  /** 是否命中冲突（监考员重复 / 教室重复占用等） */
  conflict: boolean
  /** 冲突说明（conflict=true 时有值） */
  conflictReason: string
}

/** 自动排考汇总信息 */
export interface AutoArrangeSummary {
  examName: string
  examCount: number
  roomCount: number
  normalCount: number
  conflictCount: number
}

/** 自动排考结果 */
export interface AutoArrangeResult {
  term: string
  grade: string
  summary: AutoArrangeSummary
  items: ExamArrangeItem[]
  /** 排考完成时间 YYYY-MM-DD HH:mm:ss */
  generatedAt: string
}

/** 一键自动排考（Mock 返回含冲突的数据，前端以红 tag 高亮冲突行） */
export function autoArrangeExam(data: AutoArrangeParams) {
  return http.post<AutoArrangeResult>('/exam/auto-arrange', data)
}

/* ==================== 考核方式申报审核 ==================== */

/** 考核方式申报审核状态 */
export type MethodAuditStatus = '待审核' | '已通过' | '已驳回'

/** 考核方式申报记录（管理端审核） */
export interface ExamMethodApply {
  id: number
  courseName: string
  className: string
  /** 申报教师 */
  teacher: string
  /** 拟采用的考核方式，如 闭卷考试 / 上机考试 / 课程论文 */
  methodName: string
  /** 申报说明 */
  reason: string
  status: MethodAuditStatus
  createTime: string
}

/** 考核方式申报审核列表（GET /exam/method-audit/list） */
export function getMethodAuditList(params: {
  courseName?: string
  teacher?: string
  status?: '' | MethodAuditStatus
}) {
  return http.get<ExamMethodApply[]>('/exam/method-audit/list', params)
}

/** 考核方式申报：通过（PUT /exam/method-audit/approve/{id}） */
export function approveMethodAudit(id: number) {
  return http.put<{ id: number; status: MethodAuditStatus }>(`/exam/method-audit/approve/${id}`)
}

/** 考核方式申报：驳回（PUT /exam/method-audit/reject/{id}） */
export function rejectMethodAudit(id: number) {
  return http.put<{ id: number; status: MethodAuditStatus }>(`/exam/method-audit/reject/${id}`)
}