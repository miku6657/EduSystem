import type { RouteRecordRaw } from 'vue-router'
import { CircleCheck } from '@element-plus/icons-vue'

/** 毕业审核模块（含 毕业生管理 等子路由） */
const graduationRoutes: RouteRecordRaw[] = [
  {
    path: 'graduation',
    name: 'Graduation',
    redirect: '/graduation/student-list',
    meta: { title: '毕业审核', icon: CircleCheck, roles: ['admin', 'manager'] },
    children: [
      {
        path: 'student-list',
        name: 'GraduationStudentList',
        component: () => import('@/views/graduation/student-list.vue'),
        meta: { title: '毕业生管理' },
      },
    ],
  },
]

export default graduationRoutes
