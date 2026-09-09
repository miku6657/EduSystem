import type { RouteRecordRaw } from 'vue-router'
import dashboardRoutes from './dashboard'
import teachingBaseRoutes from './teaching-base'
import examRoutes from './exam'
import textbookRoutes from './textbook'
import classroomRoutes from './classroom'
import courseAdjustRoutes from './course-adjust'
import graduationRoutes from './graduation'
import upgradeRoutes from './upgrade'
import attendanceRoutes from './attendance'

/**
 * Layout 子路由聚合：
 * 同时用于 vue-router 注册与侧边栏菜单自动生成（见 views/layout/components/AppSidebar.vue）。
 * 新模块只需在 modules 目录下新增一个路由文件并在此聚合。
 */
export const layoutModuleRoutes: RouteRecordRaw[] = [
  ...dashboardRoutes,
  ...teachingBaseRoutes,
  ...examRoutes,
  ...textbookRoutes,
  ...classroomRoutes,
  ...courseAdjustRoutes,
  ...graduationRoutes,
  ...upgradeRoutes,
  ...attendanceRoutes,
]
