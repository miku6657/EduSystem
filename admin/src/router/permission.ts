import type { RouteRecordRaw } from 'vue-router'

/** 判断当前用户角色是否命中菜单要求 */
export function hasMenuPermission(
  requiredRoles: string[] | undefined,
  currentRoles: string[],
) {
  if (!requiredRoles || requiredRoles.length === 0) {
    return true
  }
  return requiredRoles.some((role) => currentRoles.includes(role))
}

/**
 * 根据用户角色递归过滤侧边栏菜单：
 * - ADMIN：全部菜单可见（meta.roles 含 ADMIN 或未配置）
 * - MANAGER：仅教学基础数据 / 教室申请 / 调课管理 / 毕业审核
 */
export function filterMenusByRoles(routes: RouteRecordRaw[], roles: string[]) {
  const result: RouteRecordRaw[] = []
  for (const route of routes) {
    if (!hasMenuPermission(route.meta?.roles, roles)) {
      continue
    }
    const item: RouteRecordRaw = { ...route }
    if (route.children?.length) {
      const children = filterMenusByRoles(route.children, roles)
      if (children.length === 0) {
        continue
      }
      item.children = children
    }
    if (item.meta?.title && item.meta.menu !== false) {
      result.push(item)
    }
  }
  return result
}
