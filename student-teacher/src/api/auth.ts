import { http } from '@/utils/request'
import { normalizeRole } from '@/utils/role'
import type { LoginParams, LoginResult, LoginResultRaw } from '@/types/user'

/**
 * 登录并归一化返回。
 * - 当前后端返回：{ token, user: { id, username, role: 'STUDENT' } }
 * - Mock/手册约定：{ token, roles: ['student'], name: '王小明' }
 * 两种形状都能吃下，页面只拿 LoginResult。
 */
export async function login(params: LoginParams): Promise<LoginResult> {
  const raw = await http.post<LoginResultRaw>('/auth/login', params)
  const role = normalizeRole(raw.roles?.[0]) ?? normalizeRole(raw.user?.role) ?? 'student'
  return {
    token: raw.token,
    role,
    name: raw.name || raw.user?.username || params.username,
    userId: raw.user?.id ?? 0,
    username: raw.user?.username || params.username,
  }
}

/** 退出登录：后端暂无该接口，失败可忽略（前端清本地登录态即可） */
export function logout() {
  return http.post<null>('/auth/logout')
}
