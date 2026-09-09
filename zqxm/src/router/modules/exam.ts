import type { RouteRecordRaw } from 'vue-router'
import { Tickets } from '@element-plus/icons-vue'

/** 考务管理模块（自动排考 + 考核方式申报审批） */
const examRoutes: RouteRecordRaw[] = [
  {
    path: 'exam',
    name: 'Exam',
    redirect: '/exam/arrange',
    meta: { title: '考务管理', icon: Tickets, roles: ['admin'] },
    children: [
      {
        path: 'arrange',
        name: 'ExamArrange',
        component: () => import('@/views/exam/arrange.vue'),
        meta: { title: '自动排考' },
      },
      {
        path: 'method-audit',
        name: 'ExamMethodAudit',
        component: () => import('@/views/exam/method-audit.vue'),
        meta: { title: '考核方式申报' },
      },
    ],
  },
]

export default examRoutes