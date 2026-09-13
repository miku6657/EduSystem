import { http, normalizeList } from '@/utils/request'

/** 调课申请（course_adjust；status：待审核/已通过/已驳回/已撤销） */
export interface CourseAdjust {
  id?: number
  teacherId?: number
  courseId: number
  classId?: number
  /** 原上课日期 YYYY-MM-DD */
  originDate: string
  /** 原上课时段 */
  originSlot: string
  /** 调整后上课日期 */
  targetDate: string
  /** 调整后上课时段 */
  targetSlot: string
  /** 调整后教室ID（可空表示不变） */
  classroomId?: number
  reason?: string
  status?: string
  approveRemark?: string
  createTime?: string
  /** 展示用扩展字段（后端 Service 关联填充） */
  teacherName?: string
  courseName?: string
  className?: string
  roomName?: string
}

/**
 * 教师：提交调课申请
 * 后端 POST /api/course-adjusts（teacherId 由服务端按登录人写入）
 */
export function submitCourseAdjust(payload: CourseAdjust) {
  return http.post<null>('/course-adjusts', payload)
}

/**
 * 教师：我的调课（服务端按当前登录教师过滤）
 * 后端 GET /api/course-adjusts/my
 */
export async function listMyCourseAdjusts(): Promise<CourseAdjust[]> {
  const data = await http.get<unknown>('/course-adjusts/my')
  return normalizeList<CourseAdjust>(data)
}

/**
 * 教师：撤销自己的调课申请（仅待审核可撤销）
 * 后端 PUT /api/course-adjusts/{id}/cancel
 */
export function cancelCourseAdjust(id: number) {
  return http.put<null>(`/course-adjusts/${id}/cancel`)
}

/**
 * 管理端：审批列表（status 为空查全部）
 * 后端 GET /api/course-adjusts?status=
 */
export async function listCourseAdjusts(status?: string): Promise<CourseAdjust[]> {
  const data = await http.get<unknown>('/course-adjusts', status ? { status } : undefined)
  return normalizeList<CourseAdjust>(data)
}
