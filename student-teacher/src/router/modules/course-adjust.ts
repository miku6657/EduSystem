import type { RouteRecordRaw } from 'vue-router'

/** 教学管理（教师端）：调课申请（提交 / 查看我的调课 / 撤销） */
const courseAdjustRoutes: RouteRecordRaw[] = [
  {
    path: 'teacher/course-adjust',
    name: 'TeacherCourseAdjust',
    component: () => import('@/views/teacher/course-adjust.vue'),
    meta: { title: '调课申请', icon: 'exchange', roles: ['teacher'] },
  },
]

export default courseAdjustRoutes
