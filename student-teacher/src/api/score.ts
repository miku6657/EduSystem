import { getPage, http, normalizeList, pageParams } from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/api'

/** 成绩记录（exam_score） */
export interface ExamScore {
  id?: number
  examId: number
  studentId: number
  /** 分数；缺考时为 null */
  score?: number | null
  /** NORMAL / ABSENT / DELAY / CHEAT */
  status?: string
  /** 以下为展示用扩展字段：后端实体没有，Mock 会带上，缺省时页面自行兜底 */
  studentName?: string
  studentNo?: string
  examName?: string
  courseName?: string
  examDate?: string
  credit?: number
}

/** 考试统计（statByExam 返回 Map） */
export interface ExamScoreStat {
  total?: number
  actual?: number
  absent?: number
  passed?: number
  failed?: number
  [key: string]: unknown
}

/**
 * 学生：查询本人全部成绩
 * 后端 GET /api/score/list-by-student/{studentId}
 */
export async function listMyScores(studentId: number): Promise<ExamScore[]> {
  const data = await http.get<unknown>(`/score/list-by-student/${studentId}`)
  return normalizeList<ExamScore>(data)
}

/**
 * 教师：分页查询某场考试的成绩
 * 后端 GET /api/score/page-by-exam?pageNo&pageSize&examId&status
 */
export function pageScoresByExam(
  query: PageQuery & { examId: number; status?: string },
): Promise<PageResult<ExamScore>> {
  return getPage<ExamScore>('/score/page-by-exam', pageParams(query))
}

/**
 * 教师：批量录入 / 更新某场考试成绩
 * 后端 POST /api/score/save/{examId}，body 为成绩数组
 */
export function saveScores(examId: number, scores: ExamScore[]) {
  return http.post<null>(`/score/save/${examId}`, scores)
}

/**
 * 教师：考试数据统计（应考/实考/缺考/及格/不及格）
 * 后端 GET /api/score/stat/{examId}
 */
export function getScoreStat(examId: number) {
  return http.get<ExamScoreStat>(`/score/stat/${examId}`)
}
