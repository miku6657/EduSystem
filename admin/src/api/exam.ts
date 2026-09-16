import { http } from '@/utils/request'

export type Id = string

export interface PageResult<T> {
  records: T[]
  total: number
  size?: number
  current?: number
  pages?: number
}

export interface CourseItem {
  id: Id
  courseCode: string
  name: string
  credit?: number
  type?: string
  teachingGroupId?: Id
  teacherId?: Id
  termId?: Id
}

export interface ClassroomItem {
  id: Id
  roomNo: string
  campusId?: Id
  type?: string
  area?: number
  capacity?: number
  status?: string
}

export interface TeacherItem {
  id: Id
  teacherNo: string
  name: string
  gender?: string
  type?: string
  phone?: string
  departmentId?: Id
  teachingGroupId?: Id
}

export interface ExamInfoParams {
  name: string
  courseId: Id
  termId: Id
  examType: string
  examDate: string
  startTime: string
  endTime: string
}

export interface SaveExamScheduleParams {
  examInfo: ExamInfoParams
  classroomIds: Id[]
  monitorTeacherIds: Id[]
}

export interface ExamArrangeItem {
  courseId: Id
  courseName: string
  classroomId: Id
  classroomName: string
  teacherId: Id
  teacherName: string
  examDate: string
  startTime: string
  endTime: string
  saved: boolean
}

/**
 * 查询课程
 * GET /api/courses
 */
export function getArrangeCourses() {
  return http.get<PageResult<CourseItem>>('/courses', {
    pageNo: 1,
    pageSize: 1000,
  })
}

/**
 * 查询当前空闲教室
 * GET /api/classrooms/free
 */
export function getFreeClassrooms() {
  return http.get<ClassroomItem[]>('/classrooms/free')
}

/**
 * 查询教师
 * GET /api/teachers
 */
export function getArrangeTeachers() {
  return http.get<PageResult<TeacherItem>>('/teachers', {
    pageNo: 1,
    pageSize: 1000,
  })
}

/**
 * 保存一条排考结果
 * POST /api/exam-schedules
 */
export function saveExamSchedule(data: SaveExamScheduleParams) {
  return http.post<void>('/exam-schedules', data)
}

/**
 * 数据库中已有的考试
 */
export interface ExistingExamItem {
  id: Id
  name: string
  courseId: Id
  termId: Id
  examType?: string
  examDate: string
  startTime: string
  endTime: string
  status?: string
}

/**
 * 已有考试与教室的关联
 */
export interface ExistingExamRoomItem {
  id: Id
  examId: Id
  classroomId: Id
  seatCount?: number
}

/**
 * 已有考试与监考教师的关联
 */
export interface ExistingExamMonitorItem {
  id: Id
  examId: Id
  teacherId: Id
  monitorRole?: string
}

/**
 * 查询数据库已有考试
 *
 * GET /api/exams
 */
export function getExistingExams() {
  return http.get<PageResult<ExistingExamItem>>(
    '/exams',
    {
      pageNo: 1,
      pageSize: 1000,
    },
  )
}

/**
 * 查询数据库已有考场安排
 *
 * GET /api/exam-rooms
 */
export function getExistingExamRooms() {
  return http.get<ExistingExamRoomItem[]>(
    '/exam-rooms',
  )
}

/**
 * 查询数据库已有监考安排
 *
 * GET /api/exam-monitors
 */
export function getExistingExamMonitors() {
  return http.get<ExistingExamMonitorItem[]>(
    '/exam-monitors',
  )
}

/* ==================== 考核方式申报审核 ==================== */

export type MethodAuditStatus = '待审核' | '已通过' | '已驳回'

export interface ExamMethodApply {
  id: number
  courseName: string
  className: string
  teacher: string
  methodName: string
  reason: string
  status: MethodAuditStatus
  createTime: string
}

export function getMethodAuditList(params: {
  courseName?: string
  teacher?: string
  status?: '' | MethodAuditStatus
}) {
  return http.get<ExamMethodApply[]>('/exam-applies/export', params)
}

export function approveMethodAudit(id: number) {
  return http.put<{ id: number; status: MethodAuditStatus }>(
    `/exam-applies/${id}/audit`,
    undefined,
    {
      params: { status: 'PASS' },
    },
  )
}

export function rejectMethodAudit(id: number) {
  return http.put<{ id: number; status: MethodAuditStatus }>(
    `/exam-applies/${id}/audit`,
    undefined,
    {
      params: { status: 'FAIL' },
    },
  )
}