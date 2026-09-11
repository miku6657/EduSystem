import type { RouteRecordRaw } from 'vue-router'

/** 首页（学生 / 教师共用） */
const homeRoutes: RouteRecordRaw[] = [
  {
    path: 'home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', icon: 'wap-home-o', tabbar: true },
  },
]

export default homeRoutes
