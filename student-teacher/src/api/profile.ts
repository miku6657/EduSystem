import { http } from '@/utils/request'
import type { StudentProfile, TeacherProfile } from '@/types/user'

/**
 * 学生业务身份：登录名即学号时可直接解析。
 * 对应后端 GET /api/student/by-no/{studentNo}（已实现）。
 */
export function getStudentByNo(studentNo: string) {
  return http.get<StudentProfile>(`/student/by-no/${studentNo}`)
}

/**
 * 教师业务身份：登录名 → teacherId。
 * ⚠️ 缺口：后端 TeacherController 只有 /api/teacher/page、/{id}、/list-by-department，
 * 没有 by-no 接口；当前由 Mock 提供 /api/teacher/by-no/{teacherNo}，待后端补。
 */
export function getTeacherByNo(teacherNo: string) {
  return http.get<TeacherProfile>(`/teacher/by-no/${teacherNo}`)
}
