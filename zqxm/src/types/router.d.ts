import 'vue-router'
import type { Component } from 'vue'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面/菜单标题 */
    title?: string
    /** 菜单图标（element-plus 图标组件） */
    icon?: Component
    /** 是否渲染到侧边栏菜单，默认 true */
    menu?: boolean
    /** 可访问该菜单的角色；不配置表示所有登录用户可见 */
    roles?: string[]
    /** 是否需要登录，默认 true */
    requiresAuth?: boolean
  }
}
