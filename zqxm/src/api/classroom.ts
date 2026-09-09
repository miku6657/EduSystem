import { http } from '@/utils/request'
import type { PageResult } from '@/types/api'

/** 教室资源（申请教室时的候选教室） */
export interface ClassroomItem {
  id: number
  name: string
  building: string
  roomNo: string
  capacity: number
  /** 教室类型：普通教室 / 多媒体教室 / 机房 / 报告厅等 */
  type: string
}

/** 教室占用 / 申请日历事件（按天粒度，来自 /classroom/occupancy） */
export interface ClassroomCalendarEvent {
  id: string
  roomId: number
  /** YYYY-MM-DD */
  date: string
  /** 展示标题 */
  title: string
  /** 教室名称（全校占用总览按教室区分） */
  roomName: string
  /**
   * 事件类型与颜色：
   * exam=考试占用(红) / class=教学占用(红) / pending=待审核申请(黄) / approved=已通过占用(红)
   */
  status: 'exam' | 'class' | 'pending' | 'approved'
  /** 使用人/申请人 */
  applicant?: string
  /** 申请人所在班级（师生端提交时填写） */
  className?: string
  /** 使用时段文案 */
  timeSlot?: string
  /** 用途：考试 / 日常教学 / 讲座/会议 / 社团活动 / 其他 */
  purpose?: string
  reason?: string
}

/** 教室申请状态（师生端提交后进入“待审核”，管理端审批后变为 已通过/已驳回） */
export type ClassroomApplyStatus = '待审核' | '已通过' | '已驳回' | '已取消'

/** 教室申请记录（我的申请 / H5 申请记录） */
export interface ClassroomApplyRecord {
  id: number
  roomId: number
  roomName: string
  /** 申请人姓名 */
  applicant: string
  /** 申请人所在班级 */
  className: string
  /** YYYY-MM-DD */
  date: string
  /** 时段文案，如 第3-4节 10:00~11:40 */
  timeSlot: string
  /** 用途：日常教学 / 考试 / 讲座/会议 / 社团活动 / 其他 */
  purpose: string
  reason: string
  status: ClassroomApplyStatus
  /** 申请提交时间 YYYY-MM-DD HH:mm:ss */
  applyTime: string
}

/** 提交教室申请入参 */
export interface CreateClassroomApplyParams {
  roomId: number
  date: string
  timeSlot: string
  purpose: string
  reason: string
  className?: string
  applicant?: string
}

/** 教室列表（日历顶部下拉选择） */
export function getClassroomList() {
  return http.get<ClassroomItem[]>('/classroom/list')
}

/** 全校教室在某日期范围的占用总览（PC 日历月视图，管理端只读） */
export function getClassroomOccupancy(params: {
  /** 开始日期 YYYY-MM-DD */
  start: string
  /** 结束日期 YYYY-MM-DD */
  end: string
}) {
  return http.get<ClassroomCalendarEvent[]>('/classroom/occupancy', params)
}

/** 提交教室申请（仅师生端调用；管理端代码不再发起申请，此函数仅供后续师生端联调参考） */
export function createClassroomApply(data: CreateClassroomApplyParams) {
  return http.post<ClassroomApplyRecord>('/classroom/apply', data)
}

/** 我的教室申请记录（分页；仅师生端使用） */
export function getMyClassroomApplies(params: Record<string, unknown>) {
  return http.get<PageResult<ClassroomApplyRecord>>('/classroom-apply/my/list', params)
}

/** 取消教室申请（仅师生端使用；后端校验：提交后 30 分钟内不可取消） */
export function cancelClassroomApply(id: number) {
  return http.post<{ id: number; status: ClassroomApplyStatus }>('/classroom-apply/cancel', { id })
}

/** 管理端：审批状态筛选项（'' 表示全部） */
export type ClassroomApprovalFilter = '' | '待审核' | '已通过' | '已驳回'

/** 管理端：待审批列表（GET /classroom/approval/list） */
export function getClassroomApprovalList(params?: { status?: ClassroomApprovalFilter }) {
  return http.get<ClassroomApplyRecord[]>('/classroom/approval/list', params)
}

/** 管理端：通过教室申请（PUT /classroom/approve/{id}） */
export function approveClassroomApply(id: number) {
  return http.put<ClassroomApplyRecord>(`/classroom/approve/${id}`)
}

/** 管理端：驳回教室申请（PUT /classroom/reject/{id}） */
export function rejectClassroomApply(id: number) {
  return http.put<ClassroomApplyRecord>(`/classroom/reject/${id}`)
}
