import { defineStore } from 'pinia'
import {
  getUserInfo,
  login as loginApi,
} from '@/api/auth'
import type { LoginParams, UserInfo } from '@/types/user'
import {
  getStorage,
  getToken,
  removeStorage,
  removeToken,
  setStorage,
  setToken,
} from '@/utils/storage'

const USER_INFO_KEY = 'userInfo'

export const useUserStore = defineStore('user', {
  state: () => {
    const userInfo = getStorage<UserInfo>(USER_INFO_KEY)

    return {
      token: getToken(),
      userInfo,
      roles: userInfo ? [userInfo.role] : [] as string[],
    }
  },

  actions: {
    async login(params: LoginParams) {
      const result = await loginApi(params)

      this.token = result.token
      setToken(result.token)

      this.userInfo = result.user
      setStorage(USER_INFO_KEY, result.user)

      this.roles = [result.user.role]

      return result
    },

    async completeCasLogin(token: string) {
      this.token = token
      setToken(token)

      const userInfo = await getUserInfo()

      this.userInfo = userInfo
      this.roles = [userInfo.role]
      setStorage(USER_INFO_KEY, userInfo)

      return userInfo
    },

    logout() {
      this.reset()
    },

    reset() {
      this.token = ''
      this.userInfo = null
      this.roles = []

      removeToken()
      removeStorage(USER_INFO_KEY)
    },
  },
})
