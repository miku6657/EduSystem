import { http } from '@/utils/request'

/** 待办审批条目 */
export interface PendingApprovalItem {
  id: number
  /** 审批类型文案，如 调课审批 / 教室申请审批 */
  type: string
  /** 审批事项标题 */
  title: string
  applicant: string
  applyTime: string
  status?: string
}

/** 后台管理端工作台统计 */
export interface DashboardStatistics {
  /** 总课程数 */
  courseCount: number
  /** 总教室数 */
  classroomCount: number
  /** 今日调课申请数 */
  todayAdjustCount: number
  /** 待审核毕业人数 */
  pendingGraduationCount: number
  /** 最近 5 条待办审批 */
  pendingApprovals: PendingApprovalItem[]

  /* ====== 以下为 H5 示例页复用的历史字段，管理端工作台不展示 ====== */
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

/** 获取后台首页统计数据 */
export async function getDashboardStatistics(): Promise<DashboardStatistics> {
  type PageData<T> = { records?: T[]; list?: T[]; total?: number }
  type GraduateCheck = { studentId?: number; checkStatus?: string }
  type ClassroomApply = {
    id: number
    applicant?: string
    applyTime?: string
    roomName?: string
    status?: string
  }

  const [courses, classrooms, graduateChecks, classroomApplies] = await Promise.allSettled([
    http.get<PageData<unknown>>('/courses', { pageNo: 1, pageSize: 1 }),
    http.get<PageData<unknown>>('/classrooms', { pageNo: 1, pageSize: 1 }),
    http.get<GraduateCheck[]>('/graduate-checks', { checkStatus: 'WAIT' }),
    http.get<ClassroomApply[]>('/classroom-applies', { status: '待审核' }),
  ])

  const pageTotal = (result: PromiseSettledResult<PageData<unknown>>) =>
    result.status === 'fulfilled' ? result.value.total ?? 0 : 0
  const pendingGraduationCount = graduateChecks.status === 'fulfilled'
    ? graduateChecks.value.filter((item) => item.checkStatus === 'WAIT' || item.checkStatus === '待审核').length
    : 0
  const pendingApprovals: PendingApprovalItem[] = classroomApplies.status === 'fulfilled'
    ? classroomApplies.value.slice(0, 5).map((item) => ({
      id: item.id,
      type: '教室申请审批',
      title: item.roomName ? `${item.roomName} 使用申请` : '教室使用申请',
      applicant: item.applicant ?? '',
      applyTime: item.applyTime ?? '',
      status: item.status,
    }))
    : []

  return {
    courseCount: pageTotal(courses),
    classroomCount: pageTotal(classrooms),
    todayAdjustCount: 0,
    pendingGraduationCount,
    pendingApprovals,
    pendingAdjust: 0,
    pendingClassroom: pendingApprovals.length,
    classroomUsageRate: 0,
    notices: [],
    upcomingExams: [],
  }
}
