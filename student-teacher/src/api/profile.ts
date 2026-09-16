import { http } from '@/utils/request'
import type { StudentProfile, TeacherProfile } from '@/types/user'

/**
 * 学生业务身份：登录名即学号时可直接解析。
 * 后端 GET /api/students?studentNo={studentNo}（RESTful 风格）
 */
export function getStudentByNo(studentNo: string) {
  return http.get<StudentProfile>('/students', { studentNo })
}

/**
 * 教师业务身份：登录名（工号）→ teacherId。
 * 后端 GET /api/teachers?teacherNo={teacherNo}
 */
export function getTeacherByNo(teacherNo: string) {
  return http.get<TeacherProfile>('/teachers', { teacherNo })
}
