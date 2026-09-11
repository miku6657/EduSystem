import { http } from '@/utils/request'

/** 毕业资格审核（graduate_check.check_status：WAIT/PASS/FAIL；credit_status/course_status：PASS/FAIL） */
export interface GraduateCheck {
  id?: number
  studentId: number
  checkStatus?: string
  /** 学分是否合格 */
  creditStatus?: string
  /** 课程是否合格 */
  courseStatus?: string
  remark?: string
  /** 审核人 */
  checker?: string
  /** 展示用扩展字段（后端实体没有，Mock 会带） */
  studentName?: string
  studentNo?: string
  updateTime?: string
}

/**
 * 学生：查询本人毕业资格审核结果
 * 后端 GET /api/graduate-check/by-student/{studentId}
 */
export function getMyGraduateCheck(studentId: number) {
  return http.get<GraduateCheck | null>(`/graduate-check/by-student/${studentId}`)
}
