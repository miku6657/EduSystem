import type { RouteRecordRaw } from 'vue-router'

/** 课表 / 我的教学任务（学生与教师共用，不限制 roles） */
const timetableRoutes: RouteRecordRaw[] = [
  {
    path: 'timetable',
    name: 'Timetable',
    component: () => import('@/views/timetable/index.vue'),
    meta: { title: '课表', icon: 'calendar-o' },
  },
]

export default timetableRoutes
