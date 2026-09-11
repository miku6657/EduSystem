import axios from 'axios'
import type {
  AxiosError,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'
import { showToast } from 'vant'
import type { ApiResult, MpPage, PageQuery, PageResult } from '@/types/api'
import { clearAuth, getToken } from '@/utils/storage'

/** 业务错误（服务端 code 非成功值，或网络异常） */
export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

/**
 * 视为成功的状态码：
 * - 0   前端/Mock 及 API_MANUAL 的约定
 * - 200 当前后端 ResultCode.SUCCESS 的实际取值
 * 两者都接受，避免"后端改了契约前端全红"。
 */
const SUCCESS_CODES = [0, 200]

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

// 请求拦截：自动携带 Token
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** 兼容 msg（前端约定）与 message（后端实际）两种消息字段 */
function messageOf(res?: ApiResult): string {
  return res?.msg || res?.message || '请求失败'
}

function redirectToLogin(): void {
  clearAuth()
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

// 响应拦截：兼容两种响应契约，统一错误提示
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    // 非标准响应体（例如被 CAS 中转的 HTML）：原样放行，交给调用方处理
    if (!res || typeof res.code !== 'number') {
      return response
    }
    if (SUCCESS_CODES.includes(res.code)) {
      return response
    }
    if (res.code === 401) {
      showToast(messageOf(res) || '登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(new ApiError(res.code, messageOf(res)))
    }
    showToast(messageOf(res))
    return Promise.reject(new ApiError(res.code, messageOf(res)))
  },
  (error: AxiosError<ApiResult>) => {
    const status = error.response?.status
    if (status === 401) {
      showToast('登录已过期，请重新登录')
      redirectToLogin()
      return Promise.reject(error)
    }
    showToast(messageOf(error.response?.data) === '请求失败' ? '网络异常，请稍后重试' : messageOf(error.response?.data))
    return Promise.reject(error)
  },
)

/** 发送请求并直接返回业务数据（data 字段） */
async function requestData<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await instance.request<ApiResult<T>>(config)
  return response.data.data
}

export const http = {
  get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return requestData<T>({ url, method: 'GET', params, ...config })
  },
  post<T>(url: string, data?: unknown, params?: Record<string, unknown>) {
    return requestData<T>({ url, method: 'POST', data, params })
  },
  put<T>(url: string, data?: unknown, params?: Record<string, unknown>) {
    return requestData<T>({ url, method: 'PUT', data, params })
  },
  del<T>(url: string, params?: Record<string, unknown>) {
    return requestData<T>({ url, method: 'DELETE', params })
  },
}

/**
 * 把后端 MyBatis-Plus Page（records/total/current/size）、
 * 纯数组、以及前端约定的 {list,total,page,pageSize} 统一成 PageResult。
 */
export function normalizePage<T>(data: unknown): PageResult<T> {
  if (!data) {
    return { list: [], total: 0, page: 1, pageSize: 10 }
  }
  if (Array.isArray(data)) {
    const list = data as T[]
    return { list, total: list.length, page: 1, pageSize: list.length || 10 }
  }
  const anyData = data as Record<string, unknown>
  if (Array.isArray(anyData.list)) {
    return {
      list: anyData.list as T[],
      total: Number(anyData.total ?? (anyData.list as T[]).length),
      page: Number(anyData.page ?? 1),
      pageSize: Number(anyData.pageSize ?? 10),
    }
  }
  const mp = data as MpPage<T>
  if (Array.isArray(mp.records)) {
    return {
      list: mp.records,
      total: Number(mp.total ?? mp.records.length),
      page: Number(mp.current ?? 1),
      pageSize: Number(mp.size ?? 10),
    }
  }
  return { list: [], total: 0, page: 1, pageSize: 10 }
}

/** 把后端返回（数组 / 分页对象 / null）统一成数组 */
export function normalizeList<T>(data: unknown): T[] {
  return normalizePage<T>(data).list
}

/** 分页接口：自动把两种分页结构归一化 */
export async function getPage<T>(
  url: string,
  params?: Record<string, unknown>,
): Promise<PageResult<T>> {
  const data = await http.get<unknown>(url, params)
  return normalizePage<T>(data)
}

/**
 * 分页参数转换：前端用 page/pageSize，后端用 pageNo/pageSize。
 * 两套参数同时下发，后端读哪个都能取到值。
 */
export function pageParams(query: PageQuery): Record<string, unknown> {
  const { page, pageSize, ...rest } = query
  // page/pageSize 给前端约定与 Mock，pageNo/pageSize 给后端 Controller
  return { page, pageSize, pageNo: page, ...rest }
}
