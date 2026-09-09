import { http } from '@/utils/request'
import type { LoginParams, LoginResult, UserInfo } from '@/types/user'

/** 登录 */
export function login(data: LoginParams) {
  return http.post<LoginResult>('/auth/login', data)
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return http.get<UserInfo>('/auth/userinfo')
}

/** 退出登录 */
export function logout() {
  return http.post<null>('/auth/logout')
}

/** 获取当前用户角色（预留接口：当前 mock 固定返回 ['admin']，联调后返回真实角色） */
export function getCurrentRoles() {
  return http.get<string[]>('/auth/roles')
}
