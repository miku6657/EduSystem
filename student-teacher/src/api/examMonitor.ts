import { http, normalizeList } from '@/utils/request'

/** 监考安排（exam_monitor.monitor_role：MAIN 主监考 / SUB 副监考） */
export interface ExamMonitor {
  id?: number
  examId: number
  teacherId: number
  monitorRole?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  examName?: string
  courseName?: string
  examDate?: string
  startTime?: string
  endTime?: string
  roomName?: string
  className?: string
}

/**
 * 教师：我的监考任务
 * 后端 GET /api/exam-monitor/list-by-teacher/{teacherId}
 */
export async function listMyInvigilations(teacherId: number): Promise<ExamMonitor[]> {
  const data = await http.get<unknown>(`/exam-monitor/list-by-teacher/${teacherId}`)
  return normalizeList<ExamMonitor>(data)
}

/**
 * 查询某场考试的监考安排
 * 后端 GET /api/exam-monitor/list-by-exam/{examId}
 */
export async function listInvigilationsByExam(examId: number): Promise<ExamMonitor[]> {
  const data = await http.get<unknown>(`/exam-monitor/list-by-exam/${examId}`)
  return normalizeList<ExamMonitor>(data)
}
