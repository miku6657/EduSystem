import type { RouteRecordRaw } from 'vue-router'

import homeRoutes from './home'
import classroomRoutes from './classroom'
import studentRoutes from './student'
import teacherRoutes from './teacher'
import courseAdjustRoutes from './course-adjust'

/**
 * Layout 子路由聚合
 */
export const layoutModuleRoutes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...classroomRoutes,
  ...studentRoutes,
  ...teacherRoutes,
  ...courseAdjustRoutes,
]

export default layoutModuleRoutes