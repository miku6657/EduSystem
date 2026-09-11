/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** axios 基础路径，默认 /api */
  readonly VITE_API_BASE_URL?: string
  /** Vite 代理目标（后端地址） */
  readonly VITE_PROXY_TARGET?: string
  /** 是否启用本地 mock（'false' 表示关闭） */
  readonly VITE_USE_MOCK?: string
  /** 页面标题 */
  readonly VITE_APP_TITLE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
