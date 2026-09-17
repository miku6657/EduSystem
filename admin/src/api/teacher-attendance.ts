import { http } from '@/utils/request'

export interface TeacherAttendanceRecord {
  id?: string

  teacherId: string

  attendanceDate: string

  status: string

  checkTime?: string

  createTime?: string

  updateTime?: string

  /**
   * admin展示字段
   */
  teacherName?: string

  teacherNo?: string
}

export interface TeacherAttendanceStat {
  total?: number

  checked?: number

  /**
   * 你当前后端字段叫 absent
   */
  absent?: number
}

interface TeacherItem {
  id: string

  teacherNo: string

  name: string
}

interface TeacherPage {
  records: TeacherItem[]

  total: number
}

/**
 * 查询某天全部教师签到记录。
 *
 * 真实后端：
 *
 * GET
 * /api/teacher-attendances?date=...
 */
export async function listTeacherAttendanceByDate(
  date: string,
): Promise<TeacherAttendanceRecord[]> {

  const [
    attendanceList,
    teacherPage,
  ] =
    await Promise.all([
      http.get<
        TeacherAttendanceRecord[]
      >(
        '/teacher-attendances',
        {
          date,
        },
      ),

      http.get<
        TeacherPage
      >(
        '/teachers',
        {
          pageNo: 1,
          pageSize: 1000,
        },
      ),
    ])

  const teacherMap =
    new Map(
      (
        teacherPage.records
        ?? []
      ).map(
        (teacher) => [
          String(
            teacher.id,
          ),
          teacher,
        ],
      ),
    )

  return (
    attendanceList
    ?? []
  ).map(
    (item) => {

      const teacher =
        teacherMap.get(
          String(
            item.teacherId,
          ),
        )

      return {
        ...item,

        id:
          item.id === undefined
          || item.id === null
            ? undefined
            : String(
                item.id,
              ),

        teacherId:
          String(
            item.teacherId,
          ),

        teacherName:
          teacher?.name
          ?? '未知教师',

        teacherNo:
          teacher?.teacherNo
          ?? '',
      }
    },
  )
}

/**
 * 当日教师考勤统计。
 *
 * 真实后端：
 *
 * GET
 * /api/teacher-attendances/statistics
 */
export function getTeacherAttendanceStat(
  date: string,
) {

  return http.get<
    TeacherAttendanceStat
  >(
    '/teacher-attendances/statistics',
    {
      date,
    },
  )
}
