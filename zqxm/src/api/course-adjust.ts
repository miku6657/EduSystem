import { http } from '@/utils/request'

/** 调课申请状态 */
export type CourseAdjustStatus = '待审核' | '已通过' | '已驳回'

/** 调课申请记录（管理端调课审批） */
export interface CourseAdjustRequest {
  id: number
  courseName: string
  /** 申请人（教师） */
  teacher: string
  reason: string
  originalTime: string
  newTime: string
  status: CourseAdjustStatus
  createTime: string
}

/** 调课审批列表（GET /course-adjust/audit/list） */
export function getCourseAdjustAuditList(params: {
  courseName?: string
  teacher?: string
  status?: '' | CourseAdjustStatus
}) {
  return http.get<CourseAdjustRequest[]>('/course-adjust/audit/list', params)
}

/** 调课审批：通过（PUT /course-adjust/approve/{id}） */
export function approveCourseAdjust(id: number) {
  return http.put<{ id: number; status: CourseAdjustStatus }>(`/course-adjust/approve/${id}`)
}

/** 调课审批：驳回（PUT /course-adjust/reject/{id}） */
export function rejectCourseAdjust(id: number) {
  return http.put<{ id: number; status: CourseAdjustStatus }>(`/course-adjust/reject/${id}`)
}