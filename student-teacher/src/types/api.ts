/**
 * 接口层通用类型。
 *
 * 重要：本项目同时兼容两种响应契约，页面代码无需关心差异（由 utils/request.ts 统一归一化）：
 * - 前端/Mock 约定：{ code: 0, data, msg }
 * - 当前后端实际：  { code: 200, message, data }
 */
export interface ApiResult<T = unknown> {
  code: number
  data: T
  /** 前端/Mock 约定的消息字段 */
  msg?: string
  /** 后端 Result 实际使用的消息字段 */
  message?: string
}

/** 分页请求参数（前端约定；后端实际使用 pageNo/pageSize，由 api 层做映射） */
export interface PageQuery {
  page: number
  pageSize: number
  [key: string]: unknown
}

/** 归一化后的分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** 后端 MyBatis-Plus Page 的字段形状（用于归一化） */
export interface MpPage<T> {
  records: T[]
  total: number
  current: number
  size: number
}
