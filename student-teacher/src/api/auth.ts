import { http } from '@/utils/request'

import {
  normalizeRole,
} from '@/utils/role'

import type {
  Role,
} from '@/types/user'

/**
 * 后端：
 *
 * GET /api/auth/userinfo
 *
 * 原始返回结构。
 */
export interface UserInfoRaw {
  /**
   * sys_user.id
   */
  id:
    | string
    | number

  /**
   * CAS / 系统用户名。
   *
   * 这里只是登录账号，
   * 不再代表学号或工号。
   */
  username: string

  /**
   * ADMIN
   * STUDENT
   * TEACHER
   */
  role: string

  /**
   * sys_user.business_id
   *
   * STUDENT：
   * base_student.id
   *
   * TEACHER：
   * base_teacher.id
   */
  businessId?:
    | string
    | number
    | null
}

/**
 * 师生端真正使用的当前登录身份。
 */
export interface UserInfo {
  /**
   * sys_user.id
   *
   * 前端统一使用string，
   * 防止BIGINT精度丢失。
   */
  userId: string

  /**
   * CAS登录账号。
   */
  username: string

  /**
   * student / teacher
   */
  role: Role

  /**
   * 当前用户真正的业务档案ID。
   *
   * 学生：
   * base_student.id
   *
   * 教师：
   * base_teacher.id
   */
  businessId: string
}

/**
 * 获取当前JWT登录用户。
 *
 * 新身份流程：
 *
 * JWT
 * ↓
 * GET /api/auth/userinfo
 * ↓
 * sys_user
 * ↓
 * role + businessId
 *
 * 前端不再：
 *
 * username → studentNo
 * username → teacherNo
 */
export async function getUserInfo():
  Promise<UserInfo> {

  const raw =
    await http.get<UserInfoRaw>(
      '/auth/userinfo',
    )

  const role =
    normalizeRole(
      raw.role,
    )

  /**
   * ADMIN不能进入师生端。
   */
  if (!role) {
    throw new Error(
      `当前角色 ${raw.role} 不能进入师生端`,
    )
  }

  /**
   * STUDENT / TEACHER
   * 必须已经在sys_user中绑定business_id。
   */
  if (
    raw.businessId === null
    || raw.businessId === undefined
    || String(
      raw.businessId,
    ).trim() === ''
  ) {
    throw new Error(
      '当前账号未绑定业务档案',
    )
  }

  return {
    userId:
      String(
        raw.id,
      ),

    username:
      raw.username,

    role,

    businessId:
      String(
        raw.businessId,
      ),
  }
}