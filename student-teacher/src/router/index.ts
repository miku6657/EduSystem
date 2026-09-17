import {
  createRouter,
  createWebHistory,
} from 'vue-router'
import type {
  RouteRecordRaw,
} from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import {
  hasRoutePermission,
} from '@/router/permission'
import {
  layoutModuleRoutes,
} from '@/router/modules'

const Layout =
  () => import('@/views/layout/index.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component:
      () => import(
        '@/views/login/index.vue'
      ),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },

  /**
   * CAS 登录回调
   *
   * 管理端会把 STUDENT / TEACHER
   * 转发到这里。
   */
  {
    path: '/cas/callback',
    name: 'CasCallback',
    component:
      () => import(
        '@/views/login/CasCallback.vue'
      ),
    meta: {
      title: '统一身份认证',
      requiresAuth: false,
    },
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
    component:
      () => import(
        '@/views/error/404.vue'
      ),
    meta: {
      title: '页面不存在',
      requiresAuth: false,
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  /**
   * 师生端不再提供自己的登录入口
   */
  if (
    to.path === '/login'
    && !userStore.token
  ) {
    window.location.replace(
      'http://localhost:5173/login',
    )

    return false
  }

  const requiresAuth =
    to.meta.requiresAuth !== false

  /**
   * 未登录访问业务页面
   */
  if (
    requiresAuth
    && !userStore.token
  ) {
    window.location.replace(
      'http://localhost:5173/login',
    )

    return false
  }

  /**
   * 已登录访问业务页面
   */
 if (
  requiresAuth
  && userStore.token
) {
  /**
   * token存在，但角色不存在，
   * 说明本地身份状态已经不完整。
   */
  if (!userStore.role) {
    userStore.reset()

    window.location.replace(
      'http://localhost:5173/login',
    )

    return false
  }

  /**
   * 路由角色权限判断。
   */
  if (
    !hasRoutePermission(
      to.meta.roles,
      userStore.role,
    )
  ) {
    showToast(
      '当前角色无权访问该页面',
    )

    return to.path === '/home'
      ? true
      : {
          path: '/home',
        }
  }

  /**
   * 加载业务档案属于页面数据初始化，
   * 不能因为加载失败就把整个Vue路由卡死。
   *
   * businessId已经来自：
   *
   * sys_user.business_id
   *
   * 所以即使profile请求失败，
   * 也允许用户先进入页面。
   */
  if (!userStore.profile) {
    try {
      await userStore.resolveProfile()
    } catch (error) {
      console.error(
        '加载业务档案失败：',
        error,
      )
    }
  }

  /**
   * 当前学期加载失败同样不能阻断导航。
   */
  if (!userStore.currentTerm) {
    try {
      await userStore.loadTerm()
    } catch (error) {
      console.error(
        '加载当前学期失败：',
        error,
      )
    }
  }
}

  /**
   * 已登录再次访问登录页
   */
  if (
    to.path === '/login'
    && userStore.token
  ) {
    return {
      path: '/home',
    }
  }

  return true
})

router.afterEach((to) => {
  const appTitle =
    import.meta.env.VITE_APP_TITLE
    || '教学过程管理系统 · 师生端'

  document.title =
    to.meta.title
      ? `${to.meta.title} - ${appTitle}`
      : appTitle
})

export default router