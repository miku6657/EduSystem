import type { RouteRecordRaw } from 'vue-router'

/**
 * 教室申请（师生端共用）：
 * 不限制 roles —— 学生和教师都可以提交教室使用申请并查看自己的申请记录。
 */
const classroomRoutes: RouteRecordRaw[] = [
  {
    path: 'classroom-apply',
    name: 'ClassroomApply',
    component: () => import('@/views/classroom-apply/index.vue'),
    meta: { title: '教室申请', icon: 'shop-o' },
  },
]

export default classroomRoutes
