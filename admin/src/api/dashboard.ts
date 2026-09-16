import { http } from '@/utils/request'

export type Id = string

export interface PendingApprovalItem {
  id: Id
  type: string
  title: string
  applicant: string
  applyTime: string
  status?: string
}

export interface DashboardStatistics {
  courseCount: number
  classroomCount: number
  todayAdjustCount: number
  pendingGraduationCount: number
  pendingApprovals: PendingApprovalItem[]

  pendingAdjust: number
  pendingClassroom: number
  classroomUsageRate: number
  notices: NoticeItem[]
  upcomingExams: ExamItem[]
}

export interface NoticeItem {
  id: number
  title: string
  time: string
}

export interface ExamItem {
  id: number
  examName: string
  courseName: string
  examDate: string
  status: string
}

interface PageData<T> {
  records?: T[]
  list?: T[]
  total?: number
}

interface StudentItem {
  id: Id
  studentNo: string
  name: string
  status?: string
  createTime?: string
}

interface GraduateCheckItem {
  id?: Id
  studentId: Id
  checkStatus?: string
  createTime?: string
}

interface ClassroomApplyItem {
  id: Id
  applicant?: string
  applyTime?: string
  createTime?: string
  roomName?: string
  purpose?: string
  status?: string
}

export async function getDashboardStatistics(): Promise<DashboardStatistics> {
  const [
    coursesResult,
    classroomsResult,
    studentsResult,
    graduateChecksResult,
    classroomAppliesResult,
  ] = await Promise.allSettled([
    http.get<PageData<unknown>>('/courses', {
      pageNo: 1,
      pageSize: 1,
    }),

    http.get<PageData<unknown>>('/classrooms', {
      pageNo: 1,
      pageSize: 1,
    }),

    http.get<PageData<StudentItem>>('/students', {
      pageNo: 1,
      pageSize: 1000,
    }),

    http.get<GraduateCheckItem[]>(
      '/graduate-checks',
    ),

    http.get<ClassroomApplyItem[]>(
      '/classroom-applies',
      {
        status: '待审核',
      },
    ),
  ])

  const courseCount =
    coursesResult.status === 'fulfilled'
      ? coursesResult.value.total ?? 0
      : 0

  const classroomCount =
    classroomsResult.status === 'fulfilled'
      ? classroomsResult.value.total ?? 0
      : 0

  const students =
    studentsResult.status === 'fulfilled'
      ? studentsResult.value.records
        ?? studentsResult.value.list
        ?? []
      : []

  const graduateChecks =
    graduateChecksResult.status === 'fulfilled'
      ? graduateChecksResult.value
      : []

  const checkMap = new Map(
    graduateChecks.map((check) => [
      String(check.studentId),
      check,
    ]),
  )

  /**
   * 待毕业审核：
   *
   * 1. 没有 graduate_check 记录
   * 2. 或者 checkStatus = WAIT
   *
   * 都属于“待审核”
   */
  const pendingGraduationStudents =
    students.filter((student) => {
      const check = checkMap.get(
        String(student.id),
      )

      if (!check) {
        return true
      }

      return (
        check.checkStatus === 'WAIT'
        || check.checkStatus === '待审核'
      )
    })

  const pendingGraduationCount =
    pendingGraduationStudents.length

  /**
   * 毕业审核待办
   */
  const graduationApprovals:
    PendingApprovalItem[] =
    pendingGraduationStudents.map(
      (student) => ({
        id: String(student.id),
        type: '毕业资格审核',
        title:
          `${student.name}（${student.studentNo}）毕业资格审核`,
        applicant: student.name,
        applyTime:
          student.createTime ?? '',
        status: '待审核',
      }),
    )

  /**
   * 教室申请待办
   */
  const classroomApplies =
    classroomAppliesResult.status
      === 'fulfilled'
      ? classroomAppliesResult.value
      : []

  const classroomApprovals:
    PendingApprovalItem[] =
    classroomApplies.map((item) => ({
      id: String(item.id),
      type: '教室申请审批',
      title:
        item.purpose
        || (
          item.roomName
            ? `${item.roomName} 使用申请`
            : '教室使用申请'
        ),
      applicant:
        item.applicant ?? '',
      applyTime:
        item.applyTime
        || item.createTime
        || '',
      status:
        item.status ?? '待审核',
    }))

  /**
   * 合并实际待办，只显示最近5条
   */
  const pendingApprovals = [
    ...graduationApprovals,
    ...classroomApprovals,
  ]
    .sort((a, b) => {
      if (!a.applyTime) return 1
      if (!b.applyTime) return -1

      return (
        new Date(b.applyTime).getTime()
        - new Date(a.applyTime).getTime()
      )
    })
    .slice(0, 5)

  return {
    courseCount,
    classroomCount,

    // 你的后端目前还没有真正的调课模块
    todayAdjustCount: 0,

    pendingGraduationCount,
    pendingApprovals,

    pendingAdjust: 0,
    pendingClassroom:
      classroomApprovals.length,
    classroomUsageRate: 0,
    notices: [],
    upcomingExams: [],
  }
}
