import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { hasRoutePermission } from '@/router/permission'
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
    path: '/',
    component: Layout,
    redirect: '/home',
    children: layoutModuleRoutes,
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

/**
 * 路由守卫：
 * 1. 未登录 → /login（带 redirect）
 * 2. 已登录但角色不匹配 meta.roles → 回首页并提示（**路由级权限校验**，
 *    这正是 admin 工程缺失的一环：那里只隐藏菜单，手输 URL 仍可进管理页）
 * 3. 已登录 → 补解析业务身份与当前学期（各只请求一次）
 */
router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !userStore.token) {
    return {
      path: '/login',
      query: to.fullPath !== '/' && to.fullPath !== '/home' ? { redirect: to.fullPath } : undefined,
    }
  }

  if (requiresAuth && userStore.token) {
    if (!userStore.role) {
      // 本地有 token 但角色丢失（例如手工清过缓存）：重新登录
      userStore.reset()
      return { path: '/login' }
    }
    if (!hasRoutePermission(to.meta.roles, userStore.role)) {
      showToast('当前角色无权访问该页面')
      return to.path === '/home' ? true : { path: '/home' }
    }
    await userStore.resolveProfile()
    await userStore.loadTerm()
  }

  if (to.path === '/login' && userStore.token) {
    return { path: '/home' }
  }
  return true
})

router.afterEach((to) => {
  const appTitle = import.meta.env.VITE_APP_TITLE || '教学过程管理系统 · 师生端'
  document.title = to.meta.title ? `${to.meta.title} - ${appTitle}` : appTitle
})

export default router
