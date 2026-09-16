import { getPage, http, normalizeList, pageParams } from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/api'

/** 教室（base_classroom） */
export interface Classroom {
  id?: number
  roomNo: string
  campusId?: number
  type?: string
  area?: number
  capacity?: number
  status?: string
}

/** 教室申请（classroom_apply；status：待审核/已通过/已驳回/已取消） */
export interface ClassroomApply {
  id?: number
  roomId: number
  /** 申请人登录名：后端由 Authentication 自动写入，前端提交时无需传 */
  applicant?: string
  className?: string
  /** YYYY-MM-DD */
  applyDate: string
  timeSlot: string
  purpose?: string
  reason?: string
  status?: string
  createTime?: string
  /** 展示用扩展字段 */
  roomName?: string
}

/**
 * 分页查询教室
 * 后端 GET /api/classrooms?pageNo&pageSize&roomNo&campusId&type&status
 */
export function pageClassrooms(
  query: PageQuery & { roomNo?: string; campusId?: number; type?: string; status?: string },
): Promise<PageResult<Classroom>> {
  return getPage<Classroom>('/classrooms', pageParams(query))
}

/**
 * 查询空闲教室
 * 后端 GET /api/classrooms/free?campusId&type
 */
export async function listFreeClassrooms(params?: {
  campusId?: number
  type?: string
}): Promise<Classroom[]> {
  const data = await http.get<unknown>('/classrooms/free', params)
  return normalizeList<Classroom>(data)
}

/**
 * 师生端：提交教室使用申请（提交后状态为"待审核"）
 * 后端 POST /api/classroom-applies
 * 注意：同教室 + 同日期 + 同时段已被占用时后端会拒绝。
 */
export function submitClassroomApply(payload: ClassroomApply) {
  return http.post<null>('/classroom-applies', payload)
}

/**
 * 师生端：我的申请
 * 后端 GET /api/classroom-applies/my（服务端按当前登录人过滤）
 */
export async function listMyClassroomApplies(): Promise<ClassroomApply[]> {
  const data = await http.get<unknown>('/classroom-applies/my')
  return normalizeList<ClassroomApply>(data)
}

/**
 * 师生端：撤回申请（仅本人、仅"待审核"可撤回）
 * 后端 PUT /api/classroom-applies/{id}/cancel
 */
export function cancelClassroomApply(id: number) {
  return http.put<null>(`/classroom-applies/${id}/cancel`)
}
