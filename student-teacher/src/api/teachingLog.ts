import { http, normalizeList } from '@/utils/request'

/** 教学日志（teaching_log） */
export interface TeachingLog {
  id?: number
  teacherId: number
  courseId: number
  classId: number
  /** YYYY-MM-DD，不允许晚于当天 */
  teachingDate: string
  /** 授课内容 */
  content: string
  /** 作业布置 */
  homework?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  courseName?: string
  className?: string
}

/**
 * 教师：新增授课日志
 * 后端 POST /api/teaching-log，body 为 TeachingLog
 */
export function addTeachingLog(log: TeachingLog) {
  return http.post<null>('/teaching-log', log)
}

/**
 * 教师：查询本人某周的授课日志（date 传该周任意一天）
 * 后端 GET /api/teaching-log/list-by-teacher-week?teacherId&date
 */
export async function listMyTeachingLogs(
  teacherId: number,
  date: string,
): Promise<TeachingLog[]> {
  const data = await http.get<unknown>('/teaching-log/list-by-teacher-week', { teacherId, date })
  return normalizeList<TeachingLog>(data)
}

/**
 * 查询某个班级某周的授课日志
 * 后端 GET /api/teaching-log/list-by-class-week?classId&date
 */
export async function listClassTeachingLogs(
  classId: number,
  date: string,
): Promise<TeachingLog[]> {
  const data = await http.get<unknown>('/teaching-log/list-by-class-week', { classId, date })
  return normalizeList<TeachingLog>(data)
}
