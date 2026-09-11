import type { RouteRecordRaw } from 'vue-router'
import { OfficeBuilding } from '@element-plus/icons-vue'

/** 教室审批模块（Layout 子路由，自动生成侧边栏菜单；管理端只做审批，申请由师生端发起） */
const classroomRoutes: RouteRecordRaw[] = [
  {
    path: 'classroom',
    name: 'Classroom',
    component: () => import('@/views/classroom-apply/index.vue'),
    meta: { title: '教室审批', icon: OfficeBuilding, roles: ['ADMIN', 'MANAGER'] },
  },
]

export default classroomRoutes
