import { http } from '@/utils/request'
/** 教室资源（申请教室时的候选教室） */
export interface ClassroomItem {
  id: number
  roomNo: string
  campusId: number
  type: string
  area?: number
  capacity?: number
  status?: string
}

/** 教室占用 / 申请日历事件（按天粒度） */
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

/** 分页查询教室 */
export function getClassroomList(params: { pageNo?: number; pageSize?: number } = {}) {
  return http.get<{ records: ClassroomItem[]; total: number }>('/classrooms', {
    pageNo: params.pageNo ?? 1,
    pageSize: params.pageSize ?? 10,
  })
}

/** 提交教室申请（仅师生端调用；管理端代码不再发起申请，此函数仅供后续师生端联调参考） */
export function createClassroomApply(data: CreateClassroomApplyParams) {
  return http.post<ClassroomApplyRecord>('/classroom-applies', data)
}

/** 我的教室申请记录（仅师生端使用） */
export function getMyClassroomApplies() {
  return http.get<ClassroomApplyRecord[]>('/classroom-applies/my')
}

/** 管理端：审批状态筛选项（'' 表示全部） */
export type ClassroomApprovalFilter = '' | '待审核' | '已通过' | '已驳回'

/** 管理端：待审批列表（GET /classroom-applies） */
export function getClassroomApprovalList(params?: { status?: ClassroomApprovalFilter }) {
  return http.get<ClassroomApplyRecord[]>('/classroom-applies', params)
}

/** 管理端：通过教室申请（PUT /classroom-applies/{id}/approve） */
export function approveClassroomApply(id: number) {
  return http.put<ClassroomApplyRecord>(`/classroom-applies/${id}/approve`)
}

/** 管理端：驳回教室申请（PUT /classroom-applies/{id}/reject） */
export function rejectClassroomApply(id: number) {
  return http.put<ClassroomApplyRecord>(`/classroom-applies/${id}/reject`)
}
