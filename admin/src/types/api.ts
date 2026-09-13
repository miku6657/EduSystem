export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageQuery {
  page: number
  pageSize: number
  [key: string]: unknown
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
