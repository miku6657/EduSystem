import { defineStore } from 'pinia'
import {
  getUserInfo,
  login as loginApi,
  logout as logoutApi,
} from '@/api/auth'
import { getStudentByNo, getTeacherByNo } from '@/api/profile'
import { getCurrentTerm } from '@/api/term'
import type {
  LoginParams,
  StudentProfile,
  TeacherProfile,
} from '@/types/user'
import { normalizeRole } from '@/utils/role'
import {
  clearAuth,
  getCachedProfile,
  getName,
  getRole,
  getToken,
  getUserId,
  setCachedProfile,
  setName,
  setRole,
  setToken,
  setUserId,
} from '@/utils/storage'

export type Profile =
  | StudentProfile
  | TeacherProfile

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),

    role: normalizeRole(
      getRole(),
    ),

    /**
     * CAS用户名：
     * 学生通常是学号
     * 教师通常是工号
     */
    username:
      localStorage.getItem(
        'st_username',
      ) ?? '',

    name: getName(),

    /**
     * sys_user.id
     */
    userId: getUserId(),

    /**
     * 学生/教师业务信息
     */
    profile:
      getCachedProfile<Profile>(),

    currentTerm: '',
  }),

  getters: {
    isTeacher: (state) =>
      state.role === 'teacher',

    isStudent: (state) =>
      state.role === 'student',

    /**
     * 学生：
     * base_student.id
     *
     * 教师：
     * base_teacher.id
     */
    businessId: (state) =>
      state.profile?.id ?? 0,

    displayName: (state) =>
      state.profile?.name
      || state.name
      || state.username,

    /**
     * 学号 / 工号
     */
    businessNo: (state) => {
      const profile =
        state.profile

      if (!profile) {
        return state.username
      }

      return 'studentNo' in profile
        ? profile.studentNo
        : (
            profile.teacherNo
            ?? state.username
          )
    },
  },

  actions: {
    /**
     * 原账号密码登录。
     *
     * 暂时保留，
     * 后面登录页面统一CAS后不再使用。
     */
    async login(
      params: LoginParams,
    ) {
      const result =
        await loginApi(params)

      this.token = result.token
      this.role = result.role
      this.name = result.name
      this.userId = result.userId
      this.username =
        result.username
        || params.username

      setToken(result.token)
      setRole(result.role)
      setName(result.name)
      setUserId(result.userId)

      localStorage.setItem(
        'st_username',
        this.username,
      )

      this.profile = null
      localStorage.removeItem(
        'st_profile',
      )

      await this.resolveProfile(true)

      return result
    },

    /**
     * CAS登录完成。
     *
     * 此时后端已经完成CAS ticket校验，
     * 师生端拿到的是业务系统JWT。
     */
    async completeCasLogin(
      token: string,
    ) {
      /**
       * 1. 先保存JWT
       *
       * request.ts 会自动从
       * localStorage读取token并放到
       * Authorization请求头。
       */
      this.token = token
      setToken(token)

      try {
        /**
         * 2. 根据JWT查询当前用户
         */
        const userInfo =
          await getUserInfo()

        /**
         * 3. 保存系统登录身份
         */
        this.role = userInfo.role
        this.userId = userInfo.id
        this.username =
          userInfo.username
        this.name =
          userInfo.username

        setRole(userInfo.role)
        setUserId(userInfo.id)
        setName(userInfo.username)

        localStorage.setItem(
          'st_username',
          userInfo.username,
        )

        /**
         * 4. 清掉上一个账号缓存的
         * 学生/教师业务资料
         */
        this.profile = null

        localStorage.removeItem(
          'st_profile',
        )

        /**
         * 5. 根据CAS用户名继续解析：
         *
         * STUDENT
         * username → studentNo
         *
         * TEACHER
         * username → teacherNo
         */
        await this.resolveProfile(
          true,
        )

        return userInfo
      } catch (error) {
        /**
         * CAS token无效、用户不存在、
         * 或者ADMIN误进师生端，
         * 都清空本地登录态。
         */
        this.reset()

        throw error
      }
    },

    /**
     * 解析学生/教师业务身份
     */
    async resolveProfile(
      force = false,
    ): Promise<Profile | null> {
      if (
        this.profile
        && !force
      ) {
        return this.profile
      }

      if (
        !this.role
        || !this.username
      ) {
        return null
      }

      try {
        const profile =
          this.role === 'student'
            ? await getStudentByNo(
                this.username,
              )
            : await getTeacherByNo(
                this.username,
              )

        if (profile) {
          this.profile = profile

          setCachedProfile(
            profile,
          )
        }
      } catch {
        /**
         * 业务身份解析失败不直接退出。
         *
         * 登录依然成功，
         * 具体页面会通过businessId=0
         * 判断资料是否存在。
         */
      }

      return this.profile
    },

    /**
     * 加载当前学期
     */
    async loadTerm():
      Promise<string> {
      if (this.currentTerm) {
        return this.currentTerm
      }

      try {
        this.currentTerm =
          await getCurrentTerm()
      } catch {
        this.currentTerm = ''
      }

      return this.currentTerm
    },

    /**
     * 退出
     */
    async logout() {
      try {
        await logoutApi()
      } catch {
        // JWT无状态，接口失败也清本地
      } finally {
        this.reset()
      }
    },

    /**
     * 清空整个师生端登录状态
     */
    reset() {
      this.token = ''
      this.role = null
      this.username = ''
      this.name = ''
      this.userId = 0
      this.profile = null
      this.currentTerm = ''

      clearAuth()

      localStorage.removeItem(
        'st_username',
      )
    },
  },
})
