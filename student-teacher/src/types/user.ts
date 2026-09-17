/**
 * 师生端角色
 *
 * 管理员只进入 admin，
 * 所以这里不存在 admin。
 */
export type Role =
  | 'student'
  | 'teacher'

/**
 * 数据库主键统一使用 string。
 *
 * 原因：
 * 后端使用 BIGINT / Snowflake ID，
 * JavaScript number 无法安全保存。
 */
export type DbId = string

/**
 * 学生业务档案
 *
 * 数据来源：
 * base_student
 */
export interface StudentProfile {
  /**
   * base_student.id
   */
  id: DbId

  /**
   * 学号只是学生档案字段，
   * 不再参与登录和身份解析。
   */
  studentNo?: string

  name: string

  gender?: string

  /**
   * base_class.id
   */
  classId?: DbId

  className?: string

  majorName?: string

  phone?: string

  status?: string
}

/**
 * 教师业务档案
 *
 * 数据来源：
 * base_teacher
 */
export interface TeacherProfile {
  /**
   * base_teacher.id
   */
  id: DbId

  /**
   * 工号只是教师档案字段，
   * 不再参与登录和身份解析。
   */
  teacherNo?: string

  name: string

  gender?: string

  /**
   * base_department.id
   */
  departmentId?: DbId

  departmentName?: string

  /**
   * base_teaching_group.id
   */
  teachingGroupId?: DbId

  type?: string

  teacherType?: string

  title?: string

  phone?: string

  status?: string
}