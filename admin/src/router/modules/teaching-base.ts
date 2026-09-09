import type { RouteRecordRaw } from 'vue-router'
import { Notebook } from '@element-plus/icons-vue'

/**
 * 教学基础数据模块：
 * 作为侧边栏分组（含子路由），子路由 /base-data/course、/base-data/class 自动递归渲染到菜单。
 */
const teachingBaseRoutes: RouteRecordRaw[] = [
  {
    path: 'base-data',
    name: 'BaseData',
    redirect: '/base-data/course',
    meta: { title: '教学基础数据', icon: Notebook, roles: ['admin', 'manager'] },
    children: [
      {
        path: 'course',
        name: 'CourseManage',
        component: () => import('@/views/base-data/course.vue'),
        meta: { title: '课程管理' },
      },
      {
        path: 'class',
        name: 'ClassManage',
        component: () => import('@/views/base-data/class.vue'),
        meta: { title: '班级管理' },
      },
    ],
  },
]

export default teachingBaseRoutes
