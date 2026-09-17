import { http } from '@/utils/request'

export interface BaseOption {
  id: number
  name: string
}

export function getMajorList() {
  return http.get<BaseOption[]>('/major/list')
}

export function getCampusList() {
  return http.get<BaseOption[]>('/campus/list')
}
