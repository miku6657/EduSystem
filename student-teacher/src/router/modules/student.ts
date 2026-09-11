import type { RouteRecordRaw } from 'vue-router'

/**
 * 学生端页面。
 * 全部标注 roles: ['student']，路由守卫与底部 tabbar 都按此过滤（学生看不到教师页，反之亦然）。
 */
const studentRoutes: RouteRecordRaw[] = [
  {
    path: 'student/scores',
    name: 'StudentScores',
    component: () => import('@/views/student/scores.vue'),
    meta: { title: '我的成绩', icon: 'bar-chart-o', roles: ['student'], tabbar: true },
  },
  {
    path: 'student/attendance',
    name: 'StudentAttendance',
    component: () => import('@/views/student/attendance.vue'),
    meta: { title: '我的考勤', icon: 'records', roles: ['student'], tabbar: true },
  },
  {
    path: 'student/retake',
    name: 'StudentRetake',
    component: () => import('@/views/student/retake.vue'),
    meta: { title: '补考重修', icon: 'todo-list-o', roles: ['student'], tabbar: true },
  },
  {
    path: 'student/upgrade',
    name: 'StudentUpgrade',
    component: () => import('@/views/student/upgrade.vue'),
    meta: { title: '专升本报名', icon: 'upgrade', roles: ['student'] },
  },
  {
    path: 'student/graduation',
    name: 'StudentGraduation',
    component: () => import('@/views/student/graduation.vue'),
    meta: { title: '毕业资格', icon: 'certificate', roles: ['student'] },
  },
]

export default studentRoutes
