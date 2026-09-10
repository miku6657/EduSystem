import type { Role } from '@/types/user'

/** 角色别名表：后端 sys_user.role 存的是 ADMIN/TEACHER/STUDENT，前端统一用小写 student/teacher */
const ROLE_ALIAS: Record<string, Role> = {
  student: 'student',
  STUDENT: 'student',
  学生: 'student',
  teacher: 'teacher',
  TEACHER: 'teacher',
  教师: 'teacher',
}

/** 把后端/任意来源的角色字符串归一化为前端角色；无法识别时返回 null */
export function normalizeRole(raw?: string | null): Role | null {
  if (!raw) {
    return null
  }
  return ROLE_ALIAS[raw] ?? ROLE_ALIAS[raw.toUpperCase()] ?? null
}

/** 角色中文名 */
export function roleLabel(role: Role | null): string {
  if (role === 'student') {
    return '学生'
  }
  if (role === 'teacher') {
    return '教师'
  }
  return '未登录'
}
