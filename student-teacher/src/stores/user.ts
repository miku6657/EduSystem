import {
  defineStore,
} from 'pinia'

import {
  getUserInfo,
} from '@/api/auth'

import {
  getStudentById,
  getTeacherById,
} from '@/api/profile'

import {
  getCurrentTerm,
} from '@/api/term'

import type {
  DbId,
  Role,
  StudentProfile,
  TeacherProfile,
} from '@/types/user'

export type Profile =
  | StudentProfile
  | TeacherProfile

interface UserState {
  /**
   * Spring Boot签发的JWT
   */
  token: string

  /**
   * student / teacher
   */
  role:
    | Role
    | null

  /**
   * CAS登录账号。
   *
   * 注意：
   * 这里已经不代表学号/工号。
   */
  username: string

  /**
   * sys_user.id
   */
  userId: DbId

  /**
   * sys_user.business_id
   *
   * STUDENT：
   * base_student.id
   *
   * TEACHER：
   * base_teacher.id
   */
  businessId: DbId

  /**
   * 当前学生或教师的真实数据库档案
   */
  profile:
    | Profile
    | null

  /**
   * 用于界面显示的真实姓名
   */
  name: string

  /**
   * 当前学期名称
   */
  currentTerm: string
}

/**
 * 登录态 Key
 */
const TOKEN_KEY =
  'st_token'

const ROLE_KEY =
  'st_role'

const USERNAME_KEY =
  'st_username'

const USER_ID_KEY =
  'st_user_id'

const BUSINESS_ID_KEY =
  'st_business_id'

const PROFILE_KEY =
  'st_profile'

const NAME_KEY =
  'st_name'

/**
 * 从本地恢复业务档案。
 */
function loadCachedProfile():
  Profile | null {

  const raw =
    localStorage.getItem(
      PROFILE_KEY,
    )

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(
      raw,
    ) as Profile
  } catch {
    return null
  }
}

/**
 * 从本地恢复角色。
 */
function loadRole():
  Role | null {

  const role =
    localStorage.getItem(
      ROLE_KEY,
    )

  if (
    role === 'student'
    || role === 'teacher'
  ) {
    return role
  }

  return null
}

/**
 * 师生端用户状态
 */
export const useUserStore =
  defineStore(
    'user',
    {
      state: (): UserState => ({
        token:
          sessionStorage.getItem(
            TOKEN_KEY,
          ) ?? '',

        role:
          loadRole(),

        username:
          localStorage.getItem(
            USERNAME_KEY,
          ) ?? '',

        userId:
          localStorage.getItem(
            USER_ID_KEY,
          ) ?? '',

        businessId:
          localStorage.getItem(
            BUSINESS_ID_KEY,
          ) ?? '',

        profile:
          loadCachedProfile(),

        name:
          localStorage.getItem(
            NAME_KEY,
          ) ?? '',

        currentTerm:
          '',
      }),

      getters: {
        /**
         * 是否教师
         */
        isTeacher:
          (state) =>
            state.role
            === 'teacher',

        /**
         * 是否学生
         */
        isStudent:
          (state) =>
            state.role
            === 'student',

        /**
         * 页面显示姓名。
         */
        displayName:
          (state) =>
            state.profile?.name
            || state.name
            || state.username,

        /**
         * 是否已经加载真实业务身份。
         */
        hasBusinessProfile:
          (state) =>
            Boolean(
              state.businessId
              && state.profile,
            ),
      },

      actions: {
        /**
         * CAS登录完成。
         *
         * 此时：
         *
         * CAS
         * ↓
         * Spring Boot
         * ↓
         * JWT
         * ↓
         * 5174/cas/callback
         *
         * 本方法接收JWT，
         * 然后加载真正的业务身份。
         */
        async completeCasLogin(
          token: string,
        ) {
          /**
           * 1. 保存JWT
           */
          this.token =
            token

          sessionStorage.setItem(
            TOKEN_KEY,
            token,
          )

          try {
            /**
             * 2. 获取sys_user身份
             *
             * GET /api/auth/userinfo
             *
             * 返回：
             *
             * userId
             * username
             * role
             * businessId
             */
            const userInfo =
              await getUserInfo()

            this.userId =
              userInfo.userId

            this.username =
              userInfo.username

            this.role =
              userInfo.role

            this.businessId =
              userInfo.businessId

            /**
             * 3. 保存基础身份
             */
            localStorage.setItem(
              USER_ID_KEY,
              userInfo.userId,
            )

            localStorage.setItem(
              USERNAME_KEY,
              userInfo.username,
            )

            localStorage.setItem(
              ROLE_KEY,
              userInfo.role,
            )

            localStorage.setItem(
              BUSINESS_ID_KEY,
              userInfo.businessId,
            )

            /**
             * 4. 清除上一个账号的档案缓存
             */
            this.profile =
              null

            this.name =
              ''

            localStorage.removeItem(
              PROFILE_KEY,
            )

            localStorage.removeItem(
              NAME_KEY,
            )

            /**
             * 5. 根据businessId加载
             * base_student/base_teacher
             */
            await this.resolveProfile(
              true,
            )

            return userInfo
          } catch (error) {
            /**
             * 任意登录初始化失败，
             * 都不能留下半套登录状态。
             */
            this.reset()

            throw error
          }
        },

        /**
         * 加载真实业务档案。
         *
         * 新流程：
         *
         * role + businessId
         * ↓
         *
         * student
         * → GET /api/students/{businessId}
         *
         * teacher
         * → GET /api/teachers/{businessId}
         *
         * 已经完全不使用：
         *
         * username → studentNo
         * username → teacherNo
         */
        async resolveProfile(
          force = false,
        ): Promise<
          Profile | null
        > {
          /**
           * 已经有缓存，
           * 并且没有要求强刷。
           */
          if (
            this.profile
            && !force
          ) {
            return this.profile
          }

          /**
           * 身份信息不完整，
           * 无法查询业务档案。
           */
          if (
            !this.token
            || !this.role
            || !this.businessId
          ) {
            return null
          }

          try {
            let profile:
              Profile

            /**
             * 学生
             */
            if (
              this.role
              === 'student'
            ) {
              profile =
                await getStudentById(
                  this.businessId,
                )
            }

            /**
             * 教师
             */
            else {
              profile =
                await getTeacherById(
                  this.businessId,
                )
            }

            /**
             * 保存真实业务档案
             */
            this.profile =
              profile

            this.name =
              profile.name

            localStorage.setItem(
              PROFILE_KEY,
              JSON.stringify(
                profile,
              ),
            )

            localStorage.setItem(
              NAME_KEY,
              profile.name,
            )

            return profile
          } catch (error) {
            /**
             * business_id存在，
             * 但是对应学生/教师不存在，
             * 属于数据库关联错误。
             *
             * 不再像旧代码那样
             * 静默吞掉异常。
             */
            this.profile =
              null

            this.name =
              ''

            localStorage.removeItem(
              PROFILE_KEY,
            )

            localStorage.removeItem(
              NAME_KEY,
            )

            throw error
          }
        },

        /**
         * 加载当前学期。
         */
        async loadTerm():
          Promise<string> {

          if (
            this.currentTerm
          ) {
            return this.currentTerm
          }

          try {
            this.currentTerm =
              await getCurrentTerm()
          } catch {
            this.currentTerm =
              ''
          }

          return this.currentTerm
        },

        /**
         * 退出师生端。
         *
         * JWT是无状态认证，
         * 不再调用旧的
         * POST /api/auth/logout。
         */
        logout() {
          this.reset()

          /**
           * 回到统一登录入口。
           */
          window.location.replace(
            'http://localhost:5173/login',
          )
        },

        /**
         * 清空全部师生端身份。
         */
        reset() {
          this.token =
            ''

          this.role =
            null

          this.username =
            ''

          this.userId =
            ''

          this.businessId =
            ''

          this.profile =
            null

          this.name =
            ''

          this.currentTerm =
            ''

          sessionStorage.removeItem(
            TOKEN_KEY,
          )

          localStorage.removeItem(
            ROLE_KEY,
          )

          localStorage.removeItem(
            USERNAME_KEY,
          )

          localStorage.removeItem(
            USER_ID_KEY,
          )

          localStorage.removeItem(
            BUSINESS_ID_KEY,
          )

          localStorage.removeItem(
            PROFILE_KEY,
          )

          localStorage.removeItem(
            NAME_KEY,
          )
        },
      },
    },
  )
