import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { getStudentByNo, getTeacherByNo } from '@/api/profile'
import { getCurrentTerm } from '@/api/term'
import type { LoginParams, StudentProfile, TeacherProfile } from '@/types/user'
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

export type Profile = StudentProfile | TeacherProfile

/**
 * 登录态与"业务身份"。
 *
 * 关键点：登录名（sys_user.username）与业务ID（student_id / teacher_id）不是一回事，
 * 登录后需要解析一次业务身份，后续页面统一用 userStore.businessId 请求数据。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    role: normalizeRole(getRole()),
    /** 登录名（学生的学号 / 教师的工号） */
    username: localStorage.getItem('st_username') ?? '',
    /** 登录返回的显示名 */
    name: getName(),
    /** 系统用户ID（sys_user.id） */
    userId: getUserId(),
    /** 业务身份：学生为 base_student 记录，教师为 base_teacher 记录 */
    profile: getCachedProfile<Profile>(),
    /** 当前学期（用于页面顶部展示） */
    currentTerm: '',
  }),

  getters: {
    isTeacher: (state) => state.role === 'teacher',
    isStudent: (state) => state.role === 'student',
    /** 业务身份ID：学生=studentId，教师=teacherId；为 0 表示尚未解析成功 */
    businessId: (state) => state.profile?.id ?? 0,
    /** 展示名优先用业务身份里的姓名 */
    displayName: (state) => state.profile?.name || state.name || state.username,
    /** 学号 / 工号 */
    businessNo: (state) => {
      const profile = state.profile
      if (!profile) {
        return state.username
      }
      return 'studentNo' in profile ? profile.studentNo : (profile.teacherNo ?? state.username)
    },
  },

  actions: {
    /** 登录：写入登录态并解析业务身份 */
    async login(params: LoginParams) {
      const result = await loginApi(params)
      this.token = result.token
      this.role = result.role
      this.name = result.name
      this.userId = result.userId
      this.username = result.username || params.username
      setToken(result.token)
      setRole(result.role)
      setName(result.name)
      setUserId(result.userId)
      localStorage.setItem('st_username', this.username)
      this.profile = null
      await this.resolveProfile(true)
      return result
    },

    /** 解析业务身份（登录名即学号/工号时可直接命中后端接口） */
    async resolveProfile(force = false): Promise<Profile | null> {
      if (this.profile && !force) {
        return this.profile
      }
      if (!this.role || !this.username) {
        return null
      }
      try {
        const profile =
          this.role === 'student'
            ? await getStudentByNo(this.username)
            : await getTeacherByNo(this.username)
        if (profile) {
          this.profile = profile
          setCachedProfile(profile)
        }
      } catch {
        // 解析失败不阻塞页面：页面会以 businessId=0 给出提示
      }
      return this.profile
    },

    /** 当前学期（只请求一次） */
    async loadTerm(): Promise<string> {
      if (this.currentTerm) {
        return this.currentTerm
      }
      try {
        this.currentTerm = await getCurrentTerm()
      } catch {
        this.currentTerm = ''
      }
      return this.currentTerm
    },

    /** 退出登录（后端暂无该接口，失败也照常清本地） */
    async logout() {
      try {
        await logoutApi()
      } catch {
        // 忽略：后端尚未实现 /api/auth/logout
      } finally {
        this.reset()
      }
    },

    /** 清空本地登录态 */
    reset() {
      this.token = ''
      this.role = null
      this.username = ''
      this.name = ''
      this.userId = 0
      this.profile = null
      this.currentTerm = ''
      clearAuth()
      localStorage.removeItem('st_username')
    },
  },
})
