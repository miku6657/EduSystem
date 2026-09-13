import { http, normalizeList } from '@/utils/request'

/** 补考 / 重修记录（exam_retake.type：补考 / 重修） */
export interface ExamRetake {
  id?: number
  studentId: number
  courseId: number
  /** 已安排的补考场次；未安排为 null */
  examId?: number | null
  type: string
  /** 展示用扩展字段（后端 Service 已关联填充） */
  courseName?: string
  studentName?: string
  studentNo?: string
  /** 已安排场次的考试名称（未安排时为 null） */
  examName?: string
  createTime?: string
}

/**
 * 学生：申请补考 / 重修
 * 后端 POST /api/retake/apply?studentId&courseId&type（用 query 参数，不是 body）
 *
 * @param type 类型：补考 / 重修（不传时后端按"重修"处理）
 */
export function applyRetake(studentId: number, courseId: number, type?: string) {
  return http.post<null>('/retake/apply', undefined, {
    studentId,
    courseId,
    ...(type ? { type } : {}),
  })
}

/**
 * 学生：查询本人补考重修记录
 * 后端 GET /api/retake/list-by-student/{studentId}
 */
export async function listMyRetakes(studentId: number): Promise<ExamRetake[]> {
  const data = await http.get<unknown>(`/retake/list-by-student/${studentId}`)
  return normalizeList<ExamRetake>(data)
}

/**
 * 按类型查询（补考 / 重修）
 * 后端 GET /api/retake/list-by-type/{type}
 */
export async function listRetakesByType(type: string): Promise<ExamRetake[]> {
  const data = await http.get<unknown>(`/retake/list-by-type/${encodeURIComponent(type)}`)
  return normalizeList<ExamRetake>(data)
}
