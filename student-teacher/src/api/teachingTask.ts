import { http, normalizeList } from '@/utils/request'

/** 教学任务（base_teaching_task；含上课时间，用于课表） */
export interface TeachingTask {
  id?: number
  teacherId?: number
  courseId: number
  classId: number
  termId?: number
  /** 星期几（1=周一 … 7=周日）；为空表示未排课 */
  weekday?: number
  startSection?: number
  endSection?: number
  classroomId?: number
  weeks?: string
  /** 展示用扩展字段（后端 Service 关联填充） */
  courseName?: string
  className?: string
  teacherName?: string
  roomName?: string
}

/** 星期几文案 */
export const WEEKDAY_TEXT: Record<number, string> = {
  1: '周一',
  2: '周二',
  3: '周三',
  4: '周四',
  5: '周五',
  6: '周六',
  7: '周日',
}

/**
 * 学生：我的课表（按班级查教学任务）
 * 后端 GET /api/teaching-tasks?classId=&termId=
 */
export async function listTasksByClass(classId: number, termId?: number): Promise<TeachingTask[]> {
  const data = await http.get<unknown>('/teaching-tasks', {
    classId,
    ...(termId ? { termId } : {}),
  })
  return normalizeList<TeachingTask>(data)
}

/**
 * 教师：我的教学任务（含上课时间）
 * 后端 GET /api/teaching-tasks?teacherId=&termId=
 */
export async function listTasksByTeacher(
  teacherId: number,
  termId?: number,
): Promise<TeachingTask[]> {
  const data = await http.get<unknown>('/teaching-tasks', {
    teacherId,
    ...(termId ? { termId } : {}),
  })
  return normalizeList<TeachingTask>(data)
}
