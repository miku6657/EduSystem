import 'vue-router'
import type { Role } from '@/types/user'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题（用于菜单与 document.title） */
    title?: string
    /** 允许访问的角色；不配置表示登录即可访问 */
    roles?: Role[]
    /** 是否需要登录，默认 true */
    requiresAuth?: boolean
    /** 是否在底部 tabbar 显示 */
    tabbar?: boolean
    /** tabbar 图标名（Vant Icon 的 name） */
    icon?: string
  }
}
