import axios from 'axios'
import type {
  AxiosError,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'
import type { ApiResult } from '@/types/api'
import { getToken, removeToken } from '@/utils/storage'

/** 业务错误（服务端返回 code !== 0 或网络异常） */
export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

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

// 响应拦截：统一处理 { code, data, msg } 与网络错误
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res.code === 0) {
      return response
    }
    if (res.code === 401) {
      removeToken()
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new ApiError(res.code, res.msg || '登录已过期'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new ApiError(res.code, res.msg || '请求失败'))
  },
  (error: AxiosError<ApiResult>) => {
    ElMessage.error(error.response?.data?.msg || error.message || '网络异常，请稍后重试')
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
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return requestData<T>({ url, method: 'POST', data, ...config })
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return requestData<T>({ url, method: 'PUT', data, ...config })
  },
  delete<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig) {
    return requestData<T>({ url, method: 'DELETE', params, ...config })
  },
}
