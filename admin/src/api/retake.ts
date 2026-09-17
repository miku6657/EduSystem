import { http } from '@/utils/request'

export type RetakeStatus =
  | 'WAIT'
  | 'APPROVED'
  | 'REJECTED'
  | 'ARRANGED'
  | 'COMPLETED'

interface PageResult<T> {
  records: T[]
  total: number
}

interface RetakeRaw {
  id: string | number

  studentId: string | number

  courseId: string | number

  examId?: string | number | null

  type: string

  status: RetakeStatus

  createTime?: string

  updateTime?: string
}

interface StudentRaw {
  id: string | number

  studentNo: string

  name: string
}

interface CourseRaw {
  id: string | number

  courseCode: string

  name: string
}

interface ExamRaw {
  id: string | number

  name: string

  courseId: string | number

  examType?: string

  examDate?: string

  startTime?: string

  endTime?: string

  status?: string
}

export interface RetakeAuditItem {
  id: string

  studentId: string

  studentNo: string

  studentName: string

  courseId: string

  courseCode: string

  courseName: string

  examId?: string

  examName?: string

  type: string

  status: RetakeStatus

  createTime?: string
}

/**
 * 管理端查询所有“重修”申请。
 *
 * GET
 * /api/exam-retakes/type/重修
 */
export async function listRetakeAudits():
  Promise<RetakeAuditItem[]> {

  const [
    retakeData,
    studentPage,
    coursePage,
    examPage,
  ] =
    await Promise.all([
      http.get<RetakeRaw[]>(
        '/exam-retakes/type/重修',
      ),

      http.get<
        PageResult<StudentRaw>
      >(
        '/students',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),

      http.get<
        PageResult<CourseRaw>
      >(
        '/courses',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),

      http.get<
        PageResult<ExamRaw>
      >(
        '/exams',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  const studentMap =
    new Map(
      (
        studentPage.records
        ?? []
      ).map(
        item => [
          String(item.id),
          item,
        ],
      ),
    )

  const courseMap =
    new Map(
      (
        coursePage.records
        ?? []
      ).map(
        item => [
          String(item.id),
          item,
        ],
      ),
    )

  const examMap =
    new Map(
      (
        examPage.records
        ?? []
      ).map(
        item => [
          String(item.id),
          item,
        ],
      ),
    )

  return (
    retakeData
    ?? []
  ).map(
    item => {

      const student =
        studentMap.get(
          String(
            item.studentId,
          ),
        )

      const course =
        courseMap.get(
          String(
            item.courseId,
          ),
        )

      const exam =
        item.examId === null
        || item.examId === undefined
          ? undefined
          : examMap.get(
              String(
                item.examId,
              ),
            )

      return {
        id:
          String(
            item.id,
          ),

        studentId:
          String(
            item.studentId,
          ),

        studentNo:
          student?.studentNo
          ?? '—',

        studentName:
          student?.name
          ?? '未知学生',

        courseId:
          String(
            item.courseId,
          ),

        courseCode:
          course?.courseCode
          ?? '—',

        courseName:
          course?.name
          ?? '未知课程',

        examId:
          item.examId === null
          || item.examId === undefined
            ? undefined
            : String(
                item.examId,
              ),

        examName:
          exam?.name,

        type:
          item.type,

        status:
          item.status,

        createTime:
          item.createTime,
      }
    },
  )
}

/**
 * 审批通过
 *
 * WAIT -> APPROVED
 */
export function approveRetake(
  id: string,
) {

  return http.put<null>(
    `/exam-retakes/${id}/approve`,
  )
}

/**
 * 驳回
 *
 * WAIT -> REJECTED
 */
export function rejectRetake(
  id: string,
) {

  return http.put<null>(
    `/exam-retakes/${id}/reject`,
  )
}

export interface RetakeArrangePayload {
  examInfo: {
    termId: string

    examDate: string

    startTime: string

    endTime: string
  }

  classroomIds: string[]

  monitorTeacherIds: string[]
}

/**
 * 为已经批准的重修申请
 * 创建一场新的专门重修考试。
 *
 * APPROVED -> ARRANGED
 */
export function arrangeRetake(
  id: string,
  payload: RetakeArrangePayload,
) {

  return http.post<null>(
    `/exam-retakes/${id}/arrange`,
    payload,
  )
}
