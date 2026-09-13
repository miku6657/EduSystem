import { http } from '@/utils/request'
import type {
  LoginParams,
  LoginResult,
  UserInfo,
} from '@/types/user'

export function login(data: LoginParams) {
  return http.post<LoginResult>('/auth/login', data)
}

/** 当前登录用户 */
export function getUserInfo() {
  return http.get<UserInfo>('/auth/userinfo')
}

/** 发起 CAS 登录 */
export function casLogin() {
  window.location.href = '/api/auth/cas/login'
}
