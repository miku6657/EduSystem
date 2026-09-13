import type { RouteRecordRaw } from 'vue-router'
import { Calendar } from '@element-plus/icons-vue'

/** 调课管理模块（调课审批[管理端] + 我的调课[师生端]） */
const courseAdjustRoutes: RouteRecordRaw[] = [
  {
    path: 'course-adjust',
    name: 'CourseAdjust',
    redirect: '/course-adjust/audit-list',
    meta: { title: '调课管理', icon: Calendar, roles: ['ADMIN', 'MANAGER'] },
    children: [
      {
        path: 'audit-list',
        name: 'CourseAdjustAuditList',
        component: () => import('@/views/course-adjust/audit-list.vue'),
        meta: { title: '调课审批' },
      },
      {
        path: 'my-list',
        name: 'CourseAdjustMyList',
        component: () => import('@/views/course-adjust/my-list.vue'),
        meta: { title: '我的调课' },
      },
    ],
  },
]

export default courseAdjustRoutes