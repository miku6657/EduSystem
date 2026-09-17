import type { RouteRecordRaw } from 'vue-router'
import { TrendCharts } from '@element-plus/icons-vue'

/** 专升本模块（含 报名审核 等子路由） */
const upgradeRoutes: RouteRecordRaw[] = [
  {
    path: 'college-upgrade',
    name: 'CollegeUpgrade',
    redirect: '/college-upgrade/apply-list',
    meta: { title: '专升本', icon: TrendCharts, roles: ['ADMIN'] },
    children: [
      {
        path: 'apply-list',
        name: 'CollegeUpgradeApplyList',
        component: () => import('@/views/college-upgrade/apply-list.vue'),
        meta: { title: '报名审核' },
      },
    ],
  },
]

export default upgradeRoutes
