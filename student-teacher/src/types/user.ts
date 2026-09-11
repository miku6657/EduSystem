/** 师生端角色：只有学生与教师（管理员在 admin 工程） */
export type Role = 'student' | 'teacher'

export interface LoginParams {
  username: string
  password: string
}

/** 后端 /api/auth/login 的原始返回：{ token, user: { id, username, role } }；Mock 可能多带 roles/name */
export interface LoginResultRaw {
  token: string
  user?: {
    id?: number
    username?: string
    role?: string
  }
  roles?: string[]
  name?: string
}

/** 归一化后的登录结果 */
export interface LoginResult {
  token: string
  role: Role
  name: string
  /** 系统用户ID（sys_user.id），非学生/教师业务ID */
  userId: number
  username: string
}

/** 学生业务身份（base_student） */
export interface StudentProfile {
  id: number
  studentNo: string
  name: string
  gender?: string
  classId?: number
  className?: string
  majorName?: string
  phone?: string
  status?: string
}

/** 教师业务身份（base_teacher） */
export interface TeacherProfile {
  id: number
  teacherNo?: string
  name: string
  gender?: string
  departmentId?: number
  departmentName?: string
  title?: string
  teacherType?: string
  status?: string
}
