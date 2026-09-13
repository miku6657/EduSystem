import type { RouteRecordRaw } from 'vue-router'

/** 教学管理（教师端）：我的调课（提交 / 查看 / 撤销） */
const courseAdjustRoutes: RouteRecordRaw[] = [
  {
    path: 'teacher/course-adjust',
    name: 'TeacherCourseAdjust',
    component: () => import('@/views/teacher/course-adjust.vue'),
    meta: { title: '我的调课', icon: 'exchange', roles: ['teacher'] },
  },
]

export default courseAdjustRoutes
