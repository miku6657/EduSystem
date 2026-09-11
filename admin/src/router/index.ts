import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { layoutModuleRoutes } from '@/router/modules'

const Layout = () => import('@/views/layout/index.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/cas/callback',
    name: 'CasCallback',
    component: () => import('@/views/login/CasCallback.vue'),
    meta: { title: '统一身份认证', requiresAuth: false },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: layoutModuleRoutes,
  },
  {
    path: '/mobile',
    name: 'MobileDemo',
    component: () => import('@/views/mobile/index.vue'),
    meta: { title: '移动端示例（Vant）', requiresAuth: false },
  },
  {
    path: '/mobile/classroom-apply',
    name: 'MobileClassroomApply',
    component: () => import('@/views/classroom-apply/mobile.vue'),
    meta: { title: '教室申请（H5）', requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 登录守卫：无 token 且目标页需要登录时，强制跳转 /login
router.beforeEach((to) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !userStore.token) {
    const query = to.fullPath !== '/' ? { redirect: to.fullPath } : undefined
    return { path: '/login', query }
  }
  if (to.path === '/login' && userStore.token) {
    return { path: '/dashboard' }
  }
  return true
})

router.afterEach((to) => {
  const appTitle = import.meta.env.VITE_APP_TITLE || '教学过程管理系统'
  document.title = to.meta.title ? `${to.meta.title} - ${appTitle}` : appTitle
})

export default router
