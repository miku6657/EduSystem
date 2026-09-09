/** 登录请求参数 */
export interface LoginParams {
  username: string
  password: string
}

/** 登录返回结果（mock 提供 token / roles / name） */
export interface LoginResult {
  token: string
  roles: string[]
  name: string
}

/** 当前登录用户信息 */
export interface UserInfo {
  id: number
  username: string
  name: string
  roles: string[]
}
