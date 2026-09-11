import type { RouteRecordRaw } from 'vue-router'

/** 教师端页面（全部 roles: ['teacher']） */
const teacherRoutes: RouteRecordRaw[] = [
  {
    path: 'teacher/teaching-log',
    name: 'TeacherTeachingLog',
    component: () => import('@/views/teacher/teaching-log.vue'),
    meta: { title: '教学日志', icon: 'notes-o', roles: ['teacher'], tabbar: true },
  },
  {
    path: 'teacher/check-in',
    name: 'TeacherCheckIn',
    component: () => import('@/views/teacher/check-in.vue'),
    meta: { title: '我的签到', icon: 'clock-o', roles: ['teacher'], tabbar: true },
  },
  {
    path: 'teacher/scores',
    name: 'TeacherScores',
    component: () => import('@/views/teacher/scores.vue'),
    meta: { title: '成绩录入', icon: 'edit', roles: ['teacher'], tabbar: true },
  },
  {
    path: 'teacher/student-attendance',
    name: 'TeacherStudentAttendance',
    component: () => import('@/views/teacher/student-attendance.vue'),
    meta: { title: '学生考勤', icon: 'friends-o', roles: ['teacher'] },
  },
  {
    path: 'teacher/method-apply',
    name: 'TeacherMethodApply',
    component: () => import('@/views/teacher/method-apply.vue'),
    meta: { title: '考核方式申报', icon: 'description', roles: ['teacher'] },
  },
  {
    path: 'teacher/invigilation',
    name: 'TeacherInvigilation',
    component: () => import('@/views/teacher/invigilation.vue'),
    meta: { title: '我的监考', icon: 'eye-o', roles: ['teacher'] },
  },
]

export default teacherRoutes
