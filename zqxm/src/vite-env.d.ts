/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 应用标题 */
  readonly VITE_APP_TITLE: string
  /** 接口基础路径 */
  readonly VITE_API_BASE_URL: string
  /** 后端联调代理目标 */
  readonly VITE_PROXY_TARGET?: string
  /** 是否启用本地 mock */
  readonly VITE_USE_MOCK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
