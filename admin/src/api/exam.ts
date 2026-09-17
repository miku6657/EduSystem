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

/**
 * 后端真实状态：
 *
 * WAIT = 待审核
 * PASS = 已通过
 * FAIL = 已驳回
 */
export type MethodAuditStatus =
  | 'WAIT'
  | 'PASS'
  | 'FAIL'

/**
 * 后端 /exam-applies/export
 * 原始返回结构。
 */
interface ExamMethodApplyRaw {
  id: Id

  courseId: Id

  teacherId: Id

  applyType: string

  reason?: string

  status: MethodAuditStatus

  createTime?: string

  updateTime?: string
}

/**
 * admin 页面最终使用的数据。
 *
 * courseName / teacherName
 * 由前端根据ID关联出来。
 */
export interface ExamMethodApply {
  id: Id

  courseId: Id

  teacherId: Id

  courseCode?: string

  courseName: string

  teacherName: string

  applyType: string

  reason: string

  status: MethodAuditStatus

  createTime?: string
}

/**
 * 查询考核方式申报列表。
 *
 * 后端已有：
 *
 * GET /api/exam-applies/export
 * GET /api/courses
 * GET /api/teachers
 *
 * 后端没有直接返回课程名和教师名，
 * 所以前端根据ID关联。
 */
export async function getMethodAuditList(
  params: {
    courseName?: string

    teacher?: string

    status?:
      | ''
      | MethodAuditStatus
  },
): Promise<
  ExamMethodApply[]
> {

  const [
    applies,
    coursePage,
    teacherPage,
  ] =
    await Promise.all([
      http.get<
        ExamMethodApplyRaw[]
      >(
        '/exam-applies/export',
        {
          status:
            params.status
            || undefined,
        },
      ),

      http.get<
        PageResult<CourseItem>
      >(
        '/courses',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),

      http.get<
        PageResult<TeacherItem>
      >(
        '/teachers',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  /**
   * courseId -> Course
   */
  const courseMap =
    new Map(
      coursePage.records.map(
        (course) => [
          String(course.id),
          course,
        ],
      ),
    )

  /**
   * teacherId -> Teacher
   */
  const teacherMap =
    new Map(
      teacherPage.records.map(
        (teacher) => [
          String(teacher.id),
          teacher,
        ],
      ),
    )

  /**
   * 后端Entity
   * ↓
   * admin展示对象
   */
  let rows =
    applies.map(
      (item) => {

        const course =
          courseMap.get(
            String(
              item.courseId,
            ),
          )

        const teacher =
          teacherMap.get(
            String(
              item.teacherId,
            ),
          )

        return {
          id:
            String(
              item.id,
            ),

          courseId:
            String(
              item.courseId,
            ),

          teacherId:
            String(
              item.teacherId,
            ),

          courseCode:
            course?.courseCode,

          courseName:
            course?.name
            ?? '未知课程',

          teacherName:
            teacher?.name
            ?? '未知教师',

          applyType:
            item.applyType,

          reason:
            item.reason
            ?? '',

          status:
            item.status,

          createTime:
            item.createTime,
        }
      },
    )

  /**
   * courseName / teacher
   * 后端不支持这两个查询参数，
   * 所以在admin前端筛选。
   */
  const courseKeyword =
    params.courseName
      ?.trim()
      .toLowerCase()

  if (courseKeyword) {
    rows =
      rows.filter(
        (item) =>
          item.courseName
            .toLowerCase()
            .includes(
              courseKeyword,
            )
          ||
          item.courseCode
            ?.toLowerCase()
            .includes(
              courseKeyword,
            ),
      )
  }

  const teacherKeyword =
    params.teacher
      ?.trim()
      .toLowerCase()

  if (teacherKeyword) {
    rows =
      rows.filter(
        (item) =>
          item.teacherName
            .toLowerCase()
            .includes(
              teacherKeyword,
            ),
      )
  }

  return rows
}

/**
 * 通过
 *
 * 真实后端：
 *
 * PUT
 * /api/exam-applies/{id}/audit
 * ?status=PASS
 */
export function approveMethodAudit(
  id: Id,
) {

  return http.put<void>(
    `/exam-applies/${id}/audit`,
    undefined,
    {
      params: {
        status: 'PASS',
      },
    },
  )
}

/**
 * 驳回
 *
 * 真实后端：
 *
 * PUT
 * /api/exam-applies/{id}/audit
 * ?status=FAIL
 */
export function rejectMethodAudit(
  id: Id,
) {

  return http.put<void>(
    `/exam-applies/${id}/audit`,
    undefined,
    {
      params: {
        status: 'FAIL',
      },
    },
  )
}