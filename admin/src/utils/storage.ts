const STORAGE_PREFIX = 'tpms:'
const TOKEN_KEY = 'token'

/** 读取 localStorage（自动处理 JSON 序列化） */
export function getStorage<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(STORAGE_PREFIX + key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

/** 写入 localStorage */
export function setStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(STORAGE_PREFIX + key, JSON.stringify(value))
  } catch {
    // 隐私模式 / 存储被禁用等场景下忽略写入异常
  }
}

/** 删除 localStorage 项 */
export function removeStorage(key: string): void {
  try {
    localStorage.removeItem(STORAGE_PREFIX + key)
  } catch {
    // ignore
  }
}

/** 读取登录 Token（空字符串表示未登录） */
export function getToken(): string {
  return getStorage<string>(TOKEN_KEY) ?? ''
}

/** 写入登录 Token */
export function setToken(token: string): void {
  setStorage(TOKEN_KEY, token)
}

/** 清除登录 Token */
export function removeToken(): void {
  removeStorage(TOKEN_KEY)
}
