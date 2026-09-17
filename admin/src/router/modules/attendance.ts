import type { RouteRecordRaw } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'

/** 考勤管理模块（含 教师考勤日志 等子路由） */
const attendanceRoutes: RouteRecordRaw[] = [
  {
    path: 'attendance',
    name: 'Attendance',
    redirect: '/attendance/log-list',
    meta: { title: '考勤管理', icon: Clock, roles: ['ADMIN'] },
    children: [
      {
        path: 'log-list',
        name: 'AttendanceLogList',
        component: () => import('@/views/attendance/log-list.vue'),
        meta: { title: '教师考勤日志' },
      },
      {
        path: 'teaching-log',
        name: 'TeachingLogManage',
        component: () => import('@/views/attendance/teaching-log.vue'),
        meta: { title: '教学日志' },
      },
    ],
  },
]

export default attendanceRoutes
