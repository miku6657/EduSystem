import type { RouteRecordRaw } from 'vue-router'
import type { Role } from '@/types/user'

/** 判断某路由是否允许当前角色访问（未配置 roles 表示登录即可访问） */
export function hasRoutePermission(roles: Role[] | undefined, role: Role | null): boolean {
  if (!roles || roles.length === 0) {
    return true
  }
  if (!role) {
    return false
  }
  return roles.includes(role)
}

/** 取出当前角色在底部 tabbar 中应显示的路由（按路由 meta.tabbar 标记过滤） */
export function filterTabbarRoutes(routes: RouteRecordRaw[], role: Role | null): RouteRecordRaw[] {
  return routes.filter(
    (route) => route.meta?.tabbar === true && hasRoutePermission(route.meta?.roles, role),
  )
}

/** 取出当前角色可访问的全部页面路由（首页功能宫格用） */
export function filterAccessibleRoutes(
  routes: RouteRecordRaw[],
  role: Role | null,
): RouteRecordRaw[] {
  return routes.filter((route) => hasRoutePermission(route.meta?.roles, role))
}
