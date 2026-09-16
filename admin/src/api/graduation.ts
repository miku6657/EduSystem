import { http } from '@/utils/request'

export type Id = string

export interface PageResult<T> {
  records: T[]
  total: number
  size?: number
  current?: number
  pages?: number
}

/**
 * 学生
 */
export interface GraduationStudent {
  id: Id
  studentNo: string
  name: string
  gender?: string
  classId?: Id
  phone?: string
  status?: string
}

/**
 * 毕业审核记录
 */
export interface GraduateCheck {
  id?: Id
  studentId: Id

  creditStatus?: 'WAIT' | 'PASS' | 'FAIL'
  courseStatus?: 'WAIT' | 'PASS' | 'FAIL'
  checkStatus?: 'WAIT' | 'PASS' | 'FAIL'

  remark?: string
  checker?: string

  createTime?: string
  updateTime?: string
}

/**
 * 页面最终使用的数据
 */
export interface GraduationAuditRow {
  studentId: Id
  studentNo: string
  name: string
  classId?: Id

  checkStatus: 'WAIT' | 'PASS' | 'FAIL'
  remark?: string
  checker?: string
}

/**
 * 查询学生
 *
 * GET /api/students
 */
export function getGraduationStudents(params?: {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: string
}) {
  return http.get<PageResult<GraduationStudent>>(
    '/students',
    {
      pageNo: params?.pageNo ?? 1,
      pageSize: params?.pageSize ?? 1000,
      keyword: params?.keyword,
      status: params?.status,
    },
  )
}

/**
 * 查询全部毕业审核记录
 *
 * GET /api/graduate-checks
 */
export function getGraduateChecks(
  checkStatus?: string,
) {
  return http.get<GraduateCheck[]>(
    '/graduate-checks',
    {
      checkStatus: checkStatus || undefined,
    },
  )
}

/**
 * 查询某个学生的毕业审核结果
 *
 * GET /api/graduate-checks/students/{studentId}
 */
export function getGraduateCheckByStudent(
  studentId: Id,
) {
  return http.get<GraduateCheck | null>(
    `/graduate-checks/students/${studentId}`,
  )
}

/**
 * 执行毕业审核
 *
 * 后端会自动查询该学生所有成绩：
 *
 * 有 score < 60 -> FAIL
 * 没有 score < 60 -> PASS
 *
 * POST /api/graduate-checks/audit
 */
export function auditGraduation(
  studentId: Id,
  checker = 'admin',
) {
  return http.post<void>(
    '/graduate-checks/audit',
    {
      studentId,
      checker,
    },
  )
}

/**
 * 把学生和审核记录合并成页面数据
 */
export function buildGraduationAuditRows(
  students: GraduationStudent[],
  checks: GraduateCheck[],
): GraduationAuditRow[] {
  const checkMap = new Map(
    checks.map((check) => [
      String(check.studentId),
      check,
    ]),
  )

  return students.map((student) => {
    const check = checkMap.get(
      String(student.id),
    )

    return {
      studentId: String(student.id),
      studentNo: student.studentNo,
      name: student.name,
      classId: student.classId
        ? String(student.classId)
        : undefined,

      checkStatus:
        check?.checkStatus ?? 'WAIT',

      remark: check?.remark,
      checker: check?.checker,
    }
  })
}
