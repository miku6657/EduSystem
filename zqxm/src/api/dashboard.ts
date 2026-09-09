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
export function getDashboardStatistics() {
  return http.get<DashboardStatistics>('/dashboard/statistics')
}
