import type { RouteRecordRaw } from 'vue-router'
import { Reading } from '@element-plus/icons-vue'

/** 教材管理模块（含 新书入库 等子路由） */
const textbookRoutes: RouteRecordRaw[] = [
  {
    path: 'textbook',
    name: 'Textbook',
    redirect: '/textbook/stock-in',
    meta: { title: '教材管理', icon: Reading, roles: ['admin'] },
    children: [
      {
        path: 'stock-in',
        name: 'TextbookStockIn',
        component: () => import('@/views/textbook/stock-in.vue'),
        meta: { title: '新书入库' },
      },
    ],
  },
]

export default textbookRoutes
