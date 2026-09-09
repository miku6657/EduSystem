import { defineStore } from 'pinia'
import { getCurrentTerm } from '@/api/term'
import { getStorage, setStorage } from '@/utils/storage'

const SIDEBAR_KEY = 'app:sidebar-collapsed'

export const useAppStore = defineStore('app', {
  state: () => ({
    /** 当前学期（顶部导航展示，mock 接口 /api/term/current） */
    currentTerm: '',
    /** 侧边栏是否折叠 */
    sidebarCollapsed: getStorage<boolean>(SIDEBAR_KEY) ?? false,
  }),
  actions: {
    /** 获取当前学期 */
    async fetchCurrentTerm() {
      const term = await getCurrentTerm()
      this.currentTerm = term
      return term
    },

    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
      setStorage(SIDEBAR_KEY, this.sidebarCollapsed)
    },
  },
})
