import { http } from '@/utils/request'

/** 待办审批项 */
export interface TodoItem {
  id: number
  /** 业务类型：course-adjust（调课）/ classroom（教室申请） */
  type: 'course-adjust' | 'classroom'
  title: string
  applicant: string
  applyTime: string
  /** 点击跳转的审核列表页；对应审批页建好后后端按模块返回即可 */
  route: string
}

/** 获取审批待办列表（调课审批 + 教室申请审批），供顶部“待办提醒”使用 */
export function getTodoList() {
  return http.get<TodoItem[]>('/todo/list')
}
