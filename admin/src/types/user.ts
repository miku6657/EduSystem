export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  role: string
}

export interface LoginResult {
  token: string
  user: UserInfo
}
