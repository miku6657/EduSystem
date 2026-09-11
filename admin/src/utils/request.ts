import axios from 'axios'
import type {
  AxiosError,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'

import type { ApiResult } from '@/types/api'
import { getToken, removeStorage, removeToken } from '@/utils/storage'

export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

/**
 * 请求前自动携带 JWT
 */
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
)

/**
 * 响应处理
 *
 * 后端格式：
 * {
 *   code: 200,
 *   message: "success",
 *   data: ...
 * }
 */
instance.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data

    if (res.code === 200) {
      return response
    }

    ElMessage.error(res.message || '请求失败')

    return Promise.reject(
      new ApiError(
        res.code,
        res.message || '请求失败',
      ),
    )
  },

  (error: AxiosError<ApiResult>) => {

    if (error.response?.status === 401) {
      removeToken()
      removeStorage('userInfo')

      ElMessage.error('登录已失效，请重新登录')

      window.location.href = '/login'

      return Promise.reject(error)
    }

    if (error.response?.status === 403) {
      ElMessage.error('没有权限访问')

      return Promise.reject(error)
    }

    const message =
      error.response?.data?.message
      || error.message
      || '网络异常'

    ElMessage.error(message)

    return Promise.reject(error)
  },
)

async function requestData<T>(
  config: AxiosRequestConfig,
): Promise<T> {
  const response =
    await instance.request<ApiResult<T>>(config)

  return response.data.data
}

export const http = {
  get<T>(
    url: string,
    params?: Record<string, unknown>,
    config?: AxiosRequestConfig,
  ) {
    return requestData<T>({
      url,
      method: 'GET',
      params,
      ...config,
    })
  },

  post<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ) {
    return requestData<T>({
      url,
      method: 'POST',
      data,
      ...config,
    })
  },

  put<T>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ) {
    return requestData<T>({
      url,
      method: 'PUT',
      data,
      ...config,
    })
  },

  delete<T>(
    url: string,
    params?: Record<string, unknown>,
    config?: AxiosRequestConfig,
  ) {
    return requestData<T>({
      url,
      method: 'DELETE',
      params,
      ...config,
    })
  },
}
