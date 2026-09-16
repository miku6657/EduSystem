import { http } from '@/utils/request'
import { normalizeRole } from '@/utils/role'
import type {
  LoginParams,
  LoginResult,
  LoginResultRaw,
  Role,
} from '@/types/user'

/**
 * 后端 /api/auth/userinfo 返回结构
 */
export interface UserInfoRaw {
  id: number
  username: string
  role: string
}

/**
 * 师生端归一化后的用户信息
 */
export interface UserInfo {
  id: number
  username: string
  role: Role
}

/**
 * 普通账号密码登录
 *
 * 暂时保留，后面统一 CAS 后可以不再从页面使用。
 */
export async function login(
  params: LoginParams,
): Promise<LoginResult> {
  const raw = await http.post<LoginResultRaw>(
    '/auth/login',
    params,
  )

  const role =
    normalizeRole(raw.roles?.[0])
    ?? normalizeRole(raw.user?.role)
    ?? 'student'

  return {
    token: raw.token,
    role,
    name:
      raw.name
      || raw.user?.username
      || params.username,
    userId: raw.user?.id ?? 0,
    username:
      raw.user?.username
      || params.username,
  }
}

/**
 * 获取当前 CAS / JWT 登录用户
 *
 * GET /api/auth/userinfo
 */
export async function getUserInfo(): Promise<UserInfo> {
  const raw =
    await http.get<UserInfoRaw>(
      '/auth/userinfo',
    )

  const role =
    normalizeRole(raw.role)

  if (!role) {
    throw new Error(
      `当前账号角色 ${raw.role} 不能进入师生端`,
    )
  }

  return {
    id: raw.id,
    username: raw.username,
    role,
  }
}

/**
 * 退出登录
 *
 * JWT 为无状态登录，后端接口只是语义化接口，
 * 真正退出时前端还需要清除本地 token。
 */
export function logout() {
  return http.post<null>(
    '/auth/logout',
  )
}