import { defineStore } from 'pinia'
import { getUserInfo, login as loginApi, logout as logoutApi } from '@/api/auth'
import { getCurrentRoles } from '@/api/auth'
import type { LoginParams, UserInfo } from '@/types/user'
import { getToken, removeToken, setToken } from '@/utils/storage'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: null as UserInfo | null,
    /** 当前用户角色：驱动侧边栏动态菜单（admin / manager） */
    roles: [] as string[],
  }),
  actions: {
    /** 登录：token 写入 localStorage，并用返回的 name/roles 填充用户信息 */
    async login(params: LoginParams) {
      const result = await loginApi(params)
      this.token = result.token
      setToken(result.token)
      this.roles = result.roles
      this.userInfo = {
        id: 0,
        username: params.username,
        name: result.name,
        roles: result.roles,
      }
      return result
    },

    /** 预留：通过接口动态获取角色（当前 mock 固定返回 admin，后端联调后按需调整） */
    async fetchRoles() {
      const roles = await getCurrentRoles()
      this.roles = roles
      return roles
    },

    /** 刷新页面后 token 仍在时，补拉当前用户信息 */
    async fetchUserInfo() {
      const userInfo = await getUserInfo()
      this.userInfo = userInfo
      this.roles = userInfo.roles
      return userInfo
    },

    /** 退出登录 */
    async logout() {
      try {
        await logoutApi()
      } finally {
        this.reset()
      }
    },

    /** 清空本地登录态 */
    reset() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      removeToken()
    },
  },
})
