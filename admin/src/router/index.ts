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
router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !userStore.token) {
    const query = to.fullPath !== '/' ? { redirect: to.fullPath } : undefined
    return { path: '/login', query }
  }
  // 已登录：进入布局页前先保证用户信息/角色就绪，供侧边栏按 roles 动态渲染
  if (requiresAuth && userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      // 拉取失败时放行，错误提示已由请求层处理
    }
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
