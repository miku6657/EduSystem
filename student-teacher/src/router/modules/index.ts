import type { RouteRecordRaw } from 'vue-router'
import homeRoutes from './home'
import studentRoutes from './student'
import teacherRoutes from './teacher'

/** Layout 子路由聚合：同时用于 vue-router 注册与 tabbar / 功能宫格生成 */
export const layoutModuleRoutes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...studentRoutes,
  ...teacherRoutes,
]

export default layoutModuleRoutes
