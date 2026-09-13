import type { RouteRecordRaw } from 'vue-router'
import homeRoutes from './home'
import classroomRoutes from './classroom'
import timetableRoutes from './timetable'
import studentRoutes from './student'
import teacherRoutes from './teacher'
import courseAdjustRoutes from './course-adjust'

/** Layout 子路由聚合：同时用于 vue-router 注册与 tabbar / 功能宫格生成 */
export const layoutModuleRoutes: RouteRecordRaw[] = [
  ...homeRoutes,
  ...classroomRoutes,
  ...timetableRoutes,
  ...studentRoutes,
  ...teacherRoutes,
  ...courseAdjustRoutes,
]

export default layoutModuleRoutes
