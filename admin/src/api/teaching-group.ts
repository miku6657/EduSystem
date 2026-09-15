import { http } from '@/utils/request'

/** 教研室信息 */
export interface TeachingGroup {
  id: number | string
  name: string
  departmentId?: number | string
}

/** 查询教研室列表 */
export function getTeachingGroupList(params?: { departmentId?: number | string }) {
  return http.get<TeachingGroup[]>('/teaching-groups', params)
}