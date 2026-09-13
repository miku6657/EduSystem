import type { RouteRecordRaw } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

/** 工作台（Layout 子路由，自动生成侧边栏菜单） */
const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '工作台', icon: HomeFilled, roles: ['ADMIN'] },
  },
]

export default dashboardRoutes
