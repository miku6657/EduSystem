const TOKEN_KEY = 'st_token'
const ROLE_KEY = 'st_role'
const NAME_KEY = 'st_name'
const USER_ID_KEY = 'st_user_id'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRole(): string {
  return localStorage.getItem(ROLE_KEY) ?? ''
}

export function setRole(role: string): void {
  localStorage.setItem(ROLE_KEY, role)
}

export function getName(): string {
  return localStorage.getItem(NAME_KEY) ?? ''
}

export function setName(name: string): void {
  localStorage.setItem(NAME_KEY, name)
}

export function getUserId(): number {
  return Number(localStorage.getItem(USER_ID_KEY) ?? 0)
}

export function setUserId(id: number): void {
  localStorage.setItem(USER_ID_KEY, String(id))
}

/**
 * 学生/教师业务身份缓存：登录名与业务ID（studentId/teacherId）不是一回事，
 * 解析结果缓存在本地，避免每次进页面都请求一次。
 */
export function getCachedProfile<T>(): T | null {
  const raw = localStorage.getItem('st_profile')
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as T
  } catch {
    return null
  }
}

export function setCachedProfile(profile: unknown): void {
  localStorage.setItem('st_profile', JSON.stringify(profile))
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(NAME_KEY)
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem('st_profile')
}
