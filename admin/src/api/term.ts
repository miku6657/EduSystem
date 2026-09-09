import { http } from '@/utils/request'

/** 获取当前学期（用于顶部导航展示） */
export function getCurrentTerm() {
  return http.get<string>('/term/current')
}
