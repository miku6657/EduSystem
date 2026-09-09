/** 后端统一响应结构：{ code: 0, data, msg } */
export interface ApiResult<T = unknown> {
  code: number
  data: T
  msg: string
}

/** 分页查询参数 */
export interface PageQuery {
  page: number
  pageSize: number
  [key: string]: unknown
}

/** 分页返回结构 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
